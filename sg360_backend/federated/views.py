from django.shortcuts import render
from django.http import HttpResponse
from rest_framework import status
from rest_framework.views import APIView
from rest_framework.response import Response
import pandas as pd
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split
from sklearn.utils import shuffle
from sklearn.metrics import accuracy_score
from datetime import datetime
from django.conf import settings
from .models import *
from django.http import JsonResponse
import tensorflow as tf

key = settings.KEY_DECRYPT.encode('utf-8')

# Create your views here.

# Get the federated round count from settings
fed_Round_Count = settings.FED_ROUND_COUNT  # Federated round count

# Get the number of clients from settings
fed_CLIENT = settings.FED_CLIENT  # Number of clients

# Get the number of epochs from settings
fed_EPOCH = settings.FED_EPOCH  # Number of epochs

class clusterClient(APIView):
    """
    API View for the clusterClient.

    This class handles the POST request for the clusterClient.
    It receives the list of apps and the cluster name from the client.
    It performs the necessary operations based on the round count.
    """

    def post(self, request):
        """
        API endpoint for the clusterClient.

        This method handles the POST request for the clusterClient.
        It receives the list of apps and the cluster name from the client.
        It performs the necessary operations based on the round count.

        Args:
            request (HttpRequest): The HTTP request object.

        Returns:
            JsonResponse: The JSON response containing the cluster name, round count, and model data.
        """
        # Extract apps and cluster name from request data
        apps = request.data.get('apps')
        cluster_Name = request.data.get('clusterName')
        
        # Extract round count from cluster name
        round_Count = cluster_Name.split('_')[1]
        
        # Extract cluster name without round count
        cluster_Name = cluster_Name.split('_')[0]
        
        # Return error if apps or cluster name are missing
        if not apps or not cluster_Name:
            return JsonResponse({'error': 'Missing required fields'}, status=400)
        
        # Delete cluster if round count is -1
        if round_Count == "-1":
            cluster_Name = self.create_Cluster(int(round_Count), cluster_Name)
            return JsonResponse({
                'Cluster': cluster_Name,
                'Round': round_Count,
                'Epoch': fed_EPOCH,
                'modelData': []
            }, status=201)
        
        # Generate temporary data
        cl_Data = self.tempData(len(apps))
        
        # Create and assign new cluster if round count is -3
        if round_Count == "-3" and cluster_Name == "z":
            cluster_Name, round_Count = self.create_Cluster(delta=int(round_Count), name=cluster_Name)
            return JsonResponse({
                'Cluster': cluster_Name,
                'Round': round_Count,
                'Epoch': fed_EPOCH,
                'modelData': cl_Data
            }, status=201)
        
        # Return error if round count is not handled
        return Response({
            'Cluster': "Wrong point",
            'Round': 'Hit the calculation port',
            'Epoch': "Don't Know",
            'modelData': []
        }, status=status.HTTP_400_BAD_REQUEST)

    @staticmethod
    def tempData(size):
        """
        Generate temporary data for the client.

        Args:
            size (int): The size of the data to be generated.

        Returns:
            list: A list of clients containing the prepared data and labels.
        """
        
        # Read only 1000 random rows
        df = pd.read_csv('static/db_big.csv', nrows=1000)
        
        random_records = df.sample(n=size)
        data_list = random_records.iloc[:, 2:116]
        
        sc = StandardScaler()

        scaled = sc.fit_transform(data_list)

        scaled = tf.convert_to_tensor(scaled, dtype=tf.float32)
        
        # Convert tensor to json serializable
        def tensor_to_json_serializable(tensor):
            return tf.make_ndarray(tf.make_tensor_proto(tensor)).tolist()

        # Encrypt and return data
        return ('\n'.join(encrypt_data(','.join(map(str, i)), key) for i in tensor_to_json_serializable(scaled)))

    @staticmethod
    def create_Cluster(delta=0, name="Cluster0"):
        """
        Create or delete a cluster based on the round count.

        Args:
            delta (int, optional): The delta value. Defaults to 0.
            name (str, optional): The cluster name. Defaults to "Cluster0".

        Returns:
            tuple: A tuple containing the cluster name and round count.
        """
        cluster_objs = Cluster.objects.filter(name__startswith=name)
        
        if delta == -1:
            if cluster_objs.exists():
                if "A" in cluster_objs[0].numberString:
                    return "Cluster not yet assigned"
                else:
                    return "Cluster data not fully deleted"
            else:
                return "Cluster Does Not Exist"

        clusters = []

        if not cluster_objs.exists() and name=="z":
            cluster_objs = Cluster.objects.all()
            for i in cluster_objs:
                li = [y.split(",").index('A') if 'A' in y.split(",") else fed_Round_Count+1 for y in i.numberString.rstrip("\n").split("\n")]
                if all(x == li[0] and x < fed_Round_Count for x in li):
                    i.numberString = "\n".join([",".join(y.split(",")[:li[0]] + ["N/a"] + y.split(",")[li[0]+1:]) for y in i.numberString.rstrip("\n").split("\n")])
                    i.counter = float(1)
                    i.save()
                    return f"{i.name}_{str(li[0])}", fed_Round_Count
                else:
                    clusters.append(int(i.name.lstrip("Cluster")))

            new_cluster_name = f"Cluster{max(clusters, default=0) + 1}"

            string = ""

            string = "\n".join([",".join(["N/a" if j == 0 else "A" for j in range(fed_Round_Count)]) for _ in range(fed_CLIENT)])

            new_Obj = Cluster.objects.create(name=new_cluster_name, numberString=string)

            return f"{new_cluster_name}_0", fed_Round_Count

        return "ERROR", 0


class communicationRound(APIView):
    """
    View for clients to get cluster data.
    """
    def post(self, request):
        """
        Handle the GET request.

        Args:
            request (Request): The HTTP request object.

        Returns:
            Response: The HTTP response object.
        """
        # Extract cluster name, weights, best parameter, and model prediction from request data

        cluster_Name = request.data['clusterName']

        weights = request.data['weights']

        name = cluster_Name.split("_")[0]

        roundNo = int(cluster_Name.split("_")[1])

        bestParam = str(request.data['bestParam'])

        model_Predict = str(request.data['modelPredict'])

        # Record the parameters of the model
        output = self.record_params(name, roundNo, bestParam, weights, model_Predict)

        # If the output is "Call Aggregator", aggregate the cluster
        if output == "Call Aggregator":
            self.aggregate_Cluster(name)

        # Return the response with the output message
        return Response({"msg": output}, status=status.HTTP_200_OK)


    @staticmethod
    def record_params(name, roundNum, bestParam, weights: list, modelPredict):
        """
        Record the parameters of the model.

        Args:
            request (Request): The HTTP request object.

        Returns:
            Response: The HTTP response object.
        """
        cluster_objs = Cluster.objects.filter(name__startswith=name)

        for i in cluster_objs:
            li = [y.split(",") for y in i.numberString.rstrip("\n").split("\n")]
            for item in li:
                change = False
                try:
                    if item[roundNum] == "N/a":
                        if float(bestParam) >= float(modelPredict):
                            if i.counter == 1:
                                i.bestWeights = str(weights)
                                i.counter += 1
                            else:
                                a = list(i.bestWeights)
                        item[roundNum] = bestParam
                        change = True
                        break
                    elif item[roundNum] == "A":
                        return "Cluster not yet assigned"
                    change = False
                except (IndexError):
                    return "Round Number Too large"
            if change:
                i.numberString = "\n".join([",".join(y) for y in li])
                i.save()
                return "Recorded"
            else:
                if roundNum == fed_Round_Count-1:
                    if any(i != "N/a" and i != "A" for i in (item[roundNum] for item in li)):
                        return "Call Aggregator"
                    else:
                        return "Round Number Too large"

            
        if not cluster_objs:
            return "Cluster Does not Exist"

    @staticmethod
    def aggregate_Cluster(name):
        """
        Aggregate the cluster.

        Working on this.
        """
        print("Model aggregated.")
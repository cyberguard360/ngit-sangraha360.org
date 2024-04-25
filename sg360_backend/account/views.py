from rest_framework.response import Response
from rest_framework import status
from rest_framework.views import APIView
from account.serializers import  UserLoginSerializer, UserRegistrationSerializer, OTPSerializer
from django.contrib.auth import authenticate
from account.renderers import UserRenderer
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework.permissions import IsAuthenticated
from .models import *
import json
from django.http import HttpResponse
from django.views import View

import pandas as pd
import numpy as np
import pickle
from django.http import JsonResponse

from .email import *
from datetime import datetime

# Generate Token Manually


def get_tokens_for_user(user):
    """
    Generate tokens for a given user.

    Parameters:
    user (User): The user for whom the tokens are generated.

    Returns:
    dict: A dictionary containing the refresh and access tokens.
    """
    # Generate a refresh token for the user
    refresh = RefreshToken.for_user(user)
    
    # Return the refresh and access tokens as a dictionary
    return {
        'refresh': str(refresh),  # Convert the refresh token to a string
        'access': str(refresh.access_token),  # Convert the access token to a string
    }

class UserRegistrationView(APIView):
    """
    A View for User Registration
    """
    def post(self, request, format=None):
        """
        Register a new user

        Parameters:
        request (Request): The request containing the user data.
        format (str): The format of the response.

        Returns:
        Response: The response containing the tokens and a success message.
        """
        # Get the serializer data
        otp_confirm = ''.join([str(randint(0, 9)) for i in range(4)])

        data = request.data

        data["otp"] = otp_confirm

        serializer = UserRegistrationSerializer(data=data)

        if serializer.is_valid():
            # Save the user
            serializer.save()
            
            with open('static/otp.html', 'r') as html_file:
                html_content = html_file.read()
            html_content = html_content.replace('{name}', serializer.data["username"])

            # Send the OTP to the user's email
            send_otp_via_email(serializer.data["email"], otp_confirm , html_content)

            return Response({'msg':'OTP sent to your email'}, status=status.HTTP_201_CREATED)
        
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class UserLoginView(APIView):
    """
    A View for User Login
    """
    renderer_classes = [UserRenderer]
    permission_classes = [IsAuthenticated]
    
    def post(self, request, format=None):
        """
        Login a user

        Parameters:
        request (Request): The request containing the user's email and password.
        format (str): The format of the response.

        Returns:
        Response: The response containing the tokens and a success message if the login is successful, or an error message if the login is not valid.
        """
        # Get the serializer data
        serializer = UserLoginSerializer(data=request.data)
        
        # Validate the serializer data
        serializer.is_valid(raise_exception=True)
        
        # Get the email and password from the serializer data
        email = serializer.data.get('email')
        password = serializer.data.get('password')
        
        # Authenticate the user
        user = authenticate(email=email, password=password)
        
        # If the user exists and is authenticated, generate tokens for the user and return them and a success message
        if user is not None:
            return Response({'msg':'Login Success'}, status=status.HTTP_200_OK)
        
        # If the user does not exist or is not authenticated, return an error message
        else:
            return Response({'errors':{'non_field_errors':['Email or Password is not Valid']}}, status=status.HTTP_404_NOT_FOUND)

class AddDataToPostGresView(APIView):

    permission_classes = [IsAuthenticated]
    """
    View to add data to PostgreSQL database.
    """
    
    def post(self, request, format=None):
        """
        Add data to PostgreSQL database.

        Parameters:
        request (Request): The request containing the data.
        format (str): The format of the response.

        Returns:
        JsonResponse: The JSON response with the prediction result.
        """
        
        # Check if 'pslist_nproc' field is present in the request data
        if request.data.get('username') is not None:
            # Create a DataEntry instance and save it to the database
            data_entry = DataEntry.objects.create(
                name=request.data.get('username'),
                pslist_nproc=request.data.get('pslist_nproc'),
                pslist_avg_threads=request.data.get('pslist_avg_threads'),
                pslist_nprocs64bit=request.data.get('pslist_nprocs64bit'),
                handles_nfile=request.data.get('handles_nfile'),
                handles_nthread=request.data.get('handles_nthread'),
                ldrmodules_not_in_load=request.data.get('ldrmodules_not_in_load'),
                ldrmodules_not_in_init=request.data.get('ldrmodules_not_in_init'),
                ldrmodules_not_in_mem=request.data.get('ldrmodules_not_in_mem'),
                ldrmodules_not_in_load_avg=request.data.get('ldrmodules_not_in_load_avg'),
                ldrmodules_not_in_init_avg=request.data.get('ldrmodules_not_in_init_avg'),
                ldrmodules_not_in_mem_avg=request.data.get('ldrmodules_not_in_mem_avg'),
                malfind_ninjections=request.data.get('malfind_ninjections'),
                malfind_commitCharge=request.data.get('malfind_commitCharge'),
                malfind_protection=request.data.get('malfind_protection'),
                malfind_uniqueInjections=request.data.get('malfind_uniqueInjections'),
                svcscan_nservices=request.data.get('svcscan_nservices'),
                svcscan_kernel_drivers=request.data.get('svcscan_kernel_drivers'),
                svcscan_fs_drivers=request.data.get('svcscan_fs_drivers'),
                svcscan_process_services=request.data.get('svcscan_process_services'),
                svcscan_shared_process_services=request.data.get('svcscan_shared_process_services'),
                svcscan_interactive_process_services=request.data.get('svcscan_interactive_process_services'),
                svcscan_nactive=request.data.get('svcscan_nactive'),
                callbacks_ncallbacks=request.data.get('callbacks_ncallbacks'),
                callbacks_nanonymous=request.data.get('callbacks_nanonymous'),
                callbacks_ngeneric=request.data.get('callbacks_ngeneric'),
                Memory_PssTotal=request.data.get('Memory_PssTotal'),
                MemoryPssClean=request.data.get('MemoryPssClean'),
                MemorySharedDirty=request.data.get('MemorySharedDirty'),
                MemoryPrivateDirty=request.data.get('MemoryPrivateDirty'),
                MemorySharedClean=request.data.get('MemorySharedClean'),
                MemoryPrivateClean=request.data.get('MemoryPrivateClean'),
                MemoryHeapSize=request.data.get('MemoryHeapSize'),
                MemoryHeapAlloc=request.data.get('MemoryHeapAlloc'),
                MemoryHeapFree=request.data.get('MemoryHeapFree'),
                MemoryParcelMemory=request.data.get('MemoryParcelMemory'),
                totalReceivedBytes=request.data.get('totalReceivedBytes'),
                totalReceivedPackets=request.data.get('totalReceivedPackets'),
                totalTransmittedBytes=request.data.get('totalTransmittedBytes'),
                totalTransmittedPackets=request.data.get('totalTransmittedPackets'),
                APICall=request.data.get('APICall'),
                Permission=request.data.get('Permission'),
                URL=request.data.get('URL'),
                Provider=request.data.get('Provider'),
                Feature=request.data.get('Feature'),
                Intent=request.data.get('Intent'),
                Activity=str([i.lstrip("ActivityInfo{") .rstrip("}") for i in request.data.get('Activity').split("\n")])[1:-1],
                Call=request.data.get('Call'),
                ServiceReceiver=request.data.get('ServiceReceiver'),
                RealPermission=request.data.get('RealPermission'),
                Time=datetime.now() 
            )

            # Extract relevant features from the request data
            extracted_data = self.extract_features(request.data)

            # Use the extracted features to predict malware
            predictor = MalwareDetection()  # Assuming you have defined this class
            result = predictor.predict(extracted_data)
            
            # Return the prediction as JSON response
            return JsonResponse(result, status=201)
        else:
            return Response({"message": "Invalid data provided"}, status=400)

    @staticmethod
    def extract_features(data):
        """
        Extract relevant features from the request data.

        Parameters:
        data (dict): The request data.

        Returns:
        numpy.ndarray: The extracted features.
        """
        a = dict()

        a["APICall"] = data.get('APICall')
        a["URL"] = data.get('URL')
        a["Intent"] = data.get('Intent')
        a["Call"] = data.get('Call')
        a["RealPermission"] = data.get('RealPermission')
        a["Permission"] = data.get('Permission')
        a["Activity"] = data.get('Activity')
        a["Provider"] = data.get('Provider')
        a["Feature"] = data.get('Feature')
        a["ServiceReceiver"] = data.get('ServiceReceiver')

        ext_data = pd.DataFrame(a, index=[0])
        extracted_data = []

        for i in range(10):
            test_data = ext_data.values[:, i]
                
            # Load TF-IDF model
            with open(f"static/tfidf_col{i}.pkl", "rb") as file:
                tfidf = pickle.load(file)
                test_tfidf = tfidf.transform(test_data).todense()
            
            # Concatenate TF-IDF vectors
            if len(extracted_data) == 0:
                extracted_data = test_tfidf
            elif isinstance(extracted_data, np.ndarray):
                extracted_data = np.concatenate((extracted_data, test_tfidf), axis=1)
            else:
                extracted_data = np.array(extracted_data)
                extracted_data = np.concatenate((extracted_data, test_tfidf), axis=1)

        return extracted_data


class MalwareDetection:
    def __init__(self):
        """
        Initialize the MalwareDetection class.

        This method loads a pre-trained random forest classifier during object initialization.
        The classifier is stored in a file named 'rf.pkl' located in the 'static' directory.

        Attributes:
            clf (RandomForestClassifier): The loaded random forest classifier.
        """
        # Load pre-trained random forest classifier from 'rf.pkl'
        # The file is located in the 'static' directory
        with open("static/rf.pkl", "rb") as f:
            # Load the classifier
            self.clf = pickle.load(f)

    def predict(self, normalized_features):
        """
        Predicts whether input data is Malware or Goodware.

        Args:
            normalized_features (list): List of normalized features extracted from input data.

        Returns:
            dict: Dictionary with keys "Label" and "Confidence level".
        """
        y_pred = self.clf.predict(np.asarray(normalized_features))
        pred_proba = self.clf.predict_proba(np.asarray(normalized_features))
        
        pred_proba_percent1 = np.around(pred_proba[0] * 100, decimals=2)
        result = {}
        for label, conf in zip(y_pred, pred_proba_percent1):
            if label == 0:
                result = {"Label": "Goodware", "Confidence level": int(conf)}
            else:
                result = {"Label": "Malware", "Confidence level": int(conf)}
        return result

class OTPVerification(APIView):
    """
    View to verify a one-time password (OTP) sent by the user.
    """

    serializer_class = OTPSerializer

    def post(self, request):
        """
        Handle the POST request.

        Args:
            request (Request): The HTTP request object.

        Returns:
            Response: The HTTP response object.
        """
        serializer = self.serializer_class(data=request.data)

        if serializer.is_valid():
            email = serializer.validated_data.get('email')
            otp = serializer.validated_data.get('otp')

            try:
                user = User.objects.get(email=email)
            except User.DoesNotExist:
                return Response({"msg": "User does not exist"}, status=status.HTTP_400_BAD_REQUEST)

            if user.otp != otp:
                return Response({"msg": "Wrong OTP"}, status=status.HTTP_400_BAD_REQUEST)

            user.is_verified = True
            user.save()
            return Response({'msg': 'Account Verified'}, status=status.HTTP_200_OK)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class TokenView(APIView):
    """
    A View for User Login
    """
    renderer_classes = [UserRenderer]
    
    def post(self, request, format=None):
        """
        Login a user

        Parameters:
        request (Request): The request containing the user's email and password.
        format (str): The format of the response.

        Returns:
        Response: The response containing the tokens and a success message if the login is successful, or an error message if the login is not valid.
        """
        # Get the serializer data
        serializer = UserLoginSerializer(data=request.data)
        
        # Validate the serializer data
        serializer.is_valid(raise_exception=True)
        
        # Get the email and password from the serializer data
        email = serializer.data.get('email')
        password = serializer.data.get('password')
        
        # Authenticate the user
        user = authenticate(email=email, password=password)
        
        # If the user exists and is authenticated, check if their account is verified
        if user is not None:
            if not user.is_verified:
                return Response({'msg': 'Account is not verified'}, status=status.HTTP_403_FORBIDDEN)
            # Generate tokens for the verified user and return them and a success message
            token = get_tokens_for_user(user)
            return Response({'token':token}, status=status.HTTP_200_OK)
        
        # If the user does not exist or is not authenticated, return an error message
        else:
            return Response({'errors':{'non_field_errors':['Email or Password is not Valid']}}, status=status.HTTP_404_NOT_FOUND)


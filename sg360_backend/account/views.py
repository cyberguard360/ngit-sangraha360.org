from rest_framework.response import Response
from rest_framework import status
from rest_framework.views import APIView
from account.serializers import  UserLoginSerializer, UserRegistrationSerializer, OTPSerializer
from django.contrib.auth import authenticate
from account.renderers import UserRenderer
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework.permissions import IsAuthenticated
from .models import *
from django.http import HttpResponse
from django.views import View
from django.conf import settings

import pandas as pd
import numpy as np
import pickle
from django.http import JsonResponse
from .email import *
from datetime import datetime
import random
from django.core.validators import validate_email
from django.core.exceptions import ValidationError
from django.shortcuts import render, get_object_or_404
from .signals import *
import re

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
        otp_confirm = ''.join([str(random.randint(0, 9)) for i in range(4)])

        data = request.data

        data["otp"] = otp_confirm

        serializer = UserRegistrationSerializer(data=data)

        if serializer.is_valid():
            
            with open('static/otp.html', 'r') as html_file:
                html_content = html_file.read()
            html_content = html_content.replace('{name}', data["username"])

            # Send the OTP to the user's email
            send_otp_via_email(data["email"], otp_confirm , html_content)

            # Save the user
            serializer.save()

            return Response({'msg':'OTP sent to your email'}, status=status.HTTP_201_CREATED)
        
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class UserLoginView(APIView):
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
        
        # If the user exists and is authenticated, generate tokens for the user and return them and a success message
        if user is not None:
            if not user.is_verified:
                return Response({'msg': 'Account is not verified'}, status=status.HTTP_403_FORBIDDEN)
            # Generate tokens for the verified user and return them and a success message
            token = get_tokens_for_user(user)

            # Manually trigger the user_logged_in signal
            user_logged_in.send(sender=user.__class__, user=user, request=request)
            return Response(token, status=status.HTTP_200_OK)
        
        # If the user does not exist or is not authenticated, return an error message
        else:
            return Response({'errors':{'non_field_errors':['Email or Password is not Valid']}}, status=status.HTTP_404_NOT_FOUND)

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
                user = get_object_or_404(User,email=email) 
            except User.DoesNotExist:
                return Response({"msg": "User does not exist"}, status=status.HTTP_400_BAD_REQUEST)

            if user.otp != otp:
                return Response({"msg": "Wrong OTP"}, status=status.HTTP_400_BAD_REQUEST)

            user.is_verified = True
            user.save()

            user_verified.send(sender=User, instance=user, request=request)
            return Response({'msg': 'Account Verified'}, status=status.HTTP_200_OK)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class ApplicationData(APIView):
    """
    Create a new application entry.
    """

    def post(self, request):
        """
        Handle the POST request.

        Args:
            request (Request): The HTTP request object.

        Returns:
            Response: The HTTP response object.
        """
        return_list = list()

        app_list = request.data['apps']
        
        return_list.append(
            {
                "packageName" : "Null",
                "prediction" : "Null"
            }
        )

        for app in app_list:

            if not re.match("^[a-zA-Z0-9_.]*$", app["packageName"]) is not None:
                return Response(
                    {"error": "Invalid package name or source name"},
                    status=status.HTTP_400_BAD_REQUEST
                )
                
            db_entry = AppData.objects.filter(package_name=app["packageName"], source=app["source"]).first()

            if db_entry:

                if (db_entry.model_predict == db_entry.manual_predict ):
                    return_list.append(
                        {
                            "packageName" : f"{app['packageName']}",
                            "prediction" : f"{db_entry.model_predict}"
                        }
                    )
                else:
                    return_list.append(
                        {
                            "packageName" : f"{app['packageName']}",
                            "prediction" : "Ambiguous"
                        }
                    )
            else:
                AppEntry = AppData.objects.create(
                package_name = app['packageName'],
                source = app['source'],
                timestamp = str(datetime.now().date().strftime("%Y-%B-%d")),
                model_predict = random.choice(["Malware", "Benign"]),
                manual_predict = random.choice(["Malware", "Benign"])
            )

        # print({"msg":return_list})

        return Response({"msg": return_list}, status=status.HTTP_200_OK)

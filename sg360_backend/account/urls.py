"""
URL configuration for the account app.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.urls import path
from account.views import UserLoginView, UserRegistrationView, AddDataToPostGresView,OTPVerification, TokenView

from rest_framework_simplejwt.views import ( TokenRefreshView )

urlpatterns = [
    # User registration
    path('register/', UserRegistrationView.as_view(), name='register'),

    # User login
    path('login/', UserLoginView.as_view(), name='login'),

    # Add data to Postgres
    path('project-data/', AddDataToPostGresView.as_view(), name='add_data'),

    # OTP verification
    path('otpverify/', OTPVerification.as_view(), name='verify'),

    # Token refresh
    path('refresh/', TokenRefreshView.as_view(), name='refresh'),

    # Tokens
    path('tokens/', TokenView.as_view(), name='token'),
]
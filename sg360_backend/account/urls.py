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
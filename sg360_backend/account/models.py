from django.db import models
from django.contrib.auth.models import AbstractUser
from django.contrib.auth.models import User
from .manager import UserManager

#  Custom User Model
class User(AbstractUser):
    """
    Custom user model for account app.

    Extends default User model with additional fields.

    Attributes:
        username (TextField): Username of the user.
        email (EmailField): Email of the user.
        is_verified (BooleanField): Indicates if the user is verified.
        otp (CharField): One-time password of the user.
        last_login_time (DateTimeField): Time when the user last logged in.
        last_logout_time (DateTimeField): Time when the user last logged out.

    """
    username = models.TextField(blank=True) # Username of the user.
    email = models.EmailField(unique=True) # Email of the user.
    is_verified = models.BooleanField(default=False) # Indicates if the user is verified.
    otp = models.CharField(max_length=6, null=True, blank=True) # One-time password of the user.
    last_login_time = models.DateTimeField(null=True, blank=True) # Time when the user last logged in.
    last_logout_time = models.DateTimeField(null=True, blank=True) # Time when the user last logged out.

    USERNAME_FIELD = 'email' # Field to be used as unique identifier.
    REQUIRED_FIELDS = [] # Fields that are required to create a user.
    
    objects = UserManager() # Custom manager for the user model.

    def __str__(self):
        """
        Returns the email of the user.

        Returns:
            str: Email of the user.
        """
        return self.email

class AppData(models.Model):
    package_name = models.CharField(max_length=250)
    source = models.TextField(blank=True)
    timestamp = models.TextField(blank=True)
    model_predict = models.CharField(max_length=100, blank=True)
    manual_predict = models.CharField(max_length=100, blank=True)

    def __str__(self):
        return f"{self.package_name} - {self.source} - {self.timestamp} - {self.model_predict} - {self.manual_predict}"

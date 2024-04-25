from django.db import models
from django.contrib.auth.models import AbstractUser

from django.contrib.auth.models import User
from django.db.models.signals import post_save
from django.dispatch import receiver
from .manager import UserManager
from django.contrib.auth.signals import user_logged_in, user_logged_out
import datetime


# Create your models here.


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
    
    def name(self):
        """
        Returns the username of the user.

        Returns:
            str: Username of the user.
        """
        return self.username

    def __str__(self):
        """
        Returns the email of the user.

        Returns:
            str: Email of the user.
        """
        return self.email



'''  ALL SIGNALS HERE  '''



@receiver(user_logged_in) 
def _user_logged_in(sender, request, user, **kwargs):
    """
    Logs the time of user login.

    This function logs the time of user login. It updates the 'last_login_time' field of the user model
    with the current time and saves the user instance. However, this function needs to be further
    improved to handle cases like errors during database operations and to work properly with
    different authentication backends.

    Args:
        sender (Any): The sender of the signal.
        request (HttpRequest): The request object.
        user (User): The user instance.
        **kwargs: Additional keyword arguments.
    """
    try:
        user.last_login_time = timezone.now()
        user.save()
    except Exception as e: 
        print(e) # TODO: This needs to be worked on further to handle errors properly.


@receiver(user_logged_out) 
def _user_logged_out(sender, request, user, **kwargs):
    """
    Logs the time of user logout.

    This function logs the time of user logout. It updates the 'last_logout_time' field of the user model
    with the current time and saves the user instance. However, this function needs to be further
    improved to handle cases like errors during database operations and to work properly with
    different authentication backends.

    Args:
        sender (Any): The sender of the signal.
        request (HttpRequest): The request object.
        user (User): The user instance.
        **kwargs: Additional keyword arguments.
    """
    try:
        user.last_logout_time = timezone.now()
        user.save()
    except Exception as e: 
        # TODO: This needs to be worked on further to handle errors properly.
        print(e)


from django.db import models

COLUMN_FIELDS_FLOAT = [
    'pslist_nproc', 'pslist_avg_threads', 'pslist_nprocs64bit', 
    'handles_nfile', 'handles_nthread', 'ldrmodules_not_in_load',
    'ldrmodules_not_in_init', 'ldrmodules_not_in_mem',
    'ldrmodules_not_in_load_avg', 'ldrmodules_not_in_init_avg',
    'ldrmodules_not_in_mem_avg', 'malfind_ninjections',
    'malfind_commitCharge', 'malfind_protection',
    'malfind_uniqueInjections', 'svcscan_nservices',
    'svcscan_kernel_drivers', 'svcscan_fs_drivers',
    'svcscan_process_services', 'svcscan_shared_process_services',
    'svcscan_interactive_process_services', 'svcscan_nactive',
    'callbacks_ncallbacks', 'callbacks_nanonymous',
    'callbacks_ngeneric', 'Memory_PssTotal', 'MemoryPssClean',
    'MemorySharedDirty', 'MemoryPrivateDirty', 'MemorySharedClean',
    'MemoryPrivateClean', 'MemoryHeapSize', 'MemoryHeapAlloc',
    'MemoryHeapFree', 'MemoryParcelMemory', 'totalReceivedBytes',
    'totalReceivedPackets', 'totalTransmittedBytes',
    'totalTransmittedPackets'
]

class DataEntry(models.Model):
    """
    Model to store data entries for the API.
    Fields:
        name (CharField): name of the data entry
        APICall (TextField): API call of the data entry
        Permission (TextField): Permission of the data entry
        URL (TextField): URL of the data entry
        Provider (TextField): Provider of the data entry
        Feature (TextField): Feature of the data entry
        Intent (TextField): Intent of the data entry
        Activity (TextField): Activity of the data entry
        Call (TextField): Call of the data entry
        ServiceReceiver (TextField): ServiceReceiver of the data entry
        RealPermission (TextField): RealPermission of the data entry
        model_predict (CharField): model prediction of the data entry
        manual_predict (CharField): manual prediction of the data entry
    """

    name = models.CharField(max_length=100)

    # Create float fields dynamically
    for field in COLUMN_FIELDS_FLOAT:
        locals()[field] = models.FloatField(
            blank=True, null=True)  # dynamically create float fields

    APICall = models.TextField(blank=True)
    Permission = models.TextField(blank=True)
    URL = models.TextField(blank=True)
    Provider = models.TextField(blank=True)
    Feature = models.TextField(blank=True)
    Intent = models.TextField(blank=True)
    Activity = models.TextField(blank=True)
    Call = models.TextField(blank=True)
    ServiceReceiver = models.TextField(blank=True)
    RealPermission = models.TextField(blank=True)
    Time = models.DateTimeField(null=True, blank=True)

    model_predict = models.CharField(max_length=100, blank=True)
    manual_predict = models.CharField(max_length=100, blank=True)

    def __str__(self):
        """
        String representation of a DataEntry
        Returns:
            str: Data Entry - ID: {self.id}
        """
        return 'Data Entry - ID: {}'.format(self.id)
        return f'Data Entry - ID: {self.id}'


from django.apps import AppConfig


class AccountConfig(AppConfig):
    """
    This class represents the configuration for the account application.
    It provides the necessary metadata for the Django application framework.
    """
    # Specify the default auto field to be used by models in this app
    default_auto_field = 'django.db.models.BigAutoField'
    # Specify the name of the application
    name = 'account'



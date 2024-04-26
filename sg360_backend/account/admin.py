from django.contrib import admin

# Import the User model from the account app
from account.models import User

# Register the User model with the admin site, so it is available in the Django admin interface
admin.site.register(User)


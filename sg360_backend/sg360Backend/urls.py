"""
URL configuration for sg360Backend project.

"""
from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    # The URL for the Django Admin site
    path('admin/', admin.site.urls),
    
    # Include the URLs of the 'account' app
    # This path will be used as a prefix for the URLs defined in 'account.urls'
    path('api/user/', include('account.urls'))
]


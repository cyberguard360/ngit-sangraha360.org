"""
WSGI config for sg360Backend project.

It exposes the WSGI callable as a module-level variable named ``application``.

"""

import os

from django.core.wsgi import get_wsgi_application

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'sg360Backend.settings')

application = get_wsgi_application()

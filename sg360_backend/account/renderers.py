from rest_framework import renderers
import json

class UserRenderer(renderers.JSONRenderer):
    """
    Renderer for User objects.

    This renderer is used to format User objects into JSON 
    compatible with Django REST Framework.
    """
    charset = 'utf-8'

    def render(self, data, accepted_media_type=None, renderer_context=None):
        """
        Render the data into JSON.

        If the data contains 'ErrorDetail' it renders it as a JSON
        error object. Otherwise, it renders the data as a regular JSON.

        Args:
            data (dict or list): Data to be serialized.
            accepted_media_type (str): Not used in this renderer.
            renderer_context (dict): Context for the renderer.

        Returns:
            str: JSON serialized data.
        """
        response = ''

        if 'ErrorDetail' in str(data):
            # Render as JSON error object
            response = json.dumps({'errors': data})
        else:
            # Render regular JSON
            response = json.dumps(data)

        return response


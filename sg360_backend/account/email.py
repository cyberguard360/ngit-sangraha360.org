from django.core.mail import send_mail
from django.conf import settings
from random import randint
from .models import User

def send_otp_via_email(email, otp, html_content):
    """
    Sends an OTP to the provided email address using the given HTML content.

    Args:
        email (str): The email address to send the OTP to.
        otp (str): The OTP (One Time Password) to send.
        html_content (str): The HTML content template for the email.

    Returns:
        None
    """
    # Define email subject and message
    subject = "Your Account Verification Mail"
    message = ""

    # Replace {otp} in html_content with the actual OTP
    html_content = html_content.replace('{otp}', otp)

    # Get the email address from Django settings
    email_from = settings.EMAIL_HOST_USER

    # Send the email
    send_mail(subject=subject, message=message, from_email=email_from, 
              recipient_list=[email], html_message=html_content)

    # Update the OTP for the user
    user_object = User.objects.get(email=email)
    user_object.otp = otp
    user_object.save()


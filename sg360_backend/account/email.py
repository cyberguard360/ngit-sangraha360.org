from django.core.mail import EmailMessage
from django.conf import settings
from .models import User


def send_otp_via_email(email_address, otp, html_content):
    """
    Sends an OTP to the provided email address using the given HTML content.

    Args:
        email_address (str): The email address to send the OTP to.
        otp (str): The OTP (One Time Password) to send.
        html_content (str): The HTML content template for the email.

    Returns:
        None
    """
    # Define email subject and message
    subject = "Your Account Verification Mail"

    # Send the email
    try:
        email = EmailMessage(
            subject=subject,
            body=html_content.replace('{otp}', otp),
            from_email=settings.EMAIL_HOST_USER,
            to=[email_address],
            reply_to=[email_address],
            headers={'Content-Type': 'text/plain'},
        )
        email.content_subtype = "html"  # Set the content type to HTML
        email.send()

    except:
        print("something went wrong")

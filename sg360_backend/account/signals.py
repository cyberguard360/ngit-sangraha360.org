from django.db.models.signals import pre_save, post_save
from django.dispatch import receiver
from .models import User
from django.contrib.auth.signals import user_logged_in, user_logged_out
from django.utils import timezone

from django.dispatch import Signal


@receiver(user_logged_in)
def user_logged_in_handler(sender, user, request, **kwargs):
    # Your logic here
    try:
        user.last_login_time = timezone.now()
        user.save()
    except Exception as e: 
        # TODO: This needs to be worked on further to handle errors properly.
        print(e)


user_verified = Signal()

@receiver(user_verified)
def handle_user_verification(sender, instance, request, **kwargs):
    if instance.is_verified:
        instance.otp = None
        instance.save()

@receiver(post_save, sender=User)
def post_save_user(sender, instance, created, **kwargs):
    if created:
        # print("User Created {}".format(instance.username))
        # working on it
        pass


model_fetch = Signal()


@receiver(model_fetch)
def handle_model_fetch(sender, instance, **kwargs):
    return "First" 

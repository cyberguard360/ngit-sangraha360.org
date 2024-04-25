from django.contrib.auth.base_user import BaseUserManager

class UserManager(BaseUserManager):
    """
    Custom User Manager for the User Model.
    """

    use_in_migrations = True

    def _create_user(self, email, password, **extra_fields):
        """
        Creates and saves a User with the given email and password.

        Args:
            email (str): The email of the user.
            password (str): The password of the user.
            **extra_fields (dict): Additional fields for the user.

        Returns:
            User: The created user.

        Raises:
            ValueError: If the email is not set.
        """
        if not email:
            raise ValueError('The given email must be set')
        email = self.normalize_email(email)
        user = self.model(email=email, **extra_fields)
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_user(self, email, password=None, **extra_fields):
        """
        Creates and saves a User with the given email and password.

        Args:
            email (str): The email of the user.
            password (str, optional): The password of the user. Defaults to None.
            **extra_fields (dict): Additional fields for the user.

        Returns:
            User: The created user.

        Raises:
            ValueError: If the email is not set.
        """
        if not email:
            raise ValueError(_('The Email must be set'))
        email = self.normalize_email(email)
        user = self.model(email=email, **extra_fields)
        user.set_password(password)
        user.save()
        return user

    def create_superuser(self, email, password, **extra_fields):
        """
        Creates and saves a superuser with the given email and password.

        Args:
            email (str): The email of the superuser.
            password (str): The password of the superuser.
            **extra_fields (dict): Additional fields for the superuser.

        Returns:
            User: The created superuser.

        Raises:
            ValueError: If the superuser is not staff or superuser.
        """
        extra_fields.setdefault('is_staff', True)
        extra_fields.setdefault('is_superuser', True)
        extra_fields.setdefault('is_active', True)

        if extra_fields.get('is_staff') is not True:
            raise ValueError(_('Superuser must have is_staff=True.'))
        if extra_fields.get('is_superuser') is not True:
            raise ValueError(_('Superuser must have is_superuser=True.'))
        return self.create_user(email, password, **extra_fields)

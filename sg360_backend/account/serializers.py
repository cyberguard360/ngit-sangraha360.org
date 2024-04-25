from rest_framework import serializers
from account.models import User
from django.utils.encoding import smart_str, force_bytes, DjangoUnicodeDecodeError
from django.utils.http import urlsafe_base64_decode, urlsafe_base64_encode

class UserRegistrationSerializer(serializers.ModelSerializer):
    """
    Serializer for user registration. It includes an additional field for password confirmation
    and performs validation to ensure that passwords match.
    """

    class Meta:
        model = User
        fields = ['email', 'username', 'password', 'is_verified', 'otp']
        extra_kwargs = {'password': {'write_only': True}}


    def create(self, validated_data):
        """
        Create and return a new user instance, given the validated data.
        """
        user = User.objects.create_user(**validated_data)
        return user
        

class UserLoginSerializer(serializers.ModelSerializer):
    """
    Serializer for user login. It expects the user's email and password.
    """

    # User's email
    email = serializers.EmailField(max_length=255)

    class Meta:
        """
        Meta class for UserLoginSerializer.

        Attributes:
            model (User): The model to be used by the serializer.
            fields (list): The fields to be included in the serialized output.
        """
        model = User
        fields = ['email', 'password']

class OTPSerializer(serializers.Serializer):
    """
    Serializer for validating and handling OTPs.
    """
    # The OTP for verification.
    otp = serializers.CharField(
        max_length=6,
        help_text="The OTP for verification."
        )
    # The email of the user.
    email = serializers.EmailField(
        help_text="The email of the user."
        )

    def validate(self, attrs):
        """
        Validates the OTP and the email of the user. 

        Args:
            attrs (dict): The validated data.

        Raises:
            serializers.ValidationError: If OTP is not a number or email is not valid.

        Returns:
            dict: The validated data.
        """
        otp = attrs.get('otp')
        email = attrs.get('email')

        # Check if the user with given email exists.
        try:
            user = User.objects.get(email=email)
        except User.DoesNotExist:
            raise serializers.ValidationError("Invalid email")

        # Check if the OTP is a valid number.
        if not str(otp).isdigit():
            raise serializers.ValidationError("OTP must be a number")

        return attrs

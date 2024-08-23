from django.urls import path
from federated.views import clusterClient, communicationRound


urlpatterns = [
    # Client Data
    path('clientData/', clusterClient.as_view(), name='clustering_clients'),

    # Communication Round
    path('communicationRound/', communicationRound.as_view(), name='communication_round'),
]


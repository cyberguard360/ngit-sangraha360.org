from django.db import models

# Create your models here.

class Cluster(models.Model):
    name = models.CharField(max_length=100)
    numberString = models.TextField(blank=True)
    bestWeights = models.TextField(blank=True)
    counter = models.FloatField(default=1)

    def __str__(self):
        return f"{self.name} - {self.numberString}"

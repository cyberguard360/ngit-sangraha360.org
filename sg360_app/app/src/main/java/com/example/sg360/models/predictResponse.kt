package com.example.sg360.models

import com.google.gson.annotations.SerializedName

// This class represents the response from the predict API
data class PredictResponse(
    // The label predicted by the API
    @SerializedName("Label") val label: String,
    // The confidence level of the prediction, from 0 to 100
    @SerializedName("Confidence level") val confidenceLevel: Int
)

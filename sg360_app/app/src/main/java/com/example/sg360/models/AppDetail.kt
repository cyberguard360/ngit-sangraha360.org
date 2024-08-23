package com.example.sg360.models

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap

data class AppDetail(
    val appName: String,
    val packageName: String,
    val icon: ImageBitmap
)
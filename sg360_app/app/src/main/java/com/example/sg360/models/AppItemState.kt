package com.example.sg360.models

import androidx.compose.ui.graphics.ImageBitmap

data class AppItemState(
    val appName: String,
    val packageName: String,
    val icon: ImageBitmap,
    var isLoading: Boolean = false,
    var isDone: Boolean = false,
    var result: String? = null
)
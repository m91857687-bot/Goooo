package com.example.ui.screens

import kotlinx.serialization.Serializable

@Serializable
object Splash

@Serializable
object Home

@Serializable
data class ImageViewer(val initialMediaId: Long)

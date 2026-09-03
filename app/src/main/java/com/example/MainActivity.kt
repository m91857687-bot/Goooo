package com.example

import android.Manifest
import android.content.pm.PackageManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.Home
import com.example.ui.screens.ImageViewer
import com.example.ui.screens.Splash
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.HomeScreen // Will create
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            val app = application as GalleryApplication
            lifecycleScope.launch {
                app.mediaRepository.syncMedia()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GalleryApp(
                        onRequirePermissions = { checkAndRequestPermissions() }
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(neededPermissions.toTypedArray())
        } else {
            val app = application as GalleryApplication
            val viewModel = MainViewModel(app.mediaRepository)
            viewModel.sync()
        }
    }
}

@Composable
fun GalleryApp(onRequirePermissions: () -> Unit) {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = Splash) {
        composable<Splash> {
            SplashScreen(
                onNavigateToHome = {
                    onRequirePermissions()
                    navController.navigate(Home) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }
        composable<Home> {
            // Need context to get Application
            val context = androidx.compose.ui.platform.LocalContext.current
            val app = context.applicationContext as GalleryApplication
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(app.mediaRepository)
            )
            HomeScreen(
                viewModel = viewModel,
                onMediaClick = { mediaId ->
                    navController.navigate(ImageViewer(initialMediaId = mediaId))
                }
            )
        }
        composable<ImageViewer> {
            // Placeholder for now
        }
    }
}

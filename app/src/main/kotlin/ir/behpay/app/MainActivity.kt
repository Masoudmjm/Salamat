package ir.behpay.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import ir.behpay.App
import ir.behpay.core.notification.AndroidNotificationPermissionRequester
import ir.behpay.di.initKoinAndroid

class MainActivity : ComponentActivity() {

    private var targetRoute by mutableStateOf<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission state is observed via LifecycleEventEffect in AppScaffold / SettingsScreen
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            initKoinAndroid(this)
        } catch (_: Exception) {
            // Already initialized if process survived
        }

        targetRoute = intent?.getStringExtra("route")

        AndroidNotificationPermissionRequester.register(this) {
            requestNotificationPermissionPrompt()
        }

        // Prompt user on startup on Android 13+ (Tiramisu / API 33+) if not yet granted
        requestNotificationPermissionPrompt()

        setContent {
            App(initialRoute = targetRoute)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        targetRoute = intent.getStringExtra("route")
    }

    private fun requestNotificationPermissionPrompt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroy() {
        AndroidNotificationPermissionRequester.unregister()
        super.onDestroy()
    }
}

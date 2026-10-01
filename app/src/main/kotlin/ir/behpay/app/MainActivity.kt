package ir.behpay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ir.behpay.App
import ir.behpay.di.initKoinAndroid

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            initKoinAndroid(this)
        } catch (_: Exception) {
            // Already initialized if process survived
        }

        setContent {
            App()
        }
    }
}

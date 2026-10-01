package ir.behpay

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ir.behpay.core.ui.theme.BehpayTheme
import ir.behpay.ui.AppScaffold
import ir.behpay.ui.AppViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val viewModel: AppViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()

    BehpayTheme(isRtl = state.isPersian) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppScaffold(viewModel = viewModel)
        }
    }
}

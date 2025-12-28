package com.hancekim.billboard

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.designsystem.componenet.dialog.BillboardAlert
import com.hancekim.feature.splash.SplashUi
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )
        setContent {
            val splashState by viewModel.splashState.collectAsStateWithLifecycle()

            BillboardTheme {
                val colorScheme = BillboardTheme.colorScheme
                if (splashState == SplashState.Success) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .background(colorScheme.bgApp)
                        )
                    }
                } else {
                    SplashUi(modifier = Modifier.fillMaxSize())
                    if (splashState == SplashState.NetworkError) {
                        BillboardAlert(
                            onClick = { forceExit() },
                            title = "네트워크 확인",
                            body = "네트워크 연결을 확인해주세요",
                            buttonLabel = "확인",
                            onDismissRequest = { forceExit() }
                        )
                    }
                }
            }
        }
    }

    private fun forceExit() {
        finishAffinity()
        exitProcess(0)
    }
}

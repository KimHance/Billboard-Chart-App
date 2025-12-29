package com.hancekim.billboard

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hancekim.billboard.core.circuit.PopResult
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.feature.splash.SplashScreen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var circuit: Circuit

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
            BillboardTheme {
                val backStack = rememberSaveableBackStack(root = SplashScreen)
                val navigator = rememberCircuitNavigator(backStack) { result ->
                    when (result) {
                        is PopResult.QuitAppResult -> {
                            forceExit()
                        }
                    }
                }
                CircuitCompositionLocals(circuit) {
                    NavigableCircuitContent(navigator, backStack)
                }
            }
        }
    }

    private fun forceExit() {
        finishAffinity()
        exitProcess(0)
    }
}

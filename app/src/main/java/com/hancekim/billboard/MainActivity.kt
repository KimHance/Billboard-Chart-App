package com.hancekim.billboard

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.circuit.PopResult
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess
import androidx.compose.ui.graphics.Color as ComposeColor

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
                val backStack = rememberSaveableBackStack(root = BillboardScreen.Splash)
                val navigator = rememberCircuitNavigator(backStack) { result ->
                    when (result) {
                        is PopResult.QuitAppResult -> {
                            forceExit()
                        }
                    }
                }
                CircuitCompositionLocals(circuit) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics {
                                testTagsAsResourceId = true
                            },
                        color = BillboardTheme.colorScheme.bgApp,
                        contentColor = ComposeColor.Unspecified,
                    ) {
                        NavigableCircuitContent(
                            navigator = navigator,
                            backStack = backStack,
                        )
                    }
                }
            }
        }
    }

    private fun forceExit() {
        finishAndRemoveTask()
        exitProcess(0)
    }
}

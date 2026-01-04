package com.hancekim.billboard

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hancekim.billboard.core.circuit.BillboardScreen
import com.hancekim.billboard.core.circuit.PopResult
import com.hancekim.billboard.core.designsystem.BillboardTheme
import com.hancekim.billboard.core.domain.model.AppFont
import com.hancekim.billboard.core.domain.model.AppTheme
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

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            val appFont by viewModel.appFont.collectAsStateWithLifecycle()
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()

            LaunchedEffect(appTheme) {
                val systemBarStyle = when (appTheme) {
                    AppTheme.Dark -> SystemBarStyle.dark(
                        scrim = Color.TRANSPARENT
                    )
                    AppTheme.Light -> SystemBarStyle.light(
                        scrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT
                    )
                    AppTheme.System -> SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(
                    statusBarStyle = systemBarStyle,
                    navigationBarStyle = systemBarStyle,
                )
            }

            BillboardTheme(
                isDarkTheme = when (appTheme) {
                    AppTheme.Dark -> true
                    AppTheme.Light -> false
                    AppTheme.System -> isSystemInDarkTheme()
                },
                isSystemFont = appFont == AppFont.System
            ) {
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

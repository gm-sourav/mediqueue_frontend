package com.mediqueue.app

import com.mediqueue.app.ui.screens.auth.LanguageScreen
import com.mediqueue.app.ui.screens.auth.ProfileScreen
import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mediqueue.app.ui.screens.auth.LoginScreen
import com.mediqueue.app.ui.screens.auth.RegisterScreen
import com.mediqueue.app.ui.screens.auth.SplashScreen
import com.mediqueue.app.ui.theme.MediColors

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = MediColors.Teal,
                    onPrimary = MediColors.OnTeal,
                    secondary = MediColors.Teal,
                    background = MediColors.Bg,
                    surface = MediColors.Card,
                    onBackground = MediColors.Text,
                    onSurface = MediColors.Text
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MediColors.Bg
                ) {
                    // Temporary shorol navigation: "splash", "login", "register"
                    var screen by rememberSaveable { mutableStateOf("profile") }

                    BackHandler(enabled = screen != "splash" && screen != "login") {
                        screen = if (screen == "language") "profile" else "login"
                    }

                    when (screen) {
                        "splash" -> SplashScreen(onFinished = { screen = "login" })

                        "login" -> LoginScreen(
                            onLoginSuccess = {
                                Toast.makeText(this, "Login OK (demo)", Toast.LENGTH_SHORT).show()
                            },
                            onCreateAccount = { screen = "register" }
                        )

                        "register" -> RegisterScreen(
                            onRegistered = {
                                Toast.makeText(this, "Account created (demo)", Toast.LENGTH_SHORT).show()
                                screen = "login"
                            },
                            onSignInClick = { screen = "login" }
                        )

                        "language" -> LanguageScreen(onSaved = { screen = "profile" })

                        "profile" -> ProfileScreen(
                            name = "Nila Akter",
                            email = "nila@example.com",
                            role = "PATIENT",
                            onLanguageClick = { screen = "language" },
                            onLogout = { screen = "login" }
                        )
                    }
                }
            }
        }
    }
}
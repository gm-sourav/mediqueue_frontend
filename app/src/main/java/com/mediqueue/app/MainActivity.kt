package com.mediqueue.app

import com.mediqueue.app.ui.screens.patient.DoctorItem
import com.mediqueue.app.ui.screens.patient.DoctorsListScreen
import com.mediqueue.app.ui.screens.patient.Department
import com.mediqueue.app.ui.screens.patient.DepartmentsScreen
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
import com.mediqueue.app.ui.components.PatientTab
import com.mediqueue.app.ui.screens.patient.NextAppointment
import com.mediqueue.app.ui.screens.patient.PatientHomeScreen
import com.mediqueue.app.ui.screens.patient.QueueInfo
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
                    var screen by rememberSaveable { mutableStateOf("splash") }
                    var selectedDepartment by rememberSaveable { mutableStateOf("") }

                    BackHandler(enabled = screen !in listOf("splash", "login", "patient_home")) {
                        screen = when (screen) {
                            "language" -> "profile"
                            "profile", "departments" -> "patient_home"
                            "doctors" -> "departments"
                            else -> "login"
                        }
                    }

                    when (screen) {
                        "splash" -> SplashScreen(onFinished = { screen = "login" })

                        "login" -> LoginScreen(
                            onLoginSuccess = { screen = "patient_home" },
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

                        "patient_home" -> PatientHomeScreen(
                            patientName = "Nila Akter",
                            queue = QueueInfo(
                                myToken = 28,
                                nowServing = 21,
                                patientsAhead = 7,
                                estimatedMinutes = 25,
                                progress = 0.68f,
                                onTrack = true
                            ),
                            nextAppointment = NextAppointment(
                                doctorName = "Dr. Ayesha Rahman",
                                department = "Cardiology",
                                whenText = "Today, 10:30 AM",
                                token = 28,
                                status = "CONFIRMED"
                            ),
                            onBookClick = { screen = "departments" },
                            onAppointmentClick = {
                                Toast.makeText(this, "Appointment details next", Toast.LENGTH_SHORT).show()
                            },
                            onAvatarClick = { screen = "profile" },
                            onTabSelected = { tab ->
                                when (tab) {
                                    PatientTab.Profile -> screen = "profile"
                                    PatientTab.Doctors -> screen = "departments"
                                    else -> Toast.makeText(this, "$tab screen next", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        "departments" -> DepartmentsScreen(
                            departments = listOf(
                                Department(1, "Cardiology", 12, "♡"),
                                Department(2, "Neurology", 8, "✳"),
                                Department(3, "Dental", 10, "+"),
                                Department(4, "Eye", 6, "◉"),
                                Department(5, "Pediatrics", 9, "⌂"),
                                Department(6, "Orthopedics", 7, "+")
                            ),
                            onDepartmentClick = { dept ->
                                selectedDepartment = dept.name
                                screen = "doctors"
                            },
                            onTabSelected = { tab ->
                                when (tab) {
                                    PatientTab.Home -> screen = "patient_home"
                                    PatientTab.Profile -> screen = "profile"
                                    PatientTab.Doctors -> Unit
                                    else -> Toast.makeText(this, "$tab screen next", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        "doctors" -> DoctorsListScreen(
                            departmentName = selectedDepartment,
                            searchHint = "Search cardiologists",
                            doctors = listOf(
                                DoctorItem(1, "Dr. Ayesha Rahman", 12, "Cardiologist", 1000, "Today · 10:30 AM", true),
                                DoctorItem(2, "Dr. Samiul Haque", 8, "Cardiologist", 800, "Tomorrow · 9:00 AM", false),
                                DoctorItem(3, "Dr. Tasnim Noor", 6, "Cardiologist", 700, "Wed · 11:00 AM", false)
                            ),
                            onDoctorClick = { doctor ->
                                Toast.makeText(this, "${doctor.name} details next", Toast.LENGTH_SHORT).show()
                            },
                            onTabSelected = { tab ->
                                when (tab) {
                                    PatientTab.Home -> screen = "patient_home"
                                    PatientTab.Profile -> screen = "profile"
                                    PatientTab.Doctors -> screen = "departments"
                                    else -> Toast.makeText(this, "$tab screen next", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                    }
                }
            }
        }
    }
}
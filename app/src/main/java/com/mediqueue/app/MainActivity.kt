package com.mediqueue.app

import com.mediqueue.app.ui.screens.patient.PickSlotScreen
import com.mediqueue.app.ui.screens.patient.SlotItem
import com.mediqueue.app.ui.screens.patient.AppointmentItem
import com.mediqueue.app.ui.screens.patient.MyAppointmentsScreen
import com.mediqueue.app.ui.screens.patient.DoctorDetail
import com.mediqueue.app.ui.screens.patient.DoctorDetailScreen
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
import androidx.compose.runtime.remember
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

private val dummyDoctors = listOf(
    DoctorItem(1, "Dr. Ayesha Rahman", 12, "Cardiologist", 1000, "Today · 10:30 AM", true),
    DoctorItem(2, "Dr. Samiul Haque", 8, "Cardiologist", 800, "Tomorrow · 9:00 AM", false),
    DoctorItem(3, "Dr. Tasnim Noor", 6, "Cardiologist", 700, "Wed · 11:00 AM", false)
)

private val dummySlots = listOf(
    SlotItem(1, "9:00 AM", false), SlotItem(2, "9:30 AM", true), SlotItem(3, "10:00 AM", false),
    SlotItem(4, "10:30 AM", false), SlotItem(5, "11:00 AM", true), SlotItem(6, "11:30 AM", false),
    SlotItem(7, "12:00 PM", false), SlotItem(8, "12:30 PM", false), SlotItem(9, "1:00 PM", false)
)

private val dummyAppointments = listOf(
    AppointmentItem(1, "CONFIRMED", 28, "Dr. Ayesha Rahman", "Cardiology", "Today", "10:30 AM", "Nov 2", 1000),
    AppointmentItem(2, "PENDING", null, "Dr. Samiul Haque", "Neurology", "Nov 5", "9:00 AM", "Nov 5", 800),
    AppointmentItem(3, "COMPLETED", 12, "Dr. Rafiq Ahmed", "Orthopedics", "Oct 28", "11:00 AM", "Oct 28", 900),
    AppointmentItem(4, "CANCELLED", null, "Dr. Nusrat Jahan", "Medicine", "Oct 2", "4:00 PM", "Oct 2", 700)
)

private fun routeFor(tab: PatientTab): String = when (tab) {
    PatientTab.Home -> "patient_home"
    PatientTab.Doctors -> "departments"
    PatientTab.Bookings -> "my_appointments"
    PatientTab.Profile -> "profile"
}


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
                    var appointments by remember { mutableStateOf(dummyAppointments) }
                    var selectedDepartment by rememberSaveable { mutableStateOf("") }
                    var selectedDoctorName by rememberSaveable { mutableStateOf("") }

                    BackHandler(enabled = screen !in listOf("splash", "login", "patient_home")) {
                        screen = when (screen) {
                            "language" -> "profile"
                            "profile", "departments", "my_appointments" -> "patient_home"
                            "doctors" -> "departments"
                            "doctor_detail" -> "doctors"
                            "pick_slot" -> "doctor_detail"

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
                            onAppointmentClick = { screen = "my_appointments" },
                            onAvatarClick = { screen = "profile" },
                            onTabSelected = { screen = routeFor(it) }
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
                            onTabSelected = { screen = routeFor(it) }
                        )

                        "doctors" -> DoctorsListScreen(
                            departmentName = selectedDepartment,
                            searchHint = "Search cardiologists",
                            doctors = dummyDoctors,
                            onDoctorClick = { doctor ->
                                selectedDoctorName = doctor.name
                                screen = "doctor_detail"
                            },
                            onTabSelected = { screen = routeFor(it) }
                        )

                        "doctor_detail" -> {
                            val d = dummyDoctors.firstOrNull { it.name == selectedDoctorName } ?: dummyDoctors.first()
                            DoctorDetailScreen(
                                doctor = DoctorDetail(
                                    id = d.id,
                                    name = d.name,
                                    specialty = d.specialty,
                                    department = selectedDepartment,
                                    experienceYears = d.experienceYears,
                                    fee = d.fee,
                                    rating = 4.9,
                                    about = "Experienced cardiologist focused on clear, patient-centred care.",
                                    availableToday = d.availableToday,
                                    nextSlots = listOfNotNull(d.nextSlot)
                                ),
                                onBack = { screen = "doctors" },
                                onBookClick = { screen = "pick_slot" },
                                onSeeAllClick = { screen = "pick_slot" }
                            )
                        }
                        "pick_slot" -> PickSlotScreen(
                            doctorName = selectedDoctorName,
                            slotsForDate = { _ -> dummySlots },
                            onConfirm = { date, slot, reason ->
                                Toast.makeText(this, "Booked ${slot.time} on $date (demo)", Toast.LENGTH_LONG).show()
                                screen = "patient_home"
                            }
                        )

                        "my_appointments" -> MyAppointmentsScreen(
                            appointments = appointments,
                            onCancel = { item ->
                                appointments = appointments.map {
                                    if (it.id == item.id) it.copy(status = "CANCELLED") else it
                                }
                                Toast.makeText(this, getString(R.string.booking_cancelled), Toast.LENGTH_SHORT).show()
                            },
                            onDetailsClick = { /* porer screen e */ },
                            onTabSelected = { screen = routeFor(it) }
                        )

                    }
                }
            }
        }
    }
}
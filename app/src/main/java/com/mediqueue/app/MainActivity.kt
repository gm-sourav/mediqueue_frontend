package com.mediqueue.app
import com.mediqueue.app.ui.screens.patient.MedicineItem
import com.mediqueue.app.ui.screens.patient.PrescriptionItem
import com.mediqueue.app.ui.screens.patient.PrescriptionsScreen
import com.mediqueue.app.ui.screens.patient.LiveQueueInfo
import com.mediqueue.app.ui.screens.patient.LiveQueueScreen
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
import com.mediqueue.app.ui.screens.doctor.DoctorAppointment
import com.mediqueue.app.ui.screens.doctor.DoctorDashboardScreen
import com.mediqueue.app.ui.screens.patient.NextAppointment
import com.mediqueue.app.ui.screens.patient.PatientHomeScreen
import com.mediqueue.app.ui.screens.patient.PrescriptionsScreen
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

private val dummyPrescriptions = listOf(
    PrescriptionItem(
        1, "Nov 02", "Cardiology", "Dr. Ayesha Rahman", "Follow-up and medication plan", true,
        listOf(
            MedicineItem("Medicine A", "1 tablet", "After food"),
            MedicineItem("Medicine B", "1 tablet", "Night")
        ),
        "Take as directed by your doctor. Contact the clinic if you have questions."
    ),
    PrescriptionItem(
        2, "Oct 11", "General Medicine", "Dr. Farhan Kabir", "Prescription and visit notes", false,
        listOf(
            MedicineItem("Medicine C", "1 capsule", "Before food"),
            MedicineItem("Medicine D", "1 tablet", "After food"),
            MedicineItem("Medicine E", "5 ml", "Morning")
        ),
        null
    )
)

private val dummyDoctorAppointments = listOf(
    DoctorAppointment(1, "10:00", "Nila Akter", "Follow-up", "Cardiology", "PENDING", null),
    DoctorAppointment(2, "10:30", "Rafi Ahmed", "New consultation", "Cardiology", "CONFIRMED", 28),
    DoctorAppointment(3, "09:30", "Sabina Yasmin", "Check-up", "Cardiology", "COMPLETED", 27)
)


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
                    var liveQueue by remember {
                        mutableStateOf(LiveQueueInfo("Cardiology", "Dr. Ayesha Rahman", "10:30 AM", 28, 21, 7, 0.68f, 25))
                    }
                    var doctorAppointments by remember { mutableStateOf(dummyDoctorAppointments) }
                    var selectedDepartment by rememberSaveable { mutableStateOf("") }
                    var selectedDoctorName by rememberSaveable { mutableStateOf("") }

                    BackHandler(enabled = screen !in listOf("splash", "login", "patient_home", "doctor_dashboard")) {
                        screen = when (screen) {
                            "language" -> "profile"
                            "profile", "departments", "my_appointments" -> "patient_home"
                            "doctors" -> "departments"
                            "doctor_detail" -> "doctors"
                            "pick_slot" -> "doctor_detail"
                            "live_queue" -> "my_appointments"
                            "prescriptions" -> "my_appointments"

                            else -> "login"
                        }
                    }

                    when (screen) {
                        "splash" -> SplashScreen(onFinished = { screen = "login" })

                        "login" -> LoginScreen(
                            onLoginSuccess = { screen = "doctor_dashboard" },
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
                            onDetailsClick = { item ->
                                screen = if (item.status == "COMPLETED") "prescriptions" else "live_queue"
                            },
                            onTabSelected = { screen = routeFor(it) }
                        )

                        "live_queue" -> LiveQueueScreen(
                            info = liveQueue,
                            onBack = { screen = "my_appointments" },
                            onRefresh = {
                                // DEMO: protibar refresh e ekta token egiye jabe
                                if (liveQueue.nowServing < liveQueue.myToken) {
                                    val now = liveQueue.nowServing + 1
                                    liveQueue = liveQueue.copy(
                                        nowServing = now,
                                        waiting = (liveQueue.myToken - now - 1).coerceAtLeast(0),
                                        progress = now / liveQueue.myToken.toFloat(),
                                        minutesRemaining = ((liveQueue.myToken - now) * 3).coerceAtLeast(0)
                                    )
                                }
                            }
                        )

                        "prescriptions" -> PrescriptionsScreen(
                            prescriptions = dummyPrescriptions,
                            onTabSelected = { screen = routeFor(it) }
                        )


                        "doctor_dashboard" -> DoctorDashboardScreen(
                            doctorName = "Dr. Ayesha Rahman",
                            appointments = doctorAppointments,
                            onDateChange = { /* backend e GET /api/doctor/appointments?date= */ },
                            onConfirm = { a ->
                                doctorAppointments = doctorAppointments.map {
                                    if (it.id == a.id) it.copy(status = "CONFIRMED", token = 29) else it
                                }
                            },
                            onReject = { a ->
                                doctorAppointments = doctorAppointments.map {
                                    if (it.id == a.id) it.copy(status = "REJECTED") else it
                                }
                            },
                            onComplete = { a ->
                                doctorAppointments = doctorAppointments.map {
                                    if (it.id == a.id) it.copy(status = "COMPLETED") else it
                                }
                            },
                            onAvatarClick = { screen = "profile" }
                        )

                    }
                }
            }
        }
    }
}
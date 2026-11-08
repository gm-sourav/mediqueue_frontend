package com.mediqueue.app

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mediqueue.app.data.ApiClient
import com.mediqueue.app.ui.components.DoctorTab
import com.mediqueue.app.ui.components.LoadOrError
import com.mediqueue.app.ui.components.PatientTab
import com.mediqueue.app.ui.screens.auth.LanguageScreen
import com.mediqueue.app.ui.screens.auth.LoginScreen
import com.mediqueue.app.ui.screens.auth.ProfileScreen
import com.mediqueue.app.ui.screens.auth.ProfileWithBottomBar
import com.mediqueue.app.ui.screens.auth.RegisterScreen
import com.mediqueue.app.ui.screens.auth.SplashScreen
import com.mediqueue.app.ui.screens.doctor.CreateSlotsScreen
import com.mediqueue.app.ui.screens.doctor.DoctorAppointment
import com.mediqueue.app.ui.screens.doctor.DoctorDashboardScreen
import com.mediqueue.app.ui.screens.doctor.QueueControlInfo
import com.mediqueue.app.ui.screens.doctor.QueueControlScreen
import com.mediqueue.app.ui.screens.doctor.QueueEntry
import com.mediqueue.app.ui.screens.doctor.WritePrescriptionScreen
import com.mediqueue.app.ui.screens.patient.DepartmentsScreen
import com.mediqueue.app.ui.screens.patient.DoctorDetailScreen
import com.mediqueue.app.ui.screens.patient.DoctorsListScreen
import com.mediqueue.app.ui.screens.patient.LiveQueueInfo
import com.mediqueue.app.ui.screens.patient.LiveQueueScreen
import com.mediqueue.app.ui.screens.patient.MedicineItem
import com.mediqueue.app.ui.screens.patient.MyAppointmentsScreen
import com.mediqueue.app.ui.screens.patient.PatientHomeScreen
import com.mediqueue.app.ui.screens.patient.PatientViewModel
import com.mediqueue.app.ui.screens.patient.PickSlotScreen
import com.mediqueue.app.ui.screens.patient.PrescriptionItem
import com.mediqueue.app.ui.screens.patient.PrescriptionsScreen
import com.mediqueue.app.ui.screens.patient.nextAppointmentOf
import com.mediqueue.app.ui.screens.patient.slotLabel
import com.mediqueue.app.ui.screens.patient.toDetail
import com.mediqueue.app.ui.screens.patient.toItem
import com.mediqueue.app.ui.screens.patient.toItems
import com.mediqueue.app.ui.screens.patient.toNext
import com.mediqueue.app.ui.screens.patient.toSlotItems
import com.mediqueue.app.ui.screens.patient.toUi
import com.mediqueue.app.ui.theme.MediColors
import java.time.LocalDate
import java.time.LocalTime

private fun routeFor(tab: PatientTab): String = when (tab) {
    PatientTab.Home -> "patient_home"
    PatientTab.Doctors -> "departments"
    PatientTab.Bookings -> "my_appointments"
    PatientTab.Profile -> "profile"
}

private fun homeFor(role: String?): String = when (role) {
    "DOCTOR" -> "doctor_dashboard"
    "ADMIN" -> "admin_home"
    else -> "patient_home"
}

private fun doctorRouteFor(tab: DoctorTab): String = when (tab) {
    DoctorTab.Today -> "doctor_dashboard"
    DoctorTab.Slots -> "create_slots"
    DoctorTab.Queue -> "queue_control"
    DoctorTab.Profile -> "profile"
}

// ---- Ekhono dummy (porer step e real hobe): prescriptions, live queue, doctor screens ----
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

private val dummyQueueControl = QueueControlInfo(
    department = "Cardiology",
    nowServingToken = 21,
    nowServingName = "Nila Akter",
    nowServingTime = "10:00 AM",
    waiting = listOf(
        QueueEntry(22, "Rafi Ahmed", "10:15"),
        QueueEntry(23, "Tania Noor", "10:30"),
        QueueEntry(24, "Sabina Yasmin", "10:45"),
        QueueEntry(25, "Imran Hossain", "11:00"),
        QueueEntry(26, "Mitu Akter", "11:15"),
        QueueEntry(27, "Jahid Hasan", "11:30"),
        QueueEntry(28, "Farzana Islam", "11:45")
    )
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
                    val vm: PatientViewModel = viewModel()

                    // Temporary shorol navigation (pore real Navigation e jabe)
                    var screen by rememberSaveable { mutableStateOf("splash") }
                    var reload by remember { mutableIntStateOf(0) }

                    var liveQueue by remember {
                        mutableStateOf(LiveQueueInfo("Cardiology", "Dr. Ayesha Rahman", "10:30 AM", 28, 21, 7, 0.68f, 25))
                    }
                    var doctorAppointments by remember { mutableStateOf(dummyDoctorAppointments) }
                    var queueControl by remember { mutableStateOf(dummyQueueControl) }
                    var selectedDocAppt by remember { mutableStateOf<DoctorAppointment?>(null) }

                    var selectedDepartment by rememberSaveable { mutableStateOf("") }
                    var selectedDepartmentId by rememberSaveable { mutableLongStateOf(0L) }
                    var selectedDoctorName by rememberSaveable { mutableStateOf("") }
                    var selectedDoctorId by rememberSaveable { mutableLongStateOf(0L) }

                    val locale = LocalConfiguration.current.locales[0]
                    val today = LocalDate.now()
                    val todayWord = stringResource(R.string.today)
                    val tomorrowWord = stringResource(R.string.tomorrow)

                    // Screen e dukle oi screen er data load
                    LaunchedEffect(screen, reload) {
                        when (screen) {
                            "departments" -> vm.loadDepartments()
                            "doctors" -> vm.loadDoctors(selectedDepartmentId)
                            "doctor_detail" -> if (vm.deptDoctors == null) vm.loadDoctors(selectedDepartmentId)
                            "pick_slot" -> vm.loadWeek(selectedDoctorId)
                            "patient_home", "my_appointments" -> vm.loadAppointments()
                        }
                    }

                    // Token expire hole login e pathao
                    LaunchedEffect(vm.unauthorized) {
                        if (vm.unauthorized) {
                            ApiClient.session.clear()
                            vm.reset()
                            screen = "login"
                        }
                    }

                    BackHandler(
                        enabled = screen !in listOf("splash", "login", "patient_home", "doctor_dashboard", "admin_home")
                    ) {
                        screen = when (screen) {
                            "language" -> "profile"
                            "profile" -> homeFor(ApiClient.session.role)
                            "departments", "my_appointments" -> "patient_home"
                            "doctors" -> "departments"
                            "doctor_detail" -> "doctors"
                            "pick_slot" -> "doctor_detail"
                            "live_queue", "prescriptions" -> "my_appointments"
                            "create_slots", "queue_control", "write_prescription" -> "doctor_dashboard"
                            "register" -> "login"
                            else -> "login"
                        }
                    }

                    when (screen) {
                        "splash" -> SplashScreen(
                            onFinished = {
                                screen = if (ApiClient.session.isLoggedIn) homeFor(ApiClient.session.role) else "login"
                            }
                        )

                        "login" -> LoginScreen(
                            onLoginSuccess = { auth ->
                                vm.reset()
                                screen = homeFor(auth.role)
                            },
                            onCreateAccount = { screen = "register" }
                        )

                        "register" -> RegisterScreen(
                            onRegistered = { auth ->
                                vm.reset()
                                screen = homeFor(auth.role)
                            },
                            onSignInClick = { screen = "login" }
                        )

                        "language" -> LanguageScreen(onSaved = { screen = "profile" })

                        "profile", "admin_home" -> ProfileWithBottomBar(
                            role = ApiClient.session.role,
                            onPatientTab = { screen = routeFor(it) },
                            onDoctorTab = { screen = doctorRouteFor(it) }
                        ) {
                            ProfileScreen(
                                name = ApiClient.session.name,
                                email = ApiClient.session.email,
                                role = ApiClient.session.role ?: "",
                                onLanguageClick = { screen = "language" },
                                onLogout = {
                                    ApiClient.session.clear()
                                    vm.reset()
                                    screen = "login"
                                }
                            )
                        }

                        // ================= PATIENT (real data) =================
                        "patient_home" -> {
                            val next = nextAppointmentOf(vm.appointments, today)
                            PatientHomeScreen(
                                patientName = ApiClient.session.name,
                                queue = null,   // Live queue card porer step e
                                nextAppointment = next?.toNext(today, todayWord, tomorrowWord, locale),
                                onBookClick = { screen = "departments" },
                                onAppointmentClick = { screen = "my_appointments" },
                                onAvatarClick = { screen = "profile" },
                                onTabSelected = { screen = routeFor(it) }
                            )
                        }

                        "departments" -> {
                            val deps = vm.departments
                            if (deps == null) {
                                LoadOrError(vm.errorRes) { reload++ }
                            } else {
                                val docs = vm.allDoctors.orEmpty()
                                DepartmentsScreen(
                                    departments = deps.map { dep ->
                                        dep.toUi(docs.count { it.departmentId == dep.id })
                                    },
                                    onDepartmentClick = { dept ->
                                        selectedDepartment = dept.name
                                        selectedDepartmentId = dept.id.toLong()
                                        vm.clearDoctors()
                                        screen = "doctors"
                                    },
                                    onTabSelected = { screen = routeFor(it) }
                                )
                            }
                        }

                        "doctors" -> {
                            val docs = vm.deptDoctors
                            if (docs == null) {
                                LoadOrError(vm.errorRes) { reload++ }
                            } else {
                                DoctorsListScreen(
                                    departmentName = selectedDepartment,
                                    searchHint = stringResource(R.string.search_doctors),
                                    doctors = docs.map { d ->
                                        val ups = vm.upcoming[d.id].orEmpty()
                                        d.toItem(
                                            nextSlot = ups.firstOrNull()
                                                ?.let { slotLabel(it, today, todayWord, tomorrowWord, locale) },
                                            availableToday = ups.any { it.first == today }
                                        )
                                    },
                                    onDoctorClick = { doctor ->
                                        selectedDoctorId = doctor.id.toLong()
                                        selectedDoctorName = doctor.name
                                        screen = "doctor_detail"
                                    },
                                    onTabSelected = { screen = routeFor(it) }
                                )
                            }
                        }

                        "doctor_detail" -> {
                            val d = vm.deptDoctors?.firstOrNull { it.id == selectedDoctorId }
                            if (d == null) {
                                LoadOrError(vm.errorRes) { reload++ }
                            } else {
                                val ups = vm.upcoming[d.id].orEmpty()
                                DoctorDetailScreen(
                                    doctor = d.toDetail(
                                        nextSlots = ups.map { slotLabel(it, today, todayWord, tomorrowWord, locale) },
                                        availableToday = ups.any { it.first == today }
                                    ),
                                    onBack = { screen = "doctors" },
                                    onBookClick = {
                                        vm.clearWeek()
                                        screen = "pick_slot"
                                    },
                                    onSeeAllClick = {
                                        vm.clearWeek()
                                        screen = "pick_slot"
                                    }
                                )
                            }
                        }

                        "pick_slot" -> {
                            val week = vm.week
                            if (week == null) {
                                LoadOrError(vm.errorRes) { reload++ }
                            } else {
                                PickSlotScreen(
                                    doctorName = selectedDoctorName,
                                    slotsForDate = { date ->
                                        week[date].orEmpty().toSlotItems(date, today, LocalTime.now(), locale)
                                    },
                                    isSubmitting = vm.isBooking,
                                    onConfirm = { _, slot, reason ->
                                        vm.book(slot.id, reason) { ok, err ->
                                            if (ok) {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    getString(R.string.booking_requested),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                vm.loadAppointments()
                                                screen = "my_appointments"
                                            } else {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    getString(err ?: R.string.error_generic),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                vm.loadWeek(selectedDoctorId)
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        "my_appointments" -> {
                            val list = vm.appointments
                            if (list == null) {
                                LoadOrError(vm.errorRes) { reload++ }
                            } else {
                                val fees = vm.allDoctors.orEmpty().associate { it.id to it.fee.toInt() }
                                MyAppointmentsScreen(
                                    appointments = list.toItems(fees, today, todayWord, tomorrowWord, locale),
                                    onCancel = { item ->
                                        vm.cancel(item.id) { ok, err ->
                                            Toast.makeText(
                                                this@MainActivity,
                                                getString(if (ok) R.string.booking_cancelled else (err ?: R.string.error_generic)),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onDetailsClick = { item ->
                                        screen = if (item.status == "COMPLETED") "prescriptions" else "live_queue"
                                    },
                                    onTabSelected = { screen = routeFor(it) }
                                )
                            }
                        }

                        // ================= Ekhono dummy =================
                        "live_queue" -> LiveQueueScreen(
                            info = liveQueue,
                            onBack = { screen = "my_appointments" },
                            onRefresh = {
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
                            doctorName = ApiClient.session.name,
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
                            onAvatarClick = { screen = "profile" },
                            onAppointmentClick = { a ->
                                selectedDocAppt = a
                                screen = "write_prescription"
                            },
                            onTabSelected = { screen = doctorRouteFor(it) }
                        )

                        "create_slots" -> CreateSlotsScreen(
                            onCreate = { date, start, end, length, slots ->
                                Toast.makeText(this, "Created ${slots.size} slots on $date (demo)", Toast.LENGTH_LONG).show()
                                screen = "doctor_dashboard"
                            },
                            onTabSelected = { screen = doctorRouteFor(it) }
                        )

                        "queue_control" -> QueueControlScreen(
                            info = queueControl,
                            onCallNext = {
                                val next = queueControl.waiting.firstOrNull()
                                if (next != null) {
                                    queueControl = queueControl.copy(
                                        nowServingToken = next.token,
                                        nowServingName = next.patientName,
                                        nowServingTime = next.time,
                                        waiting = queueControl.waiting.drop(1)
                                    )
                                }
                            },
                            onTabSelected = { screen = doctorRouteFor(it) }
                        )

                        "write_prescription" -> {
                            val a = selectedDocAppt
                            if (a != null) {
                                WritePrescriptionScreen(
                                    patientName = a.patientName,
                                    department = a.department,
                                    time = a.time,
                                    status = a.status,
                                    onBack = { screen = "doctor_dashboard" },
                                    onSave = { medicines, notes ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Prescription saved: ${medicines.size} medicine(s) (demo)",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        screen = "doctor_dashboard"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
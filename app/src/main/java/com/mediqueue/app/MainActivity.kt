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
import com.mediqueue.app.ui.components.FullScreenError
import com.mediqueue.app.ui.components.FullScreenMessage
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
import com.mediqueue.app.ui.screens.doctor.DoctorAction
import com.mediqueue.app.ui.screens.doctor.DoctorDashboardScreen
import com.mediqueue.app.ui.screens.doctor.DoctorViewModel
import com.mediqueue.app.ui.screens.doctor.QueueControlScreen
import com.mediqueue.app.ui.screens.doctor.WritePrescriptionScreen
import com.mediqueue.app.ui.screens.doctor.queueInfoOf
import com.mediqueue.app.ui.screens.doctor.toDoctorAppointments
import com.mediqueue.app.ui.screens.patient.DepartmentsScreen
import com.mediqueue.app.ui.screens.patient.DoctorDetailScreen
import com.mediqueue.app.ui.screens.patient.DoctorsListScreen
import com.mediqueue.app.ui.screens.patient.LiveQueueScreen
import com.mediqueue.app.ui.screens.patient.MyAppointmentsScreen
import com.mediqueue.app.ui.screens.patient.PatientHomeScreen
import com.mediqueue.app.ui.screens.patient.PatientViewModel
import com.mediqueue.app.ui.screens.patient.PickSlotScreen
import com.mediqueue.app.ui.screens.patient.PrescriptionDetailScreen
import com.mediqueue.app.ui.screens.patient.PrescriptionsScreen
import com.mediqueue.app.ui.screens.patient.nextAppointmentOf
import com.mediqueue.app.ui.screens.patient.slotLabel
import com.mediqueue.app.ui.screens.patient.toDetail
import com.mediqueue.app.ui.screens.patient.toItem
import com.mediqueue.app.ui.screens.patient.toItems
import com.mediqueue.app.ui.screens.patient.toLiveQueueInfo
import com.mediqueue.app.ui.screens.patient.toNext
import com.mediqueue.app.ui.screens.patient.toQueueInfo
import com.mediqueue.app.ui.screens.patient.toPrescriptionItems
import com.mediqueue.app.ui.screens.patient.toSlotItems
import com.mediqueue.app.ui.screens.patient.toUi
import com.mediqueue.app.ui.theme.MediColors
import java.text.NumberFormat
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
                    val dvm: DoctorViewModel = viewModel()

                    // Temporary shorol navigation (pore real Navigation e jabe)
                    var screen by rememberSaveable { mutableStateOf("splash") }
                    var reload by remember { mutableIntStateOf(0) }

                    var selectedDocAppt by remember { mutableStateOf<DoctorAppointment?>(null) }

                    var selectedPrescriptionId by rememberSaveable { mutableLongStateOf(0L) }
                    var detailBackTo by rememberSaveable { mutableStateOf("prescriptions") }

                    var selectedDepartment by rememberSaveable { mutableStateOf("") }
                    var selectedDepartmentId by rememberSaveable { mutableLongStateOf(0L) }
                    var selectedDoctorName by rememberSaveable { mutableStateOf("") }
                    var selectedDoctorId by rememberSaveable { mutableLongStateOf(0L) }

                    val locale = LocalConfiguration.current.locales[0]
                    val today = LocalDate.now()
                    val todayWord = stringResource(R.string.today)
                    val tomorrowWord = stringResource(R.string.tomorrow)
                    val noReason = stringResource(R.string.no_reason)

                    fun toast(resId: Int?) {
                        Toast.makeText(
                            this@MainActivity,
                            getString(resId ?: R.string.error_generic),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    // Screen e dukle oi screen er data load
                    LaunchedEffect(screen, reload) {
                        when (screen) {
                            "departments" -> vm.loadDepartments()
                            "doctors" -> vm.loadDoctors(selectedDepartmentId)
                            "doctor_detail" -> if (vm.deptDoctors == null) vm.loadDoctors(selectedDepartmentId)
                            "pick_slot" -> vm.loadWeek(selectedDoctorId)
                            "patient_home", "live_queue" -> vm.loadHome()
                            "my_appointments" -> {
                                vm.loadAppointments()
                                vm.loadPrescriptions()
                            }
                            "prescriptions", "prescription_detail" -> {
                                vm.loadAppointments()
                                vm.loadPrescriptions()
                            }
                            "doctor_dashboard" -> dvm.loadAppointments(LocalDate.now(), clear = true)
                            "queue_control" -> dvm.loadQueue()
                        }
                    }

                    // Token expire hole login e pathao
                    LaunchedEffect(vm.unauthorized, dvm.unauthorized) {
                        if (vm.unauthorized || dvm.unauthorized) {
                            ApiClient.session.clear()
                            vm.reset()
                            dvm.reset()
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
                            "live_queue" -> "my_appointments"
                            "prescriptions" -> "patient_home"
                            "prescription_detail" -> detailBackTo
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
                                dvm.reset()
                                screen = homeFor(auth.role)
                            },
                            onCreateAccount = { screen = "register" }
                        )

                        "register" -> RegisterScreen(
                            onRegistered = { auth ->
                                vm.reset()
                                dvm.reset()
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
                                    dvm.reset()
                                    screen = "login"
                                }
                            )
                        }

                        // ================= PATIENT (real data) =================
                        "patient_home" -> {
                            val next = nextAppointmentOf(vm.appointments, today)
                            PatientHomeScreen(
                                patientName = ApiClient.session.name,
                                queue = vm.queue?.let { q -> vm.activeAppointment?.toQueueInfo(q) },
                                nextAppointment = next?.toNext(today, todayWord, tomorrowWord, locale),
                                onBookClick = { screen = "departments" },
                                onAppointmentClick = { screen = "my_appointments" },
                                onAvatarClick = { screen = "profile" },
                                onTabSelected = { screen = routeFor(it) },
                                onPrescriptionsClick = { screen = "prescriptions" }
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
                                        val rx = vm.prescriptions?.firstOrNull { it.appointmentId == item.id }
                                        if (rx != null) {
                                            selectedPrescriptionId = rx.id
                                            detailBackTo = "my_appointments"
                                            screen = "prescription_detail"
                                        } else {
                                            screen = if (item.status == "COMPLETED") "prescriptions" else "live_queue"
                                        }
                                    },
                                    onTabSelected = { screen = routeFor(it) }
                                )
                            }
                        }

                        // ================= Live Queue (real) =================
                        "live_queue" -> {
                            val appt = vm.activeAppointment
                            val q = vm.queue
                            when {
                                vm.appointments == null -> LoadOrError(vm.errorRes) { reload++ }
                                appt == null -> FullScreenMessage(R.string.queue_not_available, R.string.back) {
                                    screen = "my_appointments"
                                }
                                q == null -> LoadOrError(vm.errorRes) { reload++ }
                                else -> LiveQueueScreen(
                                    info = appt.toLiveQueueInfo(q, locale),
                                    onBack = { screen = "my_appointments" },
                                    onRefresh = { vm.refreshQueue() }
                                )
                            }
                        }

                        "prescriptions" -> {
                            val list = vm.prescriptions
                            if (list == null) {
                                LoadOrError(vm.errorRes) { reload++ }
                            } else {
                                PrescriptionsScreen(
                                    prescriptions = list.toPrescriptionItems(vm.appointments.orEmpty(), today, locale),
                                    onPrescriptionClick = { p ->
                                        selectedPrescriptionId = p.id
                                        detailBackTo = "prescriptions"
                                        screen = "prescription_detail"
                                    },
                                    onTabSelected = { screen = routeFor(it) }
                                )
                            }
                        }

                        "prescription_detail" -> {
                            val list = vm.prescriptions
                            if (list == null) {
                                LoadOrError(vm.errorRes) { reload++ }
                            } else {
                                val item = list
                                    .toPrescriptionItems(vm.appointments.orEmpty(), today, locale)
                                    .firstOrNull { it.id == selectedPrescriptionId }
                                if (item == null) {
                                    FullScreenError(R.string.error_generic) { screen = "prescriptions" }
                                } else {
                                    PrescriptionDetailScreen(
                                        prescription = item,
                                        onBack = { screen = detailBackTo }
                                    )
                                }
                            }
                        }

                        // ================= DOCTOR (real data) =================
                        "doctor_dashboard" -> {
                            val list = dvm.appointments
                            if (list == null) {
                                LoadOrError(dvm.errorRes) { reload++ }
                            } else {
                                DoctorDashboardScreen(
                                    doctorName = ApiClient.session.name,
                                    appointments = list.toDoctorAppointments(locale, noReason),
                                    onDateChange = { d -> dvm.loadAppointments(d, clear = false) },
                                    onConfirm = { a ->
                                        dvm.act(a.id, DoctorAction.CONFIRM) { ok, err -> if (!ok) toast(err) }
                                    },
                                    onReject = { a ->
                                        dvm.act(a.id, DoctorAction.REJECT) { ok, err -> if (!ok) toast(err) }
                                    },
                                    onComplete = { a ->
                                        dvm.act(a.id, DoctorAction.COMPLETE) { ok, err -> if (!ok) toast(err) }
                                    },
                                    onAvatarClick = { screen = "profile" },
                                    onAppointmentClick = { a ->
                                        if (a.status == "CONFIRMED" || a.status == "COMPLETED") {
                                            selectedDocAppt = a
                                            screen = "write_prescription"
                                        } else {
                                            toast(R.string.rx_needs_confirmed)
                                        }
                                    },
                                    onTabSelected = { screen = doctorRouteFor(it) }
                                )
                            }
                        }

                        "create_slots" -> CreateSlotsScreen(
                            onCreate = { date, start, end, length, _ ->
                                dvm.createSlots(date, start, end, length) { ok, count, err ->
                                    if (ok) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(
                                                R.string.slots_created,
                                                NumberFormat.getInstance(locale).format(count)
                                            ),
                                            Toast.LENGTH_LONG
                                        ).show()
                                        screen = "doctor_dashboard"
                                    } else {
                                        toast(err)
                                    }
                                }
                            },
                            isSubmitting = dvm.isSubmitting,
                            onTabSelected = { screen = doctorRouteFor(it) }
                        )

                        "queue_control" -> {
                            val q = dvm.queue
                            val qa = dvm.queueAppointments
                            if (q == null || qa == null) {
                                LoadOrError(dvm.errorRes) { reload++ }
                            } else {
                                QueueControlScreen(
                                    info = queueInfoOf(q, qa, dvm.myDepartment, locale),
                                    onCallNext = {
                                        dvm.callNext { ok, err -> if (!ok) toast(err) }
                                    },
                                    isCalling = dvm.isCalling,
                                    onTabSelected = { screen = doctorRouteFor(it) }
                                )
                            }
                        }

                        "write_prescription" -> {
                            val a = selectedDocAppt
                            if (a != null) {
                                WritePrescriptionScreen(
                                    patientName = a.patientName,
                                    department = a.department,
                                    time = a.time,
                                    status = a.status,
                                    onBack = { screen = "doctor_dashboard" },
                                    onSave = { diagnosis, medicines, notes ->
                                        dvm.savePrescription(a.id, diagnosis, medicines, notes) { ok, err ->
                                            if (ok) {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    getString(R.string.rx_saved),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                screen = "doctor_dashboard"
                                            } else {
                                                toast(err)
                                            }
                                        }
                                    },
                                    isSubmitting = dvm.isSubmitting
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
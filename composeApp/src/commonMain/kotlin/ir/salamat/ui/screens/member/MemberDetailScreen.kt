package ir.salamat.ui.screens.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import ir.salamat.core.datetime.toJalali
import ir.salamat.core.model.Gender
import ir.salamat.core.model.GrowthRecord
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.model.VaccineRecord
import ir.salamat.core.model.VaccineStatus
import ir.salamat.core.ui.theme.SalamatTheme
import ir.salamat.core.vaccine.IranVaccineSchedule
import ir.salamat.ui.screens.checkup.AddCheckupDialog
import ir.salamat.ui.screens.checkup.CheckupCardUiModel
import ir.salamat.ui.screens.checkup.CheckupFilter
import ir.salamat.ui.screens.checkup.CheckupRecordDialog
import ir.salamat.ui.screens.checkup.CheckupStatus
import ir.salamat.ui.screens.checkup.CheckupTrackerView
import ir.salamat.ui.screens.checkup.CheckupUiState
import ir.salamat.ui.screens.checkup.CheckupViewModel
import ir.salamat.ui.screens.growth.BmiCategory
import ir.salamat.ui.screens.growth.GrowthRecordDialog
import ir.salamat.ui.screens.growth.GrowthTrackerView
import ir.salamat.ui.screens.growth.GrowthUiState
import ir.salamat.ui.screens.growth.GrowthViewModel
import ir.salamat.ui.screens.vaccine.VaccineCardUiModel
import ir.salamat.ui.screens.vaccine.VaccineFilter
import ir.salamat.ui.screens.vaccine.VaccineRecordDialog
import ir.salamat.ui.screens.vaccine.VaccineTimelineView
import ir.salamat.ui.screens.vaccine.VaccineUiMapper
import ir.salamat.ui.screens.vaccine.VaccineUiState
import ir.salamat.ui.screens.vaccine.VaccineViewModel
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MemberDetailScreen(
    profileId: String,
    isPersian: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vaccineViewModel: VaccineViewModel = koinViewModel(parameters = { parametersOf(profileId) }),
    growthViewModel: GrowthViewModel = koinViewModel(parameters = { parametersOf(profileId) }),
    checkupViewModel: CheckupViewModel = koinViewModel(parameters = { parametersOf(profileId) })
) {
    val vaccineState by vaccineViewModel.uiState.collectAsState()
    val growthState by growthViewModel.uiState.collectAsState()
    val checkupState by checkupViewModel.uiState.collectAsState()

    MemberDetailContent(
        vaccineState = vaccineState,
        growthState = growthState,
        checkupState = checkupState,
        isPersian = isPersian,
        onBack = onBack,
        onVaccineFilterChange = { vaccineViewModel.setFilter(it) },
        onVaccineClick = { vaccineViewModel.openDialog(it) },
        onDismissVaccineDialog = { vaccineViewModel.openDialog(null) },
        onConfirmAdministeredVaccine = { id, date, notes -> vaccineViewModel.markAdministered(id, date, notes) },
        onMarkVaccinePending = { id -> vaccineViewModel.markPending(id) },
        onOpenAddGrowthDialog = { growthViewModel.setAddDialogOpen(true) },
        onDismissGrowthDialog = { growthViewModel.setAddDialogOpen(false) },
        onConfirmAddGrowth = { date, weight, height, headCirc, notes ->
            growthViewModel.addRecord(date, weight, height, headCirc, notes)
        },
        onDeleteGrowthRecord = { id -> growthViewModel.deleteRecord(id) },
        onCheckupFilterChange = { checkupViewModel.setFilter(it) },
        onOpenCheckupRecordDialog = { checkupViewModel.openRecordCompletionDialog(it) },
        onDismissCheckupRecordDialog = { checkupViewModel.openRecordCompletionDialog(null) },
        onConfirmRecordCheckup = { id, completedDate, nextDueDate, notes ->
            checkupViewModel.recordCheckupCompleted(id, completedDate, nextDueDate, notes)
        },
        onOpenAddCheckupDialog = { checkupViewModel.setAddDialogOpen(true) },
        onDismissAddCheckupDialog = { checkupViewModel.setAddDialogOpen(false) },
        onConfirmAddCheckup = { title, interval, nextDue, notes ->
            checkupViewModel.addCheckup(title, interval, nextDue, notes)
        },
        onDeleteCheckup = { id -> checkupViewModel.deleteCheckup(id) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailContent(
    vaccineState: VaccineUiState,
    growthState: GrowthUiState,
    checkupState: CheckupUiState,
    isPersian: Boolean,
    onBack: () -> Unit,
    onVaccineFilterChange: (VaccineFilter) -> Unit,
    onVaccineClick: (VaccineCardUiModel) -> Unit,
    onDismissVaccineDialog: () -> Unit,
    onConfirmAdministeredVaccine: (String, LocalDate, String?) -> Unit,
    onMarkVaccinePending: (String) -> Unit,
    onOpenAddGrowthDialog: () -> Unit,
    onDismissGrowthDialog: () -> Unit,
    onConfirmAddGrowth: (LocalDate, Double, Double, Double?, String?) -> Unit,
    onDeleteGrowthRecord: (String) -> Unit,
    onCheckupFilterChange: (CheckupFilter) -> Unit,
    onOpenCheckupRecordDialog: (CheckupCardUiModel) -> Unit,
    onDismissCheckupRecordDialog: () -> Unit,
    onConfirmRecordCheckup: (String, LocalDate, LocalDate, String?) -> Unit,
    onOpenAddCheckupDialog: () -> Unit,
    onDismissAddCheckupDialog: () -> Unit,
    onConfirmAddCheckup: (String, Int, LocalDate, String?) -> Unit,
    onDeleteCheckup: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    val profile = vaccineState.profile ?: growthState.profile ?: checkupState.profile
    val isChild = profile?.type == ProfileType.CHILD

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = profile?.name ?: if (isPersian) "پرونده سلامت" else "Health Record",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (profile == null && (vaccineState.isLoading || growthState.isLoading || checkupState.isLoading)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        if (profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPersian) "پرونده یافت نشد." else "Profile not found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile Hero Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(profile.avatarColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.name.take(1),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val jalali = profile.birthDate.toJalali()
                    val birthText = if (isPersian)
                        "متولد: ${jalali.formatPersian()}"
                    else
                        "Born: ${profile.birthDate}"
                    val typeText = if (isChild) {
                        if (isPersian) "کودک" else "Child"
                    } else {
                        if (isPersian) "بزرگسال" else "Adult"
                    }
                    Text(
                        text = "$typeText • $birthText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Dynamic Tabs: Adapted by Profile Type (Child vs Adult)
            val tab0Title = if (isChild) {
                if (isPersian) "واکسیناسیون" else "Vaccines"
            } else {
                if (isPersian) "چک‌آپ و غربالگری" else "Checkups & Screening"
            }
            val tab1Title = if (isChild) {
                if (isPersian) "پایش رشد" else "Growth Tracker"
            } else {
                if (isPersian) "شاخص بدنی و وزن" else "Weight & BMI"
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = tab0Title,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = tab1Title,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTabIndex) {
                0 -> {
                    if (isChild) {
                        VaccineTimelineView(
                            state = vaccineState,
                            isPersian = isPersian,
                            onFilterChange = onVaccineFilterChange,
                            onVaccineClick = onVaccineClick
                        )
                    } else {
                        CheckupTrackerView(
                            state = checkupState,
                            isPersian = isPersian,
                            onFilterChange = onCheckupFilterChange,
                            onOpenRecordDialog = onOpenCheckupRecordDialog,
                            onOpenAddDialog = onOpenAddCheckupDialog,
                            onDeleteCheckup = onDeleteCheckup
                        )
                    }
                }
                1 -> {
                    GrowthTrackerView(
                        state = growthState,
                        isPersian = isPersian,
                        onOpenAddDialog = onOpenAddGrowthDialog,
                        onDeleteRecord = onDeleteGrowthRecord
                    )
                }
            }
        }

        // Active Dialog for Recording Vaccine Administration (Child)
        val dialogVaccine = vaccineState.activeDialogVaccine
        if (dialogVaccine != null) {
            VaccineRecordDialog(
                vaccine = dialogVaccine,
                isPersian = isPersian,
                onDismiss = onDismissVaccineDialog,
                onConfirmAdministered = onConfirmAdministeredVaccine,
                onMarkPending = onMarkVaccinePending
            )
        }

        // Active Dialog for Adding Growth Record
        if (growthState.isAddDialogOpen) {
            GrowthRecordDialog(
                isPersian = isPersian,
                isChild = isChild,
                onDismiss = onDismissGrowthDialog,
                onConfirm = onConfirmAddGrowth
            )
        }

        // Active Dialog for Recording Checkup Completion (Adult)
        val dialogCheckup = checkupState.activeDialogCheckup
        if (dialogCheckup != null) {
            CheckupRecordDialog(
                checkup = dialogCheckup,
                isPersian = isPersian,
                onDismiss = onDismissCheckupRecordDialog,
                onConfirm = onConfirmRecordCheckup
            )
        }

        // Active Dialog for Adding Custom Checkup (Adult)
        if (checkupState.isAddDialogOpen) {
            AddCheckupDialog(
                isPersian = isPersian,
                onDismiss = onDismissAddCheckupDialog,
                onConfirm = onConfirmAddCheckup
            )
        }
    }
}

@Preview
@Composable
private fun MemberDetailChildPreview() {
    val sampleChild = Profile(
        id = "c1",
        name = "آرتین رضایی",
        birthDate = LocalDate(2024, 1, 1),
        gender = Gender.MALE,
        type = ProfileType.CHILD,
        avatarColor = 0xFF0A686D.toInt(),
        createdAt = 0L
    )
    val records = IranVaccineSchedule.ALL_VACCINES.mapIndexed { index, def ->
        VaccineRecord(
            id = "vr_$index",
            profileId = "c1",
            vaccineCode = def.code,
            targetAgeMonths = def.targetAgeMonths,
            status = if (def.targetAgeMonths == 0) VaccineStatus.COMPLETED else VaccineStatus.UPCOMING,
            administeredDate = if (def.targetAgeMonths == 0) LocalDate(2024, 1, 1) else null,
            notes = null,
            createdAt = 0L
        )
    }
    val milestones = VaccineUiMapper.mapToMilestones(
        birthDate = sampleChild.birthDate,
        records = records,
        currentDate = LocalDate(2024, 4, 1)
    )
    val vaccineState = VaccineUiState(
        profile = sampleChild,
        milestones = milestones,
        completedCount = 3,
        totalCount = records.size,
        progress = 3f / records.size.toFloat(),
        selectedFilter = VaccineFilter.ALL,
        activeDialogVaccine = null,
        isLoading = false
    )
    val growthRecord = GrowthRecord(
        id = "gr_1",
        profileId = "c1",
        date = LocalDate(2024, 4, 1),
        weightKg = 6.4,
        heightCm = 62.0,
        headCircumferenceCm = 40.5,
        bmi = 16.6,
        notes = null,
        createdAt = 0L
    )
    val growthState = GrowthUiState(
        profile = sampleChild,
        records = listOf(growthRecord),
        latestRecord = growthRecord,
        bmiCategory = BmiCategory.NORMAL,
        idealWeightRange = Pair(7.1, 9.5),
        isAddDialogOpen = false,
        isLoading = false
    )

    SalamatTheme(isRtl = true) {
        MemberDetailContent(
            vaccineState = vaccineState,
            growthState = growthState,
            checkupState = CheckupUiState(isLoading = false),
            isPersian = true,
            onBack = {},
            onVaccineFilterChange = {},
            onVaccineClick = {},
            onDismissVaccineDialog = {},
            onConfirmAdministeredVaccine = { _, _, _ -> },
            onMarkVaccinePending = {},
            onOpenAddGrowthDialog = {},
            onDismissGrowthDialog = {},
            onConfirmAddGrowth = { _, _, _, _, _ -> },
            onDeleteGrowthRecord = {},
            onCheckupFilterChange = {},
            onOpenCheckupRecordDialog = {},
            onDismissCheckupRecordDialog = {},
            onConfirmRecordCheckup = { _, _, _, _ -> },
            onOpenAddCheckupDialog = {},
            onDismissAddCheckupDialog = {},
            onConfirmAddCheckup = { _, _, _, _ -> },
            onDeleteCheckup = {}
        )
    }
}

@Preview
@Composable
private fun MemberDetailAdultPreview() {
    val sampleAdult = Profile(
        id = "a1",
        name = "مریم حسینی",
        birthDate = LocalDate(1988, 5, 20),
        gender = Gender.FEMALE,
        type = ProfileType.ADULT,
        avatarColor = 0xFF8E24AA.toInt(),
        createdAt = 0L
    )
    val checkupList = listOf(
        CheckupCardUiModel(
            id = "c1",
            profileId = "a1",
            titleKey = "blood_pressure",
            title = "سنجش فشار خون",
            description = "غربالگری پرفشاری خون و ارزیابی سلامت قلب و عروق.",
            intervalMonths = 6,
            lastCompletedDate = LocalDate(2026, 3, 10),
            nextDueDate = LocalDate(2026, 9, 10),
            status = CheckupStatus.OVERDUE,
            daysUntilDue = -6,
            notes = "آخرین مقدار: ۱۱ روی ۷"
        ),
        CheckupCardUiModel(
            id = "c2",
            profileId = "a1",
            titleKey = "annual_blood_test",
            title = "آزمایش قند ناشتا و چربی خون",
            description = "غربالگری دیابت نوع ۲، کلسترول و تری‌گلیسرید (برنامه ملی ایراپن).",
            intervalMonths = 12,
            lastCompletedDate = null,
            nextDueDate = LocalDate(2026, 10, 20),
            status = CheckupStatus.DUE,
            daysUntilDue = 34,
            notes = null
        )
    )
    val checkupState = CheckupUiState(
        profile = sampleAdult,
        checkups = checkupList,
        filteredCheckups = checkupList,
        selectedFilter = CheckupFilter.ALL,
        dueCount = 1,
        overdueCount = 1,
        completedCount = 0,
        isLoading = false
    )
    val growthRecord = GrowthRecord(
        id = "gr_2",
        profileId = "a1",
        date = LocalDate(2026, 4, 1),
        weightKg = 62.0,
        heightCm = 165.0,
        headCircumferenceCm = null,
        bmi = 22.8,
        notes = null,
        createdAt = 0L
    )
    val growthState = GrowthUiState(
        profile = sampleAdult,
        records = listOf(growthRecord),
        latestRecord = growthRecord,
        bmiCategory = BmiCategory.NORMAL,
        idealWeightRange = Pair(50.4, 67.8),
        isAddDialogOpen = false,
        isLoading = false
    )

    SalamatTheme(isRtl = true) {
        MemberDetailContent(
            vaccineState = VaccineUiState(isLoading = false),
            growthState = growthState,
            checkupState = checkupState,
            isPersian = true,
            onBack = {},
            onVaccineFilterChange = {},
            onVaccineClick = {},
            onDismissVaccineDialog = {},
            onConfirmAdministeredVaccine = { _, _, _ -> },
            onMarkVaccinePending = {},
            onOpenAddGrowthDialog = {},
            onDismissGrowthDialog = {},
            onConfirmAddGrowth = { _, _, _, _, _ -> },
            onDeleteGrowthRecord = {},
            onCheckupFilterChange = {},
            onOpenCheckupRecordDialog = {},
            onDismissCheckupRecordDialog = {},
            onConfirmRecordCheckup = { _, _, _, _ -> },
            onOpenAddCheckupDialog = {},
            onDismissAddCheckupDialog = {},
            onConfirmAddCheckup = { _, _, _, _ -> },
            onDeleteCheckup = {}
        )
    }
}

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import ir.salamat.core.datetime.toJalali
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.model.VaccineRecord
import ir.salamat.core.model.VaccineStatus
import ir.salamat.core.ui.theme.SalamatTheme
import ir.salamat.core.vaccine.IranVaccineSchedule
import ir.salamat.ui.screens.vaccine.VaccineCardUiModel
import ir.salamat.ui.screens.vaccine.VaccineFilter
import ir.salamat.ui.screens.vaccine.VaccineRecordDialog
import ir.salamat.ui.screens.vaccine.VaccineTimelineView
import ir.salamat.ui.screens.vaccine.VaccineUiMapper
import ir.salamat.ui.screens.vaccine.VaccineUiState
import ir.salamat.ui.screens.vaccine.VaccineViewModel
import kotlinx.datetime.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MemberDetailScreen(
    profileId: String,
    isPersian: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaccineViewModel = koinViewModel(parameters = { parametersOf(profileId) })
) {
    val state by viewModel.uiState.collectAsState()

    MemberDetailContent(
        state = state,
        isPersian = isPersian,
        onBack = onBack,
        onFilterChange = { viewModel.setFilter(it) },
        onVaccineClick = { viewModel.openDialog(it) },
        onDismissDialog = { viewModel.openDialog(null) },
        onConfirmAdministered = { id, date, notes -> viewModel.markAdministered(id, date, notes) },
        onMarkPending = { id -> viewModel.markPending(id) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailContent(
    state: VaccineUiState,
    isPersian: Boolean,
    onBack: () -> Unit,
    onFilterChange: (VaccineFilter) -> Unit,
    onVaccineClick: (VaccineCardUiModel) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmAdministered: (String, LocalDate, String?) -> Unit,
    onMarkPending: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    val profile = state.profile

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
        if (state.isLoading || profile == null) {
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
                    val typeText = if (profile.type == ProfileType.CHILD) {
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

            // Tabs (Vaccination & Growth)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = if (isPersian) "واکسیناسیون" else "Vaccines",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = if (isPersian) "پایش رشد" else "Growth Tracker",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTabIndex) {
                0 -> {
                    VaccineTimelineView(
                        state = state,
                        isPersian = isPersian,
                        onFilterChange = onFilterChange,
                        onVaccineClick = onVaccineClick
                    )
                }
                1 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPersian)
                                "بخش پایش رشد در مرحله بعد (گام ۵.۶) فعال خواهد شد."
                            else
                                "Growth Tracker will be enabled in step 5.6.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Active Dialog for Recording Vaccine Administration
        val dialogVaccine = state.activeDialogVaccine
        if (dialogVaccine != null) {
            VaccineRecordDialog(
                vaccine = dialogVaccine,
                isPersian = isPersian,
                onDismiss = onDismissDialog,
                onConfirmAdministered = onConfirmAdministered,
                onMarkPending = onMarkPending
            )
        }
    }
}

@Preview
@Composable
private fun MemberDetailPreview() {
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
    val state = VaccineUiState(
        profile = sampleChild,
        milestones = milestones,
        completedCount = 3,
        totalCount = records.size,
        progress = 3f / records.size.toFloat(),
        selectedFilter = VaccineFilter.ALL,
        activeDialogVaccine = null,
        isLoading = false
    )

    SalamatTheme(isRtl = true) {
        MemberDetailContent(
            state = state,
            isPersian = true,
            onBack = {},
            onFilterChange = {},
            onVaccineClick = {},
            onDismissDialog = {},
            onConfirmAdministered = { _, _, _ -> },
            onMarkPending = {}
        )
    }
}

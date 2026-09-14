package ir.salamat.ui.screens.vaccine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.model.VaccineRecord
import ir.salamat.core.model.VaccineStatus
import ir.salamat.core.ui.theme.SalamatTheme
import ir.salamat.core.vaccine.IranVaccineSchedule
import kotlinx.datetime.LocalDate
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun VaccineTimelineView(
    state: VaccineUiState,
    isPersian: Boolean,
    onFilterChange: (VaccineFilter) -> Unit,
    onVaccineClick: (VaccineCardUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Progress Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPersian) "پیشرفت واکسیناسیون" else "Immunization Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        val percent = (state.progress * 100).toInt()
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isPersian)
                            "${state.completedCount} از ${state.totalCount} نوبت واکسن دریافت شده است."
                        else
                            "${state.completedCount} of ${state.totalCount} doses administered.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Filter chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = state.selectedFilter == VaccineFilter.ALL,
                        onClick = { onFilterChange(VaccineFilter.ALL) },
                        label = { Text(if (isPersian) "همه (${state.totalCount})" else "All (${state.totalCount})") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedFilter == VaccineFilter.DUE_OR_OVERDUE,
                        onClick = { onFilterChange(VaccineFilter.DUE_OR_OVERDUE) },
                        label = { Text(if (isPersian) "نیازمند تزریق" else "Due / Overdue") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedFilter == VaccineFilter.COMPLETED,
                        onClick = { onFilterChange(VaccineFilter.COMPLETED) },
                        label = { Text(if (isPersian) "تکمیل شده" else "Completed") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedFilter == VaccineFilter.UPCOMING,
                        onClick = { onFilterChange(VaccineFilter.UPCOMING) },
                        label = { Text(if (isPersian) "نوبت‌های آینده" else "Upcoming") }
                    )
                }
            }
        }

        // Milestones
        if (state.milestones.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPersian) "موردی با این فیلتر یافت نشد." else "No vaccines match this filter.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(state.milestones, key = { it.targetAgeMonths }) { milestone ->
                MilestoneSection(
                    milestone = milestone,
                    isPersian = isPersian,
                    onVaccineClick = onVaccineClick
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MilestoneSection(
    milestone: VaccineMilestoneUiModel,
    isPersian: Boolean,
    onVaccineClick: (VaccineCardUiModel) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Milestone header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isPersian) milestone.titleFa else milestone.titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val dueText = if (isPersian)
                        "موعد: ${milestone.jalaliDueDate.formatPersian()}"
                    else
                        "Due: ${milestone.dueDate}"
                    Text(
                        text = dueText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                MilestoneStatusBadge(status = milestone.status, isPersian = isPersian)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vaccines list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                milestone.vaccines.forEach { vaccine ->
                    VaccineItemRow(
                        vaccine = vaccine,
                        isPersian = isPersian,
                        onClick = { onVaccineClick(vaccine) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneStatusBadge(
    status: MilestoneStatus,
    isPersian: Boolean
) {
    val (label, containerColor, contentColor) = when (status) {
        MilestoneStatus.ALL_COMPLETED -> Triple(
            if (isPersian) "تکمیل شده" else "Completed",
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32)
        )
        MilestoneStatus.OVERDUE -> Triple(
            if (isPersian) "تأخیر در تزریق" else "Overdue",
            Color(0xFFFFEBEE),
            Color(0xFFC62828)
        )
        MilestoneStatus.DUE_NOW -> Triple(
            if (isPersian) "موعد دریافت" else "Due Now",
            Color(0xFFFFF3E0),
            Color(0xFFE65100)
        )
        MilestoneStatus.UPCOMING -> Triple(
            if (isPersian) "در آینده" else "Upcoming",
            Color(0xFFF5F5F5),
            Color(0xFF757575)
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
private fun VaccineItemRow(
    vaccine: VaccineCardUiModel,
    isPersian: Boolean,
    onClick: () -> Unit
) {
    val isCompleted = vaccine.status == VaccineStatus.COMPLETED
    val isOverdue = vaccine.status == VaccineStatus.OVERDUE
    val isDue = vaccine.status == VaccineStatus.DUE

    val tintColor = when {
        isCompleted -> Color(0xFF2E7D32)
        isOverdue -> Color(0xFFC62828)
        isDue -> Color(0xFFE65100)
        else -> Color(0xFF9E9E9E)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = tintColor.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.Favorite,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPersian) vaccine.definition.nameFa else vaccine.definition.nameEn,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = when {
                    isCompleted -> {
                        val dateStr = vaccine.administeredJalali?.formatPersian() ?: vaccine.administeredDate.toString()
                        if (isPersian) "تزریق شده در: $dateStr" else "Administered on: $dateStr"
                    }
                    isOverdue -> if (isPersian) "موعد گذشته — نیازمند مراجعه فوری" else "Past due date — action required"
                    isDue -> if (isPersian) "هم‌اکنون قابل دریافت است" else "Due now"
                    else -> if (isPersian) "نوبت آینده" else "Upcoming scheduled dose"
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCompleted || isOverdue || isDue) tintColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun VaccineTimelinePreview() {
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
        VaccineTimelineView(
            state = state,
            isPersian = true,
            onFilterChange = {},
            onVaccineClick = {}
        )
    }
}

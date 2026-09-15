package ir.salamat.ui.screens.checkup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import ir.salamat.core.datetime.toJalali
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.ui.theme.SalamatTheme
import kotlinx.datetime.LocalDate
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckupTrackerView(
    state: CheckupUiState,
    isPersian: Boolean,
    onFilterChange: (CheckupFilter) -> Unit,
    onOpenRecordDialog: (CheckupCardUiModel) -> Unit,
    onOpenAddDialog: () -> Unit,
    onDeleteCheckup: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary & Adherence Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            CheckupSummaryCard(
                state = state,
                isPersian = isPersian,
                onOpenAddDialog = onOpenAddDialog
            )
        }

        // Filter Chips Row
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CheckupFilter.entries.forEach { filter ->
                    val badgeCount = when (filter) {
                        CheckupFilter.ALL -> state.checkups.size
                        CheckupFilter.DUE_OR_OVERDUE -> state.dueCount + state.overdueCount
                        CheckupFilter.UPCOMING -> state.checkups.count { it.status == CheckupStatus.UPCOMING }
                        CheckupFilter.COMPLETED -> state.completedCount
                    }
                    val title = if (isPersian) filter.titleFa else filter.titleEn
                    val labelText = if (badgeCount > 0) "$title ($badgeCount)" else title

                    FilterChip(
                        selected = state.selectedFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (state.selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Section Title & Add Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isPersian) "فهرست غربالگری و چک‌آپ‌ها" else "Screening & Checkup List",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = onOpenAddDialog,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isPersian) "افزودن" else "Add")
                }
            }
        }

        // Empty State
        if (state.filteredCheckups.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isPersian) "موردی در این فیلتر یافت نشد." else "No checkups in this filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isPersian)
                                "می‌توانید فیلتر دیگری را انتخاب کنید یا چک‌آپ جدیدی ثبت نمایید."
                            else
                                "Choose another filter or add a custom checkup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Checkup Items
            items(state.filteredCheckups, key = { it.id }) { checkup ->
                CheckupCardItem(
                    checkup = checkup,
                    isPersian = isPersian,
                    onOpenRecordDialog = { onOpenRecordDialog(checkup) },
                    onDelete = { onDeleteCheckup(checkup.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CheckupSummaryCard(
    state: CheckupUiState,
    isPersian: Boolean,
    onOpenAddDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isPersian) "وضعیت چک‌آپ‌های دوره‌ای" else "Periodic Checkup Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Adherence message
            val statusColor = when {
                state.overdueCount > 0 -> Color(CheckupStatus.OVERDUE.colorHex)
                state.dueCount > 0 -> Color(CheckupStatus.DUE.colorHex)
                else -> Color(CheckupStatus.COMPLETED.colorHex)
            }

            val statusText = when {
                state.overdueCount > 0 -> {
                    if (isPersian)
                        "شما ${state.overdueCount} چک‌آپ دارای تأخیر دارید؛ لطفاً جهت حفظ سلامت مراجعه فرمایید."
                    else
                        "You have ${state.overdueCount} overdue checkups. Please schedule a visit."
                }
                state.dueCount > 0 -> {
                    if (isPersian)
                        "${state.dueCount} چک‌آپ در این دوره زمانی موعد رسیدگی دارند."
                    else
                        "${state.dueCount} checkups are due for review soon."
                }
                else -> {
                    if (isPersian)
                        "تمامی مراقبت‌ها و آزمایش‌های دوره‌ای شما تا این تاریخ به‌موقع و بر اساس راهنمای بالینی پیگیری شده است."
                    else
                        "All preventive screenings and checkups are up to date."
                }
            }

            Surface(
                color = statusColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (state.overdueCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats row (Overdue / Due / Completed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CheckupStatMetric(
                    label = if (isPersian) "دارای تأخیر" else "Overdue",
                    count = state.overdueCount,
                    color = Color(CheckupStatus.OVERDUE.colorHex),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CheckupStatMetric(
                    label = if (isPersian) "موعد فرا رسیده" else "Due Soon",
                    count = state.dueCount,
                    color = Color(CheckupStatus.DUE.colorHex),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                CheckupStatMetric(
                    label = if (isPersian) "انجام شده" else "Completed",
                    count = state.completedCount,
                    color = Color(CheckupStatus.COMPLETED.colorHex),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CheckupStatMetric(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CheckupCardItem(
    checkup: CheckupCardUiModel,
    isPersian: Boolean,
    onOpenRecordDialog: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = Color(checkup.status.colorHex)
    val nextJalali = checkup.nextDueDate.toJalali()
    val lastJalali = checkup.lastCompletedDate?.toJalali()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Status badge & Interval pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Badge
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isPersian) checkup.status.titleFa else checkup.status.titleEn,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Interval pill & Delete button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isPersian) "هر ${checkup.intervalMonths} ماه" else "Every ${checkup.intervalMonths}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Description
            Text(
                text = checkup.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (checkup.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = checkup.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dates section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isPersian) "موعد بعدی:" else "Next Due:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val daysText = when {
                        checkup.daysUntilDue < 0 -> if (isPersian) " (${abs(checkup.daysUntilDue)} روز تأخیر)" else " (${abs(checkup.daysUntilDue)} days overdue)"
                        checkup.daysUntilDue == 0 -> if (isPersian) " (امروز)" else " (Today)"
                        else -> if (isPersian) " (${checkup.daysUntilDue} روز مانده)" else " (${checkup.daysUntilDue} days left)"
                    }
                    Text(
                        text = (if (isPersian) nextJalali.formatPersian() else checkup.nextDueDate.toString()) + daysText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                if (lastJalali != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isPersian) "آخرین انجام:" else "Last Done:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isPersian) lastJalali.formatPersian() else checkup.lastCompletedDate.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Optional Notes
            if (!checkup.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = checkup.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Record completion
            Button(
                onClick = onOpenRecordDialog,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPersian) "ثبت انجام چک‌آپ / نتایج" else "Record Completion / Results",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview
@Composable
private fun CheckupTrackerWithDataPreview() {
    SalamatTheme(isRtl = true) {
        CheckupTrackerView(
            state = CheckupUiState(
                profile = Profile(
                    id = "p1",
                    name = "سارا احمدی",
                    birthDate = LocalDate(1985, 4, 12),
                    gender = Gender.FEMALE,
                    type = ProfileType.ADULT,
                    avatarColor = 0xFF3F51B5.toInt(),
                    createdAt = 0
                ),
                checkups = listOf(
                    CheckupCardUiModel(
                        id = "c1",
                        profileId = "p1",
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
                        profileId = "p1",
                        titleKey = "annual_blood_test",
                        title = "آزمایش قند ناشتا و چربی خون",
                        description = "غربالگری دیابت نوع ۲، کلسترول و تری‌گلیسرید (برنامه ملی ایراپن).",
                        intervalMonths = 12,
                        lastCompletedDate = null,
                        nextDueDate = LocalDate(2026, 10, 5),
                        status = CheckupStatus.DUE,
                        daysUntilDue = 19,
                        notes = null
                    ),
                    CheckupCardUiModel(
                        id = "c3",
                        profileId = "p1",
                        titleKey = "breast_screening",
                        title = "غربالگری سرطان پستان (ماموگرافی)",
                        description = "ماموگرافی دوره‌ای و معاینه بالینی پستان برای بانوان بالای ۴۰ سال.",
                        intervalMonths = 24,
                        lastCompletedDate = LocalDate(2026, 1, 15),
                        nextDueDate = LocalDate(2028, 1, 15),
                        status = CheckupStatus.COMPLETED,
                        daysUntilDue = 485,
                        notes = "نتیجه طبیعی اعلام شد."
                    )
                ),
                filteredCheckups = listOf(
                    CheckupCardUiModel(
                        id = "c1",
                        profileId = "p1",
                        titleKey = "blood_pressure",
                        title = "سنجش فشار خون",
                        description = "غربالگری پرفشاری خون و ارزیابی سلامت قلب و عروق.",
                        intervalMonths = 6,
                        lastCompletedDate = LocalDate(2026, 3, 10),
                        nextDueDate = LocalDate(2026, 9, 10),
                        status = CheckupStatus.OVERDUE,
                        daysUntilDue = -6,
                        notes = "آخرین مقدار: ۱۱ روی ۷"
                    )
                ),
                selectedFilter = CheckupFilter.ALL,
                dueCount = 1,
                overdueCount = 1,
                completedCount = 1,
                isLoading = false
            ),
            isPersian = true,
            onFilterChange = {},
            onOpenRecordDialog = {},
            onOpenAddDialog = {},
            onDeleteCheckup = {}
        )
    }
}

@Preview
@Composable
private fun CheckupTrackerEmptyPreview() {
    SalamatTheme(isRtl = true) {
        CheckupTrackerView(
            state = CheckupUiState(
                profile = Profile(
                    id = "p1",
                    name = "سارا احمدی",
                    birthDate = LocalDate(1985, 4, 12),
                    gender = Gender.FEMALE,
                    type = ProfileType.ADULT,
                    avatarColor = 0xFF3F51B5.toInt(),
                    createdAt = 0
                ),
                checkups = emptyList(),
                filteredCheckups = emptyList(),
                selectedFilter = CheckupFilter.ALL,
                dueCount = 0,
                overdueCount = 0,
                completedCount = 0,
                isLoading = false
            ),
            isPersian = true,
            onFilterChange = {},
            onOpenRecordDialog = {},
            onOpenAddDialog = {},
            onDeleteCheckup = {}
        )
    }
}

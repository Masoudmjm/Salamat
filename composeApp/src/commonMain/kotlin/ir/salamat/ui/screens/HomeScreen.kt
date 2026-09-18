package ir.salamat.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import ir.salamat.core.datetime.toJalali
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.ui.theme.SalamatTheme
import ir.salamat.ui.AppUiState
import ir.salamat.ui.components.ProfileAvatar
import ir.salamat.ui.screens.home.AlertSeverity
import ir.salamat.ui.screens.home.AlertType
import ir.salamat.ui.screens.home.FamilyAlertItem
import kotlinx.datetime.LocalDate

@Composable
fun HomeScreen(
    state: AppUiState,
    onNavigateToFamily: () -> Unit,
    onNavigateToQuickTools: () -> Unit,
    onNavigateToAddProfile: () -> Unit,
    onNavigateToMemberDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeProfile = state.activeProfile

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Active Profile Hero Card
        item {
            Spacer(modifier = Modifier.height(8.dp))
            if (activeProfile != null) {
                ActiveProfileHeroCard(
                    profile = activeProfile,
                    isPersian = state.isPersian,
                    onClick = {
                        onNavigateToMemberDetail?.invoke(activeProfile.id) ?: onNavigateToFamily()
                    }
                )
            } else {
                EmptyProfileHeroCard(
                    isPersian = state.isPersian,
                    onAddProfile = onNavigateToAddProfile
                )
            }
        }

        // 2. Adaptive Quick Access Cards (Child vs Adult)
        item {
            Text(
                text = if (state.isPersian) "دسترسی سریع" else "Quick Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            val isChild = activeProfile?.type == ProfileType.CHILD
            val action1Title = if (isChild || activeProfile == null) {
                if (state.isPersian) "واکسیناسیون" else "Vaccines"
            } else {
                if (state.isPersian) "چک‌آپ و غربالگری" else "Checkups"
            }
            val action1Subtitle = if (isChild || activeProfile == null) {
                if (state.isPersian) "برنامه کشوری (۱۶ نوبت)" else "National Schedule"
            } else {
                if (state.isPersian) "فشار خون، دیابت و چربی" else "Preventive Health"
            }

            val action2Title = if (isChild || activeProfile == null) {
                if (state.isPersian) "پایش رشد" else "Growth Tracker"
            } else {
                if (state.isPersian) "شاخص بدنی و وزن" else "Weight & BMI"
            }
            val action2Subtitle = if (isChild || activeProfile == null) {
                if (state.isPersian) "قد، وزن و دور سر" else "Height & Weight"
            } else {
                if (state.isPersian) "ارزیابی تناسب و سلامت" else "Body Mass Index"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = action1Title,
                    subtitle = action1Subtitle,
                    icon = Icons.Default.Favorite,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (activeProfile != null) {
                            onNavigateToMemberDetail?.invoke(activeProfile.id) ?: onNavigateToQuickTools()
                        } else {
                            onNavigateToQuickTools()
                        }
                    }
                )
                QuickActionCard(
                    title = action2Title,
                    subtitle = action2Subtitle,
                    icon = Icons.Default.DateRange,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (activeProfile != null) {
                            onNavigateToMemberDetail?.invoke(activeProfile.id) ?: onNavigateToQuickTools()
                        } else {
                            onNavigateToQuickTools()
                        }
                    }
                )
            }
        }

        // 3. Family Health Status Banner
        item {
            Text(
                text = if (state.isPersian) "وضعیت سلامت خانواده" else "Family Health Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            FamilyHealthStatusCard(
                state = state,
                isPersian = state.isPersian,
                onAddProfile = onNavigateToAddProfile
            )
        }

        // 4. Family-Wide Urgent Alerts Feed
        if (state.alerts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (state.isPersian) "یادآورهای مهم خانواده" else "Urgent Family Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        color = if (state.overdueCount > 0)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (state.isPersian) "${state.alerts.size} مورد" else "${state.alerts.size} alerts",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (state.overdueCount > 0)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            items(state.alerts, key = { it.id }) { alert ->
                FamilyAlertCard(
                    alert = alert,
                    isPersian = state.isPersian,
                    onClick = { onNavigateToMemberDetail?.invoke(alert.profileId) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FamilyHealthStatusCard(
    state: AppUiState,
    isPersian: Boolean,
    onAddProfile: () -> Unit
) {
    val hasProfiles = state.profiles.isNotEmpty()
    val overdue = state.overdueCount
    val dueSoon = state.dueSoonCount

    val bannerColor = when {
        !hasProfiles -> MaterialTheme.colorScheme.surface
        overdue > 0 -> Color(AlertSeverity.OVERDUE.colorHex)
        dueSoon > 0 -> Color(AlertSeverity.DUE_SOON.colorHex)
        else -> Color(0xFF2E7D32)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!hasProfiles) {
                Text(
                    text = if (isPersian)
                        "برای مشاهده یادآورهای واکسن و چکاپ‌های دوره‌ای، ابتدا یک عضو خانواده اضافه کنید."
                    else
                        "Add a family member to see tailored immunization and checkup alerts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(bannerColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (overdue > 0 || dueSoon > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = bannerColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val headerText = when {
                            overdue > 0 -> if (isPersian) "$overdue نوبت دارای تأخیر در خانواده" else "$overdue Overdue items in family"
                            dueSoon > 0 -> if (isPersian) "$dueSoon نوبت نیازمند پیگیری در این ماه" else "$dueSoon Due soon items in family"
                            else -> if (isPersian) "تمام سوابق خانواده به‌روز است" else "All family health records on track"
                        }
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = bannerColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val subText = when {
                            overdue > 0 -> if (isPersian)
                                "جهت حفظ سلامت، لطفاً نسبت به مراجعه و ثبت موارد دارای تأخیر اقدام فرمایید."
                            else
                                "Please schedule visits for overdue items as soon as possible."
                            dueSoon > 0 -> if (isPersian)
                                "موعد واکسن‌ها یا آزمایش‌های دوره‌ای اعضای خانواده فرا رسیده است."
                            else
                                "Scheduled vaccinations or checkups are due soon."
                            else -> if (isPersian)
                                "تمام واکسیناسیون‌ها و چک‌آپ‌های دوره‌ای طبق برنامه نظام سلامت پیش می‌رود."
                            else
                                "All vaccinations and adult checkups are up to date."
                        }
                        Text(
                            text = subText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyAlertCard(
    alert: FamilyAlertItem,
    isPersian: Boolean,
    onClick: () -> Unit
) {
    val severityColor = Color(alert.severity.colorHex)
    val jalaliDate = alert.dueDate.toJalali()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile avatar
            ProfileAvatar(
                name = alert.profileName,
                avatarColor = alert.profileAvatarColor,
                avatarPhoto = alert.profileAvatarPhoto,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = alert.profileName + (if (alert.isChild) (if (isPersian) " (کودک)" else " (Child)") else (if (isPersian) " (بزرگسال)" else " (Adult)")),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        color = severityColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isPersian) alert.severity.titleFa else alert.severity.titleEn,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = severityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${alert.type.iconEmoji} ${alert.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                val datePrefix = if (isPersian) "موعد: ${jalaliDate.formatPersian()}" else "Due: ${alert.dueDate}"
                Text(
                    text = "$datePrefix • ${alert.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = severityColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ActiveProfileHeroCard(
    profile: Profile,
    isPersian: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                name = profile.name,
                avatarColor = profile.avatarColor,
                avatarPhoto = profile.avatarPhoto,
                size = 56.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isPersian) "عضو فعال" else "Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val jalali = profile.birthDate.toJalali()
                val birthText = if (isPersian) {
                    "متولد: ${jalali.formatPersian()}"
                } else {
                    "Born: ${profile.birthDate}"
                }
                val typeText = if (profile.type == ProfileType.CHILD) {
                    if (isPersian) "کودک" else "Child"
                } else {
                    if (isPersian) "بزرگسال" else "Adult"
                }
                Text(
                    text = "$typeText • $birthText",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun EmptyProfileHeroCard(
    isPersian: Boolean,
    onAddProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddProfile),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPersian) "افزودن اولین عضو خانواده" else "Add First Family Member",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPersian) "برای پایش واکسیناسیون، چک‌آپ و رشد" else "Start tracking vaccinations, checkups & growth",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenActiveChildPreview() {
    val sampleChild = Profile(
        id = "p1",
        name = "آرتین رضایی",
        birthDate = LocalDate(2023, 8, 20),
        gender = Gender.MALE,
        type = ProfileType.CHILD,
        avatarColor = 0xFF0A686D.toInt(),
        createdAt = 0L
    )
    val alert1 = FamilyAlertItem(
        id = "a1",
        profileId = "p1",
        profileName = "آرتین رضایی",
        profileAvatarColor = 0xFF0A686D.toInt(),
        isChild = true,
        title = "واکسن پنج‌گانه ۱ (پنتاوالان)",
        description = "موعد تزریق ۲ ماهگی فرا رسیده است",
        type = AlertType.VACCINE,
        severity = AlertSeverity.DUE_SOON,
        dueDate = LocalDate(2026, 9, 20),
        daysDifference = 4,
        actionTextFa = "ثبت تزریق",
        actionTextEn = "Record Vaccine"
    )
    val alert2 = FamilyAlertItem(
        id = "a2",
        profileId = "p2",
        profileName = "مریم حسینی",
        profileAvatarColor = 0xFF8E24AA.toInt(),
        isChild = false,
        title = "سنجش فشار خون",
        description = "۶ روز تأخیر در مراجعه",
        type = AlertType.CHECKUP,
        severity = AlertSeverity.OVERDUE,
        dueDate = LocalDate(2026, 9, 10),
        daysDifference = -6,
        actionTextFa = "ثبت چک‌آپ",
        actionTextEn = "Record Checkup"
    )

    SalamatTheme(isRtl = true) {
        HomeScreen(
            state = AppUiState(
                profiles = listOf(sampleChild),
                activeProfile = sampleChild,
                alerts = listOf(alert2, alert1),
                overdueCount = 1,
                dueSoonCount = 1,
                isPersian = true,
                isLoading = false
            ),
            onNavigateToFamily = {},
            onNavigateToQuickTools = {},
            onNavigateToAddProfile = {},
            onNavigateToMemberDetail = {}
        )
    }
}

@Preview
@Composable
private fun HomeScreenEmptyProfilePreview() {
    SalamatTheme(isRtl = true) {
        HomeScreen(
            state = AppUiState(
                profiles = emptyList(),
                activeProfile = null,
                alerts = emptyList(),
                overdueCount = 0,
                dueSoonCount = 0,
                isPersian = true,
                isLoading = false
            ),
            onNavigateToFamily = {},
            onNavigateToQuickTools = {},
            onNavigateToAddProfile = {},
            onNavigateToMemberDetail = {}
        )
    }
}

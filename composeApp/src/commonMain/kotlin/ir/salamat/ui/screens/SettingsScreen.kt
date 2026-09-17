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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import ir.salamat.core.notification.NotificationPreferences
import ir.salamat.core.ui.theme.SalamatTheme
import ir.salamat.ui.AppUiState

@Composable
fun SettingsScreen(
    state: AppUiState,
    onToggleLanguage: () -> Unit,
    onSetLanguage: (Boolean) -> Unit,
    onUpdateNotificationPreferences: ((NotificationPreferences) -> Unit)? = null,
    onCheckNotificationPermission: (() -> Unit)? = null,
    onRequestNotificationPermission: (() -> Unit)? = null,
    onSendTestNotification: (() -> Unit)? = null,
    onClearTestNotificationMessage: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        onCheckNotificationPermission?.invoke()
    }

    val prefs = state.notificationPreferences

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (state.isPersian) "تنظیمات" else "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 1. Language setting
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (state.isPersian) "زبان برنامه" else "App Language",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.isPersian) "زبان مورد نظر خود را انتخاب کنید:" else "Select your preferred language:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.isPersian,
                            onClick = { onSetLanguage(true) },
                            label = { Text("فارسی (پیش‌فرض)") }
                        )
                        FilterChip(
                            selected = !state.isPersian,
                            onClick = { onSetLanguage(false) },
                            label = { Text("English") }
                        )
                    }
                }
            }
        }

        // 2. Health Reminders & Notifications
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (state.isPersian) "یادآورها و اعلان‌های سلامت" else "Health Reminders & Alerts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (state.isPersian) "هشدارهای واکسن و چک‌آپ" else "Vaccine & checkup alerts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = prefs.enabled,
                            onCheckedChange = { checked ->
                                onUpdateNotificationPreferences?.invoke(prefs.copy(enabled = checked))
                            }
                        )
                    }

                    if (prefs.enabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Permission status
                        Surface(
                            color = if (state.hasNotificationPermission)
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (state.hasNotificationPermission)
                                        (if (state.isPersian) "✓ دسترسی اعلان‌های سیستم فعال است" else "✓ System notification permission active")
                                    else
                                        (if (state.isPersian) "⚠️ اعلان‌های سیستم در تنظیمات دستگاه غیرفعال است" else "⚠️ System notifications disabled in OS"),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (state.hasNotificationPermission)
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    else
                                        MaterialTheme.colorScheme.onErrorContainer
                                )

                                if (!state.hasNotificationPermission) {
                                    OutlinedButton(
                                        onClick = { onRequestNotificationPermission?.invoke() },
                                        contentPadding = ButtonDefaults.TextButtonContentPadding
                                    ) {
                                        Text(
                                            text = if (state.isPersian) "تنظیمات سیستم" else "Settings",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Timing
                        Text(
                            text = if (state.isPersian) "ساعت ارسال اعلان روزانه:" else "Preferred Daily Notification Time:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val timeSlots = listOf(8 to "۰۸:۰۰", 9 to "۰۹:۰۰", 10 to "۱۰:۰۰", 18 to "۱۸:۰۰", 20 to "۲۰:۰۰")
                            timeSlots.forEach { (hour, label) ->
                                val selected = prefs.preferredHour == hour
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        onUpdateNotificationPreferences?.invoke(prefs.copy(preferredHour = hour))
                                    },
                                    label = { Text(if (state.isPersian) label else "$hour:00") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Reminder interval checkboxes
                        Text(
                            text = if (state.isPersian) "فاصله زمانی هشدارهای موعد:" else "Reminder Alert Intervals:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        NotificationCheckboxRow(
                            title = if (state.isPersian) "۷ روز قبل از موعد (یادآور زودهنگام)" else "7 days before due date",
                            checked = prefs.notify7DaysBefore,
                            onCheckedChange = {
                                onUpdateNotificationPreferences?.invoke(prefs.copy(notify7DaysBefore = it))
                            }
                        )

                        NotificationCheckboxRow(
                            title = if (state.isPersian) "۱ روز قبل از موعد (یادآور نهایی)" else "1 day before due date",
                            checked = prefs.notify1DayBefore,
                            onCheckedChange = {
                                onUpdateNotificationPreferences?.invoke(prefs.copy(notify1DayBefore = it))
                            }
                        )

                        NotificationCheckboxRow(
                            title = if (state.isPersian) "در روز موعد نوبت" else "On the due date",
                            checked = prefs.notifyOnDueDate,
                            onCheckedChange = {
                                onUpdateNotificationPreferences?.invoke(prefs.copy(notifyOnDueDate = it))
                            }
                        )

                        NotificationCheckboxRow(
                            title = if (state.isPersian) "هشدار پیگیری نوبت‌های دارای تأخیر" else "Alert for overdue records",
                            checked = prefs.notifyWhenOverdue,
                            onCheckedChange = {
                                onUpdateNotificationPreferences?.invoke(prefs.copy(notifyWhenOverdue = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Test Notification Trigger
                        OutlinedButton(
                            onClick = { onSendTestNotification?.invoke() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (state.isPersian) "🔔 ارسال اعلان آزمایشی به دستگاه" else "🔔 Send Test Notification",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (state.testNotificationMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onClearTestNotificationMessage?.invoke() }
                            ) {
                                Text(
                                    text = state.testNotificationMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Calendar info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (state.isPersian) "تقویم رسمی" else "Calendar System",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (state.isPersian)
                            "محاسبات زمانی و یادآورهای واکسن بر مبنای گاه‌شماری هجری شمسی (جلالی) انجام می‌شود."
                        else
                            "Dates and reminders are natively calculated with Solar Hijri (Jalali) calendar support.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. Privacy & Storage
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (state.isPersian) "حریم خصوصی و ذخیره‌سازی" else "Privacy & Storage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (state.isPersian)
                            "تمامی اطلاعات پرونده‌ها و واکسن‌ها به صورت کاملاً محلی و امن روی دستگاه شما نگهداری می‌شوند."
                        else
                            "All records are stored locally and securely on your device (Offline-First).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. App info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (state.isPersian) "سلامت — نسخه ۱.۰.۰" else "Salamat — v1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NotificationCheckboxRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    SalamatTheme(isRtl = true) {
        SettingsScreen(
            state = AppUiState(isPersian = true),
            onToggleLanguage = {},
            onSetLanguage = {}
        )
    }
}

package ir.salamat.ui.screens.checkup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import ir.salamat.core.checkup.CheckupCatalog
import ir.salamat.core.datetime.JalaliDate
import ir.salamat.core.datetime.toJalali
import ir.salamat.core.datetime.toLocalDate
import ir.salamat.core.datetime.todayJalaliDate
import ir.salamat.core.datetime.todayLocalDate
import ir.salamat.core.ui.theme.SalamatTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCheckupDialog(
    isPersian: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (title: String, intervalMonths: Int, nextDueDate: LocalDate, notes: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedIntervalMonths by remember { mutableStateOf(12) }

    // Default due date: today + selectedIntervalMonths
    val defaultDueDate = remember(selectedIntervalMonths) {
        todayLocalDate().plus(selectedIntervalMonths, DateTimeUnit.MONTH).toJalali()
    }

    var yearStr by remember { mutableStateOf(defaultDueDate.year.toString()) }
    var monthStr by remember { mutableStateOf(defaultDueDate.month.toString()) }
    var dayStr by remember { mutableStateOf(defaultDueDate.day.toString()) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val intervalOptions = listOf(3, 6, 12, 24, 36)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (isPersian) "افزودن یادآور چک‌آپ جدید" else "Add New Checkup Reminder",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isPersian) "عنوان چک‌آپ یا آزمایش:" else "Checkup / Test Title:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isPersian) "نام چک‌آپ" else "Checkup Name") },
                    placeholder = {
                        Text(if (isPersian) "مثال: سنجش ویتامین D" else "e.g., Vitamin D Level")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Quick preset chips from catalog
                Text(
                    text = if (isPersian) "یا انتخاب از موارد استاندارد:" else "Or choose from standard presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CheckupCatalog.ALL_CHECKUPS.take(5).forEach { item ->
                        val itemTitle = if (isPersian) item.nameFa else item.nameEn
                        FilterChip(
                            selected = title == itemTitle,
                            onClick = {
                                title = itemTitle
                                selectedIntervalMonths = item.defaultIntervalMonths
                                val newDue = todayLocalDate().plus(item.defaultIntervalMonths, DateTimeUnit.MONTH).toJalali()
                                yearStr = newDue.year.toString()
                                monthStr = newDue.month.toString()
                                dayStr = newDue.day.toString()
                            },
                            label = {
                                Text(
                                    text = itemTitle,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Interval Selector
                Text(
                    text = if (isPersian) "دوره تکرار:" else "Repeat Interval:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    intervalOptions.forEach { months ->
                        FilterChip(
                            selected = selectedIntervalMonths == months,
                            onClick = {
                                selectedIntervalMonths = months
                                val newDue = todayLocalDate().plus(months, DateTimeUnit.MONTH).toJalali()
                                yearStr = newDue.year.toString()
                                monthStr = newDue.month.toString()
                                dayStr = newDue.day.toString()
                            },
                            label = {
                                Text(
                                    text = if (isPersian) "$months ماهه" else "${months}m",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // First Due Date (Solar Hijri)
                Text(
                    text = if (isPersian) "موعد اولین مراجعه (خورشیدی):" else "First Due Date (Solar Hijri):",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = yearStr,
                        onValueChange = { yearStr = it },
                        label = { Text(if (isPersian) "سال" else "Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = monthStr,
                        onValueChange = { monthStr = it },
                        label = { Text(if (isPersian) "ماه" else "Mo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dayStr,
                        onValueChange = { dayStr = it },
                        label = { Text(if (isPersian) "روز" else "Day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPersian) "توضیحات و یادداشت (اختیاری)" else "Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = if (isPersian) "لطفاً عنوان چک‌آپ را وارد کنید." else "Please enter a title."
                        return@Button
                    }
                    val parsedDate = try {
                        val y = yearStr.toIntOrNull()
                        val m = monthStr.toIntOrNull()
                        val d = dayStr.toIntOrNull()
                        if (y != null && m != null && d != null && m in 1..12 && d in 1..31) {
                            JalaliDate(y, m, d).toLocalDate()
                        } else null
                    } catch (_: Exception) {
                        null
                    }
                    if (parsedDate == null) {
                        errorMessage = if (isPersian) "لطفاً تاریخ معتبر وارد کنید." else "Please enter a valid date."
                        return@Button
                    }
                    onConfirm(title.trim(), selectedIntervalMonths, parsedDate, notes.ifBlank { null })
                }
            ) {
                Text(text = if (isPersian) "افزودن" else "Add Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = if (isPersian) "انصراف" else "Cancel")
            }
        }
    )
}

@Preview
@Composable
private fun AddCheckupDialogPreview() {
    SalamatTheme(isRtl = true) {
        AddCheckupDialog(
            isPersian = true,
            onDismiss = {},
            onConfirm = { _, _, _, _ -> }
        )
    }
}

package ir.salamat.ui.screens.checkup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import ir.salamat.core.datetime.JalaliDate
import ir.salamat.core.datetime.toJalali
import ir.salamat.core.datetime.toLocalDate
import ir.salamat.core.datetime.todayJalaliDate
import ir.salamat.core.ui.theme.SalamatTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

@Composable
fun CheckupRecordDialog(
    checkup: CheckupCardUiModel,
    isPersian: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (id: String, completedDate: LocalDate, nextDueDate: LocalDate, notes: String?) -> Unit
) {
    val initialJalali = todayJalaliDate()
    var yearStr by remember { mutableStateOf(initialJalali.year.toString()) }
    var monthStr by remember { mutableStateOf(initialJalali.month.toString()) }
    var dayStr by remember { mutableStateOf(initialJalali.day.toString()) }

    var notes by remember { mutableStateOf(checkup.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

    val calculatedNextDueDate = parsedDate?.plus(checkup.intervalMonths, DateTimeUnit.MONTH)
    val calculatedNextJalali = calculatedNextDueDate?.toJalali()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text = if (isPersian) "ثبت انجام چک‌آپ" else "Record Completed Checkup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = checkup.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (checkup.description.isNotBlank()) {
                    Text(
                        text = checkup.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = if (isPersian) "تاریخ انجام چک‌آپ (خورشیدی):" else "Completion Date (Solar Hijri):",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                // Date Fields: Year / Month / Day
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

                // Next Due Date Preview Card
                if (calculatedNextJalali != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isPersian) "موعد دوره بعدی:" else "Next Scheduled Date:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPersian) {
                                    "${calculatedNextJalali.formatPersian()} (${checkup.intervalMonths} ماه بعد)"
                                } else {
                                    "$calculatedNextDueDate (${checkup.intervalMonths} months later)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Notes input (results, clinic, doctor advice)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPersian) "نتایج آزمایش یا یادداشت (اختیاری)" else "Results or Notes (Optional)") },
                    placeholder = {
                        Text(
                            text = if (isPersian) "مثال: قند ناشتا ۹۲، فشار ۱۲ روی ۸" else "e.g., FBS 92, BP 120/80",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Error Message
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
                    if (parsedDate == null || calculatedNextDueDate == null) {
                        errorMessage = if (isPersian) "لطفاً تاریخ معتبر وارد کنید." else "Please enter a valid date."
                        return@Button
                    }
                    onConfirm(checkup.id, parsedDate, calculatedNextDueDate, notes.ifBlank { null })
                }
            ) {
                Text(text = if (isPersian) "ثبت انجام چک‌آپ" else "Confirm Completion")
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
private fun CheckupRecordDialogPreview() {
    SalamatTheme(isRtl = true) {
        CheckupRecordDialog(
            checkup = CheckupCardUiModel(
                id = "bp_1",
                profileId = "p1",
                titleKey = "blood_pressure",
                title = "سنجش فشار خون",
                description = "غربالگری پرفشاری خون و ارزیابی سلامت قلب و عروق.",
                intervalMonths = 6,
                lastCompletedDate = null,
                nextDueDate = LocalDate(2026, 10, 15),
                status = CheckupStatus.DUE,
                daysUntilDue = 15,
                notes = null
            ),
            isPersian = true,
            onDismiss = {},
            onConfirm = { _, _, _, _ -> }
        )
    }
}

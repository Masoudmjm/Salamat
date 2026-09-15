package ir.salamat.ui.screens.growth

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
import ir.salamat.core.datetime.toLocalDate
import ir.salamat.core.datetime.todayJalaliDate
import ir.salamat.core.ui.theme.SalamatTheme
import kotlinx.datetime.LocalDate

@Composable
fun GrowthRecordDialog(
    isPersian: Boolean,
    isChild: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (date: LocalDate, weightKg: Double, heightCm: Double, headCircumferenceCm: Double?, notes: String?) -> Unit
) {
    val initialJalali = todayJalaliDate()
    var yearStr by remember { mutableStateOf(initialJalali.year.toString()) }
    var monthStr by remember { mutableStateOf(initialJalali.month.toString()) }
    var dayStr by remember { mutableStateOf(initialJalali.day.toString()) }

    var weightStr by remember { mutableStateOf("") }
    var heightStr by remember { mutableStateOf("") }
    var headCircumferenceStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val parsedWeight = weightStr.toDoubleOrNull()
    val parsedHeight = heightStr.toDoubleOrNull()
    val liveBmi = if (parsedWeight != null && parsedHeight != null && parsedWeight > 0 && parsedHeight > 0) {
        BmiCalculator.calculateBmi(parsedWeight, parsedHeight)
    } else null
    val liveBmiCategory = liveBmi?.let { BmiCalculator.getBmiCategory(it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (isPersian) "ثبت اندازه‌گیری جدید" else "New Measurement",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isPersian) "تاریخ ثبت (خورشیدی):" else "Date (Solar Hijri):",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                // Date Picker Fields (Year, Month, Day)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = yearStr,
                        onValueChange = { yearStr = it },
                        label = { Text(if (isPersian) "سال" else "Year") },
                        modifier = Modifier.weight(1.3f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = monthStr,
                        onValueChange = { monthStr = it },
                        label = { Text(if (isPersian) "ماه" else "Month") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dayStr,
                        onValueChange = { dayStr = it },
                        label = { Text(if (isPersian) "روز" else "Day") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Weight & Height
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text(if (isPersian) "وزن (کیلوگرم)" else "Weight (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = heightStr,
                        onValueChange = { heightStr = it },
                        label = { Text(if (isPersian) "قد (سانتی‌متر)" else "Height (cm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                // Head Circumference for Children
                if (isChild) {
                    OutlinedTextField(
                        value = headCircumferenceStr,
                        onValueChange = { headCircumferenceStr = it },
                        label = { Text(if (isPersian) "دور سر (سانتی‌متر — اختیاری)" else "Head Circ. (cm — optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                // Live BMI Preview Card
                if (liveBmi != null && liveBmiCategory != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(liveBmiCategory.colorHex()).copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isPersian) "شاخص توده بدنی (BMI): $liveBmi" else "Calculated BMI: $liveBmi",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(liveBmiCategory.colorHex())
                            )
                            Text(
                                text = if (isPersian) liveBmiCategory.titleFa() else liveBmiCategory.titleEn(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(liveBmiCategory.colorHex())
                            )
                        }
                    }
                }

                // Optional Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPersian) "یادداشت (اختیاری)" else "Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val y = yearStr.toIntOrNull()
                    val m = monthStr.toIntOrNull()
                    val d = dayStr.toIntOrNull()
                    if (y == null || m == null || d == null || m !in 1..12 || d !in 1..31) {
                        errorMessage = if (isPersian) "لطفاً تاریخ معتبر وارد نمایید." else "Invalid date format."
                        return@Button
                    }
                    val weight = weightStr.toDoubleOrNull()
                    if (weight == null || weight <= 0) {
                        errorMessage = if (isPersian) "لطفاً وزن معتبر وارد نمایید." else "Please enter a valid weight."
                        return@Button
                    }
                    val height = heightStr.toDoubleOrNull()
                    if (height == null || height <= 0) {
                        errorMessage = if (isPersian) "لطفاً قد معتبر وارد نمایید." else "Please enter a valid height."
                        return@Button
                    }
                    val headCirc = headCircumferenceStr.toDoubleOrNull()
                    val date = JalaliDate(y, m, d).toLocalDate()

                    onConfirm(date, weight, height, headCirc, notes.trim().ifEmpty { null })
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isPersian) "ثبت اندازه‌گیری" else "Save Measurement")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isPersian) "انصراف" else "Cancel")
            }
        }
    )
}

@Preview
@Composable
private fun GrowthRecordDialogPreview() {
    SalamatTheme(isRtl = true) {
        GrowthRecordDialog(
            isPersian = true,
            isChild = true,
            onDismiss = {},
            onConfirm = { _, _, _, _, _ -> }
        )
    }
}

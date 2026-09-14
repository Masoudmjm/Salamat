package ir.salamat.ui.screens.vaccine

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import ir.salamat.core.datetime.JalaliDate
import ir.salamat.core.datetime.toLocalDate
import ir.salamat.core.datetime.todayJalaliDate
import ir.salamat.core.model.VaccineRecord
import ir.salamat.core.model.VaccineStatus
import ir.salamat.core.ui.theme.SalamatTheme
import ir.salamat.core.vaccine.IranVaccineSchedule
import kotlinx.datetime.LocalDate
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun VaccineRecordDialog(
    vaccine: VaccineCardUiModel,
    isPersian: Boolean,
    onDismiss: () -> Unit,
    onConfirmAdministered: (recordId: String, date: LocalDate, notes: String?) -> Unit,
    onMarkPending: (recordId: String) -> Unit
) {
    val initialJalali = vaccine.administeredJalali ?: todayJalaliDate()
    var yearStr by remember { mutableStateOf(initialJalali.year.toString()) }
    var monthStr by remember { mutableStateOf(initialJalali.month.toString()) }
    var dayStr by remember { mutableStateOf(initialJalali.day.toString()) }
    var notes by remember { mutableStateOf(vaccine.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isAlreadyCompleted = vaccine.status == VaccineStatus.COMPLETED

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text = if (isPersian) vaccine.definition.nameFa else vaccine.definition.nameEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPersian)
                        vaccine.definition.descriptionFa
                    else
                        vaccine.definition.descriptionEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isPersian) "تاریخ تزریق واکسن (شمسی):" else "Administration Date (Solar Hijri):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = dayStr,
                        onValueChange = { if (it.length <= 2) dayStr = it },
                        label = { Text(if (isPersian) "روز" else "Day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = monthStr,
                        onValueChange = { if (it.length <= 2) monthStr = it },
                        label = { Text(if (isPersian) "ماه" else "Month") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = yearStr,
                        onValueChange = { if (it.length <= 4) yearStr = it },
                        label = { Text(if (isPersian) "سال" else "Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.4f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isPersian) "توضیحات و یادداشت (اختیاری)" else "Notes (Optional)") },
                    placeholder = { Text(if (isPersian) "مرکز بهداشت، واکنش و تب..." else "Health clinic, symptoms...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 3
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (isAlreadyCompleted) {
                    OutlinedButton(
                        onClick = { onMarkPending(vaccine.record.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isPersian) "لغو ثبت واکسن (بازگشت به نوبت)" else "Revert to Pending")
                    }
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
                    val date = JalaliDate(y, m, d).toLocalDate()
                    onConfirmAdministered(vaccine.record.id, date, notes.trim().ifEmpty { null })
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isPersian) "ثبت تزریق" else "Save")
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
private fun VaccineRecordDialogPreview() {
    val def = IranVaccineSchedule.getByCode("BCG")!!
    val dummyCard = VaccineCardUiModel(
        record = VaccineRecord(
            id = "vr_1",
            profileId = "p1",
            vaccineCode = def.code,
            targetAgeMonths = 0,
            status = VaccineStatus.DUE,
            administeredDate = null,
            notes = null,
            createdAt = 0L
        ),
        definition = def,
        status = VaccineStatus.DUE,
        administeredDate = null,
        administeredJalali = null,
        notes = null
    )
    SalamatTheme(isRtl = true) {
        VaccineRecordDialog(
            vaccine = dummyCard,
            isPersian = true,
            onDismiss = {},
            onConfirmAdministered = { _, _, _ -> },
            onMarkPending = {}
        )
    }
}

package ir.salamat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import ir.salamat.core.vaccine.IranVaccineSchedule
import ir.salamat.ui.AppUiState

@Composable
fun QuickToolsScreen(
    state: AppUiState,
    modifier: Modifier = Modifier
) {
    var heightInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }
    var calculatedBmi by remember { mutableStateOf<Double?>(null) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (state.isPersian) "ابزارهای سلامت" else "Health Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Tool 1: BMI Calculator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isPersian) "محاسبه شاخص توده بدنی (BMI)" else "BMI Calculator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = { Text(if (state.isPersian) "قد (سانتی‌متر)" else "Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text(if (state.isPersian) "وزن (کیلوگرم)" else "Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val h = heightInput.toDoubleOrNull()
                            val w = weightInput.toDoubleOrNull()
                            if (h != null && w != null && h > 0) {
                                val hMeter = h / 100.0
                                val bmi = w / (hMeter * hMeter)
                                calculatedBmi = (bmi * 10).toInt() / 10.0
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (state.isPersian) "محاسبه BMI" else "Calculate BMI")
                    }

                    if (calculatedBmi != null) {
                        val bmi = calculatedBmi!!
                        val (categoryFa, categoryEn, categoryColor) = when {
                            bmi < 18.5 -> Triple("کمبود وزن", "Underweight", Color(0xFFE76F51))
                            bmi < 25.0 -> Triple("وزن طبیعی و ایده‌آل", "Normal weight", Color(0xFF0A686D))
                            bmi < 30.0 -> Triple("اضافه وزن", "Overweight", Color(0xFFF4A261))
                            else -> Triple("چاقی", "Obesity", Color(0xFFD90429))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "${if (state.isPersian) "شاخص توده بدنی:" else "BMI:"} $bmi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = categoryColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${if (state.isPersian) "وضعیت:" else "Category:"} ${if (state.isPersian) categoryFa else categoryEn}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = categoryColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tool 2: Iran Vaccine Schedule Guide
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showScheduleDialog = !showScheduleDialog },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isPersian) "جدول واکسیناسیون کشوری ایران" else "Iran National Vaccine Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.isPersian)
                            "تقویم رسمی ایمن‌سازی کودکان از بدو تولد تا ۶ سالگی بر اساس آخرین دستورالعمل وزارت بهداشت."
                        else
                            "Official childhood immunization schedule from birth to 6 years according to Iran Ministry of Health.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (showScheduleDialog) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (def in IranVaccineSchedule.ALL_VACCINES) {
                                VaccineScheduleItem(def = def, isPersian = state.isPersian)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VaccineScheduleItem(
    def: ir.salamat.core.vaccine.VaccineDefinition,
    isPersian: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (isPersian) def.nameFa else def.nameEn,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            val ageText = when (def.targetAgeMonths) {
                0 -> if (isPersian) "بدو تولد" else "At birth"
                in 1..11 -> if (isPersian) "${def.targetAgeMonths} ماهگی" else "${def.targetAgeMonths} months"
                else -> {
                    val years = def.targetAgeMonths / 12
                    if (isPersian) "$years سالگی" else "$years years"
                }
            }
            Text(
                text = ageText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

package ir.salamat.ui.screens.growth

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import ir.salamat.core.datetime.toJalali
import ir.salamat.core.model.Gender
import ir.salamat.core.model.GrowthRecord
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.ui.theme.SalamatTheme
import kotlinx.datetime.LocalDate

@Composable
fun GrowthTrackerView(
    state: GrowthUiState,
    isPersian: Boolean,
    onOpenAddDialog: () -> Unit,
    onDeleteRecord: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary & Evaluation Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            GrowthSummaryCard(
                state = state,
                isPersian = isPersian,
                onOpenAddDialog = onOpenAddDialog
            )
        }

        // Section Title & Add Action
        if (state.records.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isPersian) "تاریخچه اندازه‌گیری‌ها" else "Measurement History",
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
                        Text(text = if (isPersian) "ثبت جدید" else "New Entry")
                    }
                }
            }

            items(state.records, key = { it.id }) { record ->
                GrowthRecordCard(
                    record = record,
                    isPersian = isPersian,
                    onDelete = { onDeleteRecord(record.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun GrowthSummaryCard(
    state: GrowthUiState,
    isPersian: Boolean,
    onOpenAddDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latest = state.latestRecord
    val category = state.bmiCategory

    if (latest != null && category != null) {
        val categoryColor = Color(category.colorHex())

        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isPersian) "شاخص توده بدنی (BMI)" else "Body Mass Index (BMI)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${latest.bmi}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = categoryColor
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = categoryColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = if (isPersian) category.titleFa() else category.titleEn(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Metrics Row (Weight, Height, Head Circ)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MetricColumn(
                        title = if (isPersian) "وزن" else "Weight",
                        value = if (isPersian) "${latest.weightKg} ک.گ" else "${latest.weightKg} kg"
                    )
                    MetricColumn(
                        title = if (isPersian) "قد" else "Height",
                        value = if (isPersian) "${latest.heightCm} س.م" else "${latest.heightCm} cm"
                    )
                    if (latest.headCircumferenceCm != null && latest.headCircumferenceCm > 0) {
                        MetricColumn(
                            title = if (isPersian) "دور سر" else "Head Circ.",
                            value = if (isPersian) "${latest.headCircumferenceCm} س.م" else "${latest.headCircumferenceCm} cm"
                        )
                    }
                }

                // Ideal Weight Range
                val range = state.idealWeightRange
                if (range != null && range.first > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isPersian)
                            "محدوده وزن استاندارد برای این قد: ${range.first} تا ${range.second} کیلوگرم"
                        else
                            "Standard weight range for this height: ${range.first} - ${range.second} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Recommendation
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isPersian) category.recommendationFa() else category.recommendationEn(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(10.dp))
                val jalali = latest.date.toJalali()
                Text(
                    text = if (isPersian)
                        "آخرین پایش: ${jalali.formatPersian()}"
                    else
                        "Last measured: ${latest.date}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        // Empty State Hero Card
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isPersian) "هنوز اندازه‌گیری قدی یا وزنی ثبت نشده است" else "No growth records yet",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isPersian)
                        "با ثبت قد و وزن، شاخص توده بدنی (BMI) و محدوده سلامت وزن به طور خودکار محاسبه و نمودار پایش رشد تشکیل می‌شود."
                    else
                        "Record height and weight to calculate BMI and track growth trends over time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onOpenAddDialog,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPersian) "ثبت اولین اندازه‌گیری" else "Record First Measurement")
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(
    title: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun GrowthRecordCard(
    record: GrowthRecord,
    isPersian: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = BmiCalculator.getBmiCategory(record.bmi)
    val categoryColor = Color(category.colorHex())
    val jalali = record.date.toJalali()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPersian) jalali.formatPersian() else record.date.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (isPersian) category.titleFa() else category.titleEn(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val metricsText = buildString {
                    append(if (isPersian) "وزن: ${record.weightKg} ک.گ" else "Weight: ${record.weightKg} kg")
                    append(" • ")
                    append(if (isPersian) "قد: ${record.heightCm} س.م" else "Height: ${record.heightCm} cm")
                    if (record.headCircumferenceCm != null && record.headCircumferenceCm > 0) {
                        append(" • ")
                        append(if (isPersian) "دور سر: ${record.headCircumferenceCm} س.م" else "Head: ${record.headCircumferenceCm} cm")
                    }
                    append(" • ")
                    append("BMI: ${record.bmi}")
                }
                Text(
                    text = metricsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!record.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun GrowthTrackerViewPreview() {
    val sampleProfile = Profile(
        id = "p1",
        name = "سارا احمدی",
        birthDate = LocalDate(2023, 5, 10),
        gender = Gender.FEMALE,
        type = ProfileType.CHILD,
        avatarColor = 0xFFE76F51.toInt(),
        createdAt = 0L
    )
    val r1 = GrowthRecord(
        id = "r1",
        profileId = "p1",
        date = LocalDate(2024, 3, 15),
        weightKg = 11.2,
        heightCm = 82.0,
        headCircumferenceCm = 46.5,
        bmi = 16.7,
        notes = "چک‌آپ ۱۰ ماهگی در مرکز بهداشت",
        createdAt = 0L
    )
    val r2 = GrowthRecord(
        id = "r2",
        profileId = "p1",
        date = LocalDate(2024, 1, 10),
        weightKg = 9.8,
        heightCm = 76.0,
        headCircumferenceCm = 45.0,
        bmi = 17.0,
        notes = null,
        createdAt = 0L
    )
    val state = GrowthUiState(
        profile = sampleProfile,
        records = listOf(r1, r2),
        latestRecord = r1,
        bmiCategory = BmiCategory.NORMAL,
        idealWeightRange = Pair(12.4, 16.7),
        isAddDialogOpen = false,
        isLoading = false
    )

    SalamatTheme(isRtl = true) {
        GrowthTrackerView(
            state = state,
            isPersian = true,
            onOpenAddDialog = {},
            onDeleteRecord = {}
        )
    }
}

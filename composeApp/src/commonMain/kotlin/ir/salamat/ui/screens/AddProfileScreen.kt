package ir.salamat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import ir.salamat.core.ui.image.rememberImagePicker
import ir.salamat.ui.components.ProfileAvatar
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.salamat.core.datetime.JalaliDate
import ir.salamat.core.datetime.toJalali
import ir.salamat.core.datetime.toLocalDate
import ir.salamat.core.model.Gender
import ir.salamat.core.model.Profile
import ir.salamat.core.model.ProfileType
import ir.salamat.core.ui.theme.SalamatTheme
import kotlinx.datetime.LocalDate
import androidx.compose.ui.tooling.preview.Preview

private val AVATAR_COLORS = listOf(
    0xFF0A686D.toInt(), // Teal
    0xFFE76F51.toInt(), // Coral
    0xFF264653.toInt(), // Deep Navy
    0xFF2A9D8F.toInt(), // Mint Green
    0xFFE9C46A.toInt(), // Warm Amber
    0xFF8338EC.toInt()  // Soft Purple
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileScreen(
    isPersian: Boolean,
    initialProfile: Profile? = null,
    onBack: () -> Unit,
    onSaveProfile: (
        name: String,
        birthDate: LocalDate,
        gender: Gender,
        type: ProfileType,
        avatarColor: Int,
        avatarPhoto: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEdit = initialProfile != null
    val initialJalali = initialProfile?.birthDate?.toJalali()

    var name by remember(initialProfile) { mutableStateOf(initialProfile?.name ?: "") }
    var yearStr by remember(initialProfile) { mutableStateOf(initialJalali?.year?.toString() ?: "1402") }
    var monthStr by remember(initialProfile) { mutableStateOf(initialJalali?.month?.toString() ?: "1") }
    var dayStr by remember(initialProfile) { mutableStateOf(initialJalali?.day?.toString() ?: "1") }
    var selectedGender by remember(initialProfile) { mutableStateOf(initialProfile?.gender ?: Gender.MALE) }
    var selectedType by remember(initialProfile) { mutableStateOf(initialProfile?.type ?: ProfileType.CHILD) }
    var selectedColor by remember(initialProfile) { mutableStateOf(initialProfile?.avatarColor ?: AVATAR_COLORS[0]) }
    var selectedPhoto by remember(initialProfile) { mutableStateOf(initialProfile?.avatarPhoto) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pickImage = rememberImagePicker { base64 ->
        if (base64 != null) {
            selectedPhoto = base64
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEdit) {
                            if (isPersian) "ویرایش عضو خانواده" else "Edit Family Member"
                        } else {
                            if (isPersian) "ثبت عضو جدید" else "Add New Member"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo & Avatar Picker Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        ProfileAvatar(
                            name = name.ifBlank { "؟" },
                            avatarColor = selectedColor,
                            avatarPhoto = selectedPhoto,
                            size = 104.dp,
                            modifier = Modifier.clickable { pickImage() }
                        )
                        Surface(
                            onClick = { pickImage() },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = if (isPersian) "تغییر تصویر" else "Change Photo",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { pickImage() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (selectedPhoto != null) {
                                    if (isPersian) "تغییر تصویر" else "Change Photo"
                                } else {
                                    if (isPersian) "انتخاب تصویر نمایه" else "Pick Profile Photo"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (selectedPhoto != null) {
                            OutlinedButton(
                                onClick = { selectedPhoto = null },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(
                                    text = if (isPersian) "حذف تصویر" else "Remove Photo",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text(if (isPersian) "نام و نام خانوادگی *" else "Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Type
            item {
                Text(
                    text = if (isPersian) "نوع عضو:" else "Member Type:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == ProfileType.CHILD,
                        onClick = { selectedType = ProfileType.CHILD },
                        label = { Text(if (isPersian) "کودک (پایش واکسن و رشد)" else "Child (Vaccines & Growth)") }
                    )
                    FilterChip(
                        selected = selectedType == ProfileType.ADULT,
                        onClick = { selectedType = ProfileType.ADULT },
                        label = { Text(if (isPersian) "بزرگسال" else "Adult") }
                    )
                }
            }

            // Gender
            item {
                Text(
                    text = if (isPersian) "جنسیت:" else "Gender:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedGender == Gender.MALE,
                        onClick = { selectedGender = Gender.MALE },
                        label = { Text(if (isPersian) "پسر / آقا" else "Male") }
                    )
                    FilterChip(
                        selected = selectedGender == Gender.FEMALE,
                        onClick = { selectedGender = Gender.FEMALE },
                        label = { Text(if (isPersian) "دختر / خانم" else "Female") }
                    )
                }
            }

            // Jalali Birth Date
            item {
                Text(
                    text = if (isPersian) "تاریخ تولد شمسی:" else "Birth Date (Solar Hijri):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        label = { Text(if (isPersian) "سال (مثلاً ۱۴۰۲)" else "Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }

            // Avatar Color
            item {
                Text(
                    text = if (isPersian) "رنگ نماد:" else "Avatar Color:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AVATAR_COLORS.forEach { colorInt ->
                        val isSelected = selectedColor == colorInt
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .clickable { selectedColor = colorInt }
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Error display
            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (name.trim().isEmpty()) {
                            errorMessage = if (isPersian) "لطفاً نام را وارد کنید." else "Please enter a name."
                            return@Button
                        }
                        val y = yearStr.toIntOrNull()
                        val m = monthStr.toIntOrNull()
                        val d = dayStr.toIntOrNull()
                        if (y == null || m == null || d == null || m !in 1..12 || d !in 1..31) {
                            errorMessage = if (isPersian) "لطفاً تاریخ تولد معتبر وارد کنید." else "Please enter a valid birth date."
                            return@Button
                        }

                        val jalali = JalaliDate(y, m, d)
                        val gregorian = jalali.toLocalDate()

                        onSaveProfile(
                            name.trim(),
                            gregorian,
                            selectedGender,
                            selectedType,
                            selectedColor,
                            selectedPhoto
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (isEdit) {
                            if (isPersian) "ثبت تغییرات پرونده" else "Save Changes"
                        } else {
                            if (isPersian) "ذخیره و ایجاد پرونده" else "Save & Create Profile"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview
@Composable
private fun AddProfileScreenPreview() {
    SalamatTheme(isRtl = true) {
        AddProfileScreen(
            isPersian = true,
            onBack = {},
            onSaveProfile = { _, _, _, _, _, _ -> }
        )
    }
}

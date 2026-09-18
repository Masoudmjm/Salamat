package ir.salamat.core.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
enum class Gender {
    MALE,
    FEMALE
}

@Serializable
enum class ProfileType {
    CHILD,
    ADULT
}

@Serializable
data class Profile(
    val id: String,
    val name: String,
    val birthDate: LocalDate,
    val gender: Gender,
    val type: ProfileType,
    val avatarColor: Int,
    val avatarPhoto: String? = null,
    val createdAt: Long
)

@Serializable
enum class VaccineStatus {
    UPCOMING,
    DUE,
    COMPLETED,
    OVERDUE
}

@Serializable
data class VaccineRecord(
    val id: String,
    val profileId: String,
    val vaccineCode: String,
    val targetAgeMonths: Int,
    val status: VaccineStatus,
    val administeredDate: LocalDate?,
    val notes: String?,
    val createdAt: Long
)

@Serializable
data class GrowthRecord(
    val id: String,
    val profileId: String,
    val date: LocalDate,
    val weightKg: Double,
    val heightCm: Double,
    val headCircumferenceCm: Double?,
    val bmi: Double,
    val notes: String?,
    val createdAt: Long
)

@Serializable
data class CheckupReminder(
    val id: String,
    val profileId: String,
    val titleKey: String,
    val intervalMonths: Int,
    val lastCompletedDate: LocalDate?,
    val nextDueDate: LocalDate,
    val notes: String?,
    val createdAt: Long
)

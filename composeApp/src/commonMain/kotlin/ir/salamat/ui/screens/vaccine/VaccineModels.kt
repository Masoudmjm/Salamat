package ir.salamat.ui.screens.vaccine

import ir.salamat.core.datetime.JalaliDate
import ir.salamat.core.datetime.toJalali
import ir.salamat.core.model.VaccineRecord
import ir.salamat.core.model.VaccineStatus
import ir.salamat.core.vaccine.IranVaccineSchedule
import ir.salamat.core.vaccine.VaccineDefinition
import kotlinx.datetime.LocalDate

enum class MilestoneStatus {
    ALL_COMPLETED,
    OVERDUE,
    DUE_NOW,
    UPCOMING
}

data class VaccineCardUiModel(
    val record: VaccineRecord,
    val definition: VaccineDefinition,
    val status: VaccineStatus,
    val administeredDate: LocalDate?,
    val administeredJalali: JalaliDate?,
    val notes: String?
)

data class VaccineMilestoneUiModel(
    val targetAgeMonths: Int,
    val titleFa: String,
    val titleEn: String,
    val dueDate: LocalDate,
    val jalaliDueDate: JalaliDate,
    val status: MilestoneStatus,
    val vaccines: List<VaccineCardUiModel>
)

object VaccineUiMapper {

    fun getMilestoneTitleFa(months: Int): String = when (months) {
        0 -> "بدو تولد"
        2 -> "۲ ماهگی"
        4 -> "۴ ماهگی"
        6 -> "۶ ماهگی"
        12 -> "۱۲ ماهگی (۱ سالگی)"
        18 -> "۱۸ ماهگی (۱.۵ سالگی)"
        72 -> "۶ سالگی (قبل از مدرسه)"
        else -> "$months ماهگی"
    }

    fun getMilestoneTitleEn(months: Int): String = when (months) {
        0 -> "At Birth"
        2 -> "2 Months"
        4 -> "4 Months"
        6 -> "6 Months"
        12 -> "12 Months (1 Year)"
        18 -> "18 Months (1.5 Years)"
        72 -> "6 Years (Pre-School)"
        else -> "$months Months"
    }

    fun mapToMilestones(
        birthDate: LocalDate,
        records: List<VaccineRecord>,
        currentDate: LocalDate
    ): List<VaccineMilestoneUiModel> {
        val grouped = records.groupBy { it.targetAgeMonths }
        val milestonesOrder = listOf(0, 2, 4, 6, 12, 18, 72)

        return milestonesOrder.mapNotNull { months ->
            val list = grouped[months] ?: return@mapNotNull null
            val dueDate = IranVaccineSchedule.calculateDueDate(birthDate, months)
            val jalaliDueDate = dueDate.toJalali()

            val vaccineCards = list.map { rec ->
                val def = IranVaccineSchedule.getByCode(rec.vaccineCode) ?: VaccineDefinition(
                    code = rec.vaccineCode,
                    nameFa = rec.vaccineCode,
                    nameEn = rec.vaccineCode,
                    targetAgeMonths = months,
                    descriptionFa = "",
                    descriptionEn = ""
                )
                val status = if (rec.administeredDate != null) {
                    VaccineStatus.COMPLETED
                } else {
                    IranVaccineSchedule.computeStatus(
                        targetAgeMonths = months,
                        birthDate = birthDate,
                        administeredDate = null,
                        currentDate = currentDate
                    )
                }

                VaccineCardUiModel(
                    record = rec,
                    definition = def,
                    status = status,
                    administeredDate = rec.administeredDate,
                    administeredJalali = rec.administeredDate?.toJalali(),
                    notes = rec.notes
                )
            }

            val milestoneStatus = when {
                vaccineCards.all { it.status == VaccineStatus.COMPLETED } -> MilestoneStatus.ALL_COMPLETED
                vaccineCards.any { it.status == VaccineStatus.OVERDUE } -> MilestoneStatus.OVERDUE
                vaccineCards.any { it.status == VaccineStatus.DUE } -> MilestoneStatus.DUE_NOW
                else -> MilestoneStatus.UPCOMING
            }

            VaccineMilestoneUiModel(
                targetAgeMonths = months,
                titleFa = getMilestoneTitleFa(months),
                titleEn = getMilestoneTitleEn(months),
                dueDate = dueDate,
                jalaliDueDate = jalaliDueDate,
                status = milestoneStatus,
                vaccines = vaccineCards
            )
        }
    }
}

package ir.behpay.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ir.behpay.data.repository.CheckupRepository
import ir.behpay.data.repository.ProfileRepository
import ir.behpay.data.repository.VaccineRepository
import ir.behpay.di.initKoinAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.context.GlobalContext

class BehpayBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                try {
                    initKoinAndroid(context.applicationContext)
                } catch (_: Exception) {
                    // Already initialized
                }

                val koin = GlobalContext.get()
                val profileRepo: ProfileRepository = koin.get()
                val vaccineRepo: VaccineRepository = koin.get()
                val checkupRepo: CheckupRepository = koin.get()
                val syncEngine: ReminderSyncEngine = koin.get()

                val profiles = profileRepo.getAllProfiles().firstOrNull() ?: emptyList()
                val pendingVaccines = vaccineRepo.getAllPendingVaccines().firstOrNull() ?: emptyList()
                val checkups = checkupRepo.getAllCheckups().firstOrNull() ?: emptyList()

                val now = Clock.System.now()
                val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

                syncEngine.sync(
                    profiles = profiles,
                    pendingVaccines = pendingVaccines,
                    checkups = checkups,
                    preferences = NotificationPreferences(
                        enabled = true,
                        notify7DaysBefore = true,
                        notify1DayBefore = true,
                        notifyOnDueDate = true,
                        notifyWhenOverdue = true
                    ),
                    currentDate = today,
                    isPersian = true
                )
            } catch (_: Exception) {
                // Ignore background errors on boot
            } finally {
                pendingResult.finish()
            }
        }
    }
}

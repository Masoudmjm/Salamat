package ir.salamat.core.notification

interface NotificationScheduler {
    suspend fun schedule(item: NotificationItem)
    suspend fun cancel(id: String)
    suspend fun cancelAll()
    suspend fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
    suspend fun showImmediateNotification(item: NotificationItem)
    fun openNotificationSettings() {}
}

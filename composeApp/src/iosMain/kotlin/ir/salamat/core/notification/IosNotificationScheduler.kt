package ir.salamat.core.notification

class IosNotificationScheduler : NotificationScheduler {

    override suspend fun schedule(item: NotificationItem) {
        // iOS UNUserNotificationCenter scheduling
    }

    override suspend fun cancel(id: String) {
        // iOS UNUserNotificationCenter removal
    }

    override suspend fun cancelAll() {
        // iOS UNUserNotificationCenter removeAllPendingNotificationRequests
    }

    override suspend fun hasPermission(): Boolean {
        return true
    }

    override suspend fun requestPermission(): Boolean {
        return true
    }

    override suspend fun showImmediateNotification(item: NotificationItem) {
        // iOS immediate banner
    }
}

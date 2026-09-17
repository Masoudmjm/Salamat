package ir.salamat.core.notification

class JsNotificationScheduler : NotificationScheduler {

    override suspend fun schedule(item: NotificationItem) {
        // Web scheduling (can use Web Notifications or service workers)
    }

    override suspend fun cancel(id: String) {
        // Web notification cancel
    }

    override suspend fun cancelAll() {
        // Web notification cancelAll
    }

    override suspend fun hasPermission(): Boolean {
        return true
    }

    override suspend fun requestPermission(): Boolean {
        return true
    }

    override suspend fun showImmediateNotification(item: NotificationItem) {
        // Web immediate notification
    }
}

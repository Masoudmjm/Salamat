package ir.behpay.core.notification

import android.app.Activity
import java.lang.ref.WeakReference

object AndroidNotificationPermissionRequester {
    private var activityRef: WeakReference<Activity>? = null
    private var promptCallback: (() -> Unit)? = null

    fun register(activity: Activity, onPromptRequested: () -> Unit) {
        activityRef = WeakReference(activity)
        promptCallback = onPromptRequested
    }

    fun unregister() {
        activityRef = null
        promptCallback = null
    }

    fun requestPrompt(): Boolean {
        val callback = promptCallback
        val activity = activityRef?.get()
        if (callback != null && activity != null && !activity.isFinishing && !activity.isDestroyed) {
            activity.runOnUiThread { callback.invoke() }
            return true
        }
        return false
    }
}

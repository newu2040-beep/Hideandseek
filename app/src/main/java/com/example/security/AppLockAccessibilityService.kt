package com.example.security

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class AppLockAccessibilityService : AccessibilityService() {

    private lateinit var securityManager: VaultSecurityManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        securityManager = VaultSecurityManager(this)
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        this.serviceInfo = info
        Log.d("AppLockService", "Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Check if the package is in the locked list
            val lockedApps = securityManager.getLockedApps()
            if (lockedApps.contains(packageName)) {
                // Ignore if it's our own app
                if (packageName == this.packageName) return
                
                // Block the app by launching our LockScreen overlay or returning to home
                Log.d("AppLockService", "Locked app launched: $packageName")
                val intent = Intent(this, com.example.MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("LOCKED_APP_PACKAGE", packageName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Required method
    }
}

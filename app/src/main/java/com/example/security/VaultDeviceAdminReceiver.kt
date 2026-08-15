package com.example.security

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class VaultDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        val securityManager = VaultSecurityManager(context)
        securityManager.isDeviceAdminFrictionEnabled = true
        Toast.makeText(context, "HIDEANDSEEK Vault Protection Activated", Toast.LENGTH_SHORT).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Disabling Device Protection makes your hidden vault vulnerable to removal."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        val securityManager = VaultSecurityManager(context)
        securityManager.isDeviceAdminFrictionEnabled = false
        Toast.makeText(context, "HIDEANDSEEK Protection Deactivated", Toast.LENGTH_SHORT).show()
    }
}

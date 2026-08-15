package com.example.security

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.R

data class DisguiseOption(
    val id: String,
    val name: String,
    val aliasClassName: String,
    val iconResId: Int,
    val isDefault: Boolean = false
)

class DisguiseModeManager(private val context: Context) {

    private val packageManager = context.packageManager
    private val packageName = context.packageName

    val availableDisguises = listOf(
        DisguiseOption(
            id = "DEFAULT",
            name = "HIDEANDSEEK",
            aliasClassName = "$packageName.DefaultAlias",
            iconResId = R.drawable.vault_app_icon_1786808376985,
            isDefault = true
        ),
        DisguiseOption(
            id = "CALCULATOR",
            name = "Calculator",
            aliasClassName = "$packageName.CalculatorAlias",
            iconResId = R.drawable.ic_disguise_calculator
        ),
        DisguiseOption(
            id = "NOTES",
            name = "Notes",
            aliasClassName = "$packageName.NotesAlias",
            iconResId = R.drawable.ic_disguise_notes
        ),
        DisguiseOption(
            id = "WEATHER",
            name = "Weather",
            aliasClassName = "$packageName.WeatherAlias",
            iconResId = R.drawable.ic_disguise_weather
        ),
        DisguiseOption(
            id = "CALENDAR",
            name = "Calendar",
            aliasClassName = "$packageName.CalendarAlias",
            iconResId = R.drawable.ic_disguise_calendar
        ),
        DisguiseOption(
            id = "CLOCK",
            name = "Clock",
            aliasClassName = "$packageName.ClockAlias",
            iconResId = R.drawable.ic_disguise_clock
        ),
        DisguiseOption(
            id = "RECORDER",
            name = "Recorder",
            aliasClassName = "$packageName.RecorderAlias",
            iconResId = R.drawable.ic_disguise_recorder
        ),
        DisguiseOption(
            id = "CONTACTS",
            name = "Contacts",
            aliasClassName = "$packageName.ContactsAlias",
            iconResId = R.drawable.ic_disguise_contacts
        ),
        DisguiseOption(
            id = "FILES",
            name = "Files",
            aliasClassName = "$packageName.FilesAlias",
            iconResId = R.drawable.ic_disguise_files
        ),
        DisguiseOption(
            id = "COMPASS",
            name = "Compass",
            aliasClassName = "$packageName.CompassAlias",
            iconResId = R.drawable.ic_disguise_compass
        )
    )

    fun applyDisguise(selectedDisguiseId: String, securityManager: VaultSecurityManager): Boolean {
        val target = availableDisguises.find { it.id == selectedDisguiseId } ?: return false

        try {
            availableDisguises.forEach { disguise ->
                val component = ComponentName(packageName, disguise.aliasClassName)
                val newState = if (disguise.id == selectedDisguiseId) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                packageManager.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            }
            securityManager.activeDisguiseAlias = selectedDisguiseId
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getCurrentDisguise(securityManager: VaultSecurityManager): DisguiseOption {
        val currentId = securityManager.activeDisguiseAlias
        return availableDisguises.find { it.id == currentId } ?: availableDisguises.first()
    }
}

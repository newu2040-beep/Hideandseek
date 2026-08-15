package com.example.security

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AppCategory(val displayName: String, val emoji: String) {
    ALL("All Apps", "📱"),
    GAMES("Games", "🎮"),
    SOCIAL("Social & Chat", "💬"),
    MEDIA("Media & Video", "🎬"),
    PRODUCTIVITY("Productivity", "💼"),
    TOOLS("Tools & Utils", "🛠️"),
    PROTECTED("Protected", "🔒"),
    SYSTEM("System", "⚙️")
}

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val iconBitmap: Bitmap?,
    val isHidden: Boolean = false,
    val isSystemApp: Boolean = false,
    val category: AppCategory = AppCategory.TOOLS,
    val versionName: String? = null
)

class InstalledAppsManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun getInstalledApps(hiddenPackageNames: Set<String>): List<InstalledAppItem> = withContext(Dispatchers.IO) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        val ownPackage = context.packageName

        resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == ownPackage) return@mapNotNull null // Don't list self

            val appName = resolveInfo.loadLabel(packageManager).toString()
            val drawable = try {
                resolveInfo.loadIcon(packageManager)
            } catch (e: Exception) {
                null
            }
            val bitmap = drawable?.toBitmapSafe()
            val isHidden = hiddenPackageNames.contains(pkg)

            val appInfo = try {
                packageManager.getApplicationInfo(pkg, 0)
            } catch (e: Exception) {
                null
            }

            val isSystem = if (appInfo != null) {
                (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            } else false

            val versionName = try {
                packageManager.getPackageInfo(pkg, 0).versionName
            } catch (e: Exception) {
                null
            }

            val category = determineCategory(pkg, appInfo, appName)

            InstalledAppItem(
                packageName = pkg,
                appName = appName,
                iconBitmap = bitmap,
                isHidden = isHidden,
                isSystemApp = isSystem,
                category = category,
                versionName = versionName
            )
        }.sortedBy { it.appName.lowercase() }
    }

    private fun determineCategory(packageName: String, appInfo: ApplicationInfo?, appName: String): AppCategory {
        val lowerPkg = packageName.lowercase()
        val lowerName = appName.lowercase()

        // 1. Android category inspection (API 26+)
        if (appInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.GAMES
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.SOCIAL
                ApplicationInfo.CATEGORY_NEWS,
                ApplicationInfo.CATEGORY_MAPS,
                ApplicationInfo.CATEGORY_ACCESSIBILITY -> return AppCategory.TOOLS
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
            }
        }

        // 2. Game detection flags
        if (appInfo != null && (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0) {
            return AppCategory.GAMES
        }

        // 3. Package and Name Heuristics for Games
        val gameKeywords = listOf("game", "unity", "unreal", "supercell", "rovio", "pubg", "minecraft", "roblox", "ea.", "gameloft", "chess", "sudoku", "puzzle", "racing", "runner", "arcade", "subway", "candy", "clash", "pokemon", "fortnite", "asphalt", "shadow", "rpg")
        if (gameKeywords.any { lowerPkg.contains(it) || lowerName.contains(it) }) {
            return AppCategory.GAMES
        }

        // 4. Social & Chat heuristics
        val socialKeywords = listOf("whatsapp", "telegram", "instagram", "facebook", "twitter", "tiktok", "snapchat", "discord", "messenger", "viber", "signal", "wechat", "reddit", "linkedin", "threads", "skype", "zoom", "meet", "chat")
        if (socialKeywords.any { lowerPkg.contains(it) || lowerName.contains(it) }) {
            return AppCategory.SOCIAL
        }

        // 5. Media heuristics
        val mediaKeywords = listOf("youtube", "netflix", "spotify", "camera", "gallery", "photos", "music", "audio", "video", "player", "primevideo", "disney", "hulu", "vlc", "soundcloud", "twitch", "stream", "recorder")
        if (mediaKeywords.any { lowerPkg.contains(it) || lowerName.contains(it) }) {
            return AppCategory.MEDIA
        }

        // 6. Productivity heuristics
        val productivityKeywords = listOf("docs", "sheets", "slides", "excel", "word", "powerpoint", "office", "notion", "evernote", "keep", "trello", "slack", "jira", "asana", "gmail", "email", "calendar", "drive", "dropbox", "tasks", "notes")
        if (productivityKeywords.any { lowerPkg.contains(it) || lowerName.contains(it) }) {
            return AppCategory.PRODUCTIVITY
        }

        // 7. System app checks
        if (lowerPkg.startsWith("com.android.") || lowerPkg.startsWith("com.google.android.packageinstaller") || lowerPkg.contains("settings")) {
            return AppCategory.SYSTEM
        }

        return AppCategory.TOOLS
    }

    private fun Drawable.toBitmapSafe(): Bitmap {
        if (this is BitmapDrawable && bitmap != null) {
            return bitmap
        }
        val width = if (intrinsicWidth > 0) intrinsicWidth else 96
        val height = if (intrinsicHeight > 0) intrinsicHeight else 96
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }
}

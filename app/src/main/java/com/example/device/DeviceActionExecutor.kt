package com.example.device

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.DeviceAction
import com.example.model.DeviceActionType
import com.example.model.InstalledAppInfo

private const val TAG = "DeviceActionExecutor"

data class ActionExecutionResult(
    val success: Boolean,
    val messageBn: String,
    val needsContactPicker: Boolean = false
)

class DeviceActionExecutor {

    private val aliasMap = mapOf(
        "হোয়াটসঅ্যাপ" to listOf("whatsapp"),
        "হোয়াটসঅ্যাপ" to listOf("whatsapp"),
        "whatsapp" to listOf("whatsapp"),
        "ইউটিউব" to listOf("youtube"),
        "youtube" to listOf("youtube"),
        "ফেসবুক" to listOf("facebook"),
        "facebook" to listOf("facebook"),
        "মেসেঞ্জার" to listOf("messenger"),
        "messenger" to listOf("messenger"),
        "ইনস্টাগ্রাম" to listOf("instagram"),
        "instagram" to listOf("instagram"),
        "ক্রোম" to listOf("chrome"),
        "chrome" to listOf("chrome"),
        "জিমেইল" to listOf("gmail"),
        "gmail" to listOf("gmail"),
        "ম্যাপ" to listOf("maps"),
        "ম্যাপস" to listOf("maps"),
        "maps" to listOf("maps"),
        "ক্যামেরা" to listOf("camera"),
        "camera" to listOf("camera"),
        "সেটিংস" to listOf("settings", "সেটিংস"),
        "settings" to listOf("settings"),
        "ক্যালকুলেটর" to listOf("calculator"),
        "calculator" to listOf("calculator"),
        "টেলিগ্রাম" to listOf("telegram"),
        "telegram" to listOf("telegram"),
        "স্পটিফাই" to listOf("spotify"),
        "spotify" to listOf("spotify"),
        "বিকাশ" to listOf("bkash"),
        "bkash" to listOf("bkash"),
        "নগদ" to listOf("nagad"),
        "nagad" to listOf("nagad"),
        "ফোন" to listOf("phone", "dialer", "contacts"),
        "phone" to listOf("phone", "dialer"),
        "ঘড়ি" to listOf("clock"),
        "clock" to listOf("clock"),
        "প্লে স্টোর" to listOf("play store", "vending"),
        "play store" to listOf("play store", "vending")
    )

    fun execute(context: Context, deviceAction: DeviceAction): ActionExecutionResult {
        return when (deviceAction.action) {
            DeviceActionType.OPEN_YOUTUBE_SEARCH -> openYouTubeSearch(context, deviceAction.target)
            DeviceActionType.OPEN_APP -> openApp(context, deviceAction.target)
            DeviceActionType.MAKE_CALL -> makeCall(context, deviceAction.target)
            DeviceActionType.NONE -> ActionExecutionResult(
                success = true,
                messageBn = ""
            )
        }
    }

    fun openYouTubeSearch(context: Context, query: String): ActionExecutionResult {
        val cleanQuery = query.trim()
        val uri = if (cleanQuery.isBlank()) {
            Uri.parse("https://www.youtube.com")
        } else {
            Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(cleanQuery)}")
        }

        val youtubeIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.youtube")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(youtubeIntent)
            ActionExecutionResult(
                success = true,
                messageBn = if (cleanQuery.isBlank()) {
                    "YouTube অ্যাপ খোলা হয়েছে।"
                } else {
                    "YouTube-এ \"$cleanQuery\" সার্চ করা হচ্ছে।"
                }
            )
        } catch (_: ActivityNotFoundException) {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(browserIntent)
                ActionExecutionResult(
                    success = true,
                    messageBn = if (cleanQuery.isBlank()) {
                        "ব্রাউজারে YouTube খোলা হয়েছে।"
                    } else {
                        "ব্রাউজারে YouTube-এ \"$cleanQuery\" খোলা হচ্ছে।"
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open YouTube search", e)
                ActionExecutionResult(
                    success = false,
                    messageBn = "YouTube বা ব্রাউজার পাওয়া যায়নি।"
                )
            }
        }
    }

    fun openApp(context: Context, targetName: String): ActionExecutionResult {
        val trimmed = targetName.trim()
        if (trimmed.isEmpty()) {
            return ActionExecutionResult(false, "কোন অ্যাপ খুলতে হবে তা উল্লেখ করা হয়নি।")
        }

        val pm = context.packageManager
        val normalized = trimmed.lowercase()
        val candidateKeywords = buildList {
            add(normalized)
            aliasMap.entries.forEach { (key, aliases) ->
                if (normalized.contains(key)) {
                    addAll(aliases)
                }
            }
        }.distinct()

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val installedActivities = pm.queryIntentActivities(launcherIntent, 0)

        val matched = installedActivities.firstOrNull { resolveInfo ->
            val label = resolveInfo.loadLabel(pm).toString().lowercase()
            val pkg = resolveInfo.activityInfo.packageName.lowercase()
            candidateKeywords.any { keyword ->
                label.contains(keyword) || pkg.contains(keyword)
            }
        }

        if (matched != null) {
            val launchIntent = pm.getLaunchIntentForPackage(matched.activityInfo.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (launchIntent != null) {
                return try {
                    context.startActivity(launchIntent)
                    val appLabel = matched.loadLabel(pm).toString()
                    ActionExecutionResult(true, "$appLabel খোলা হয়েছে।")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to launch matched app", e)
                    ActionExecutionResult(false, "$trimmed অ্যাপটি চালু করা যায়নি।")
                }
            }
        }

        // System Intent fallbacks for Camera / Settings / Phone
        val fallbackIntent = when {
            candidateKeywords.any { it.contains("camera") } ->
                Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            candidateKeywords.any { it.contains("settings") || it.contains("সেটিংস") } ->
                Intent(Settings.ACTION_SETTINGS)
            candidateKeywords.any { it.contains("phone") || it.contains("dialer") } ->
                Intent(Intent.ACTION_DIAL)
            candidateKeywords.any { it.contains("youtube") } ->
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
            else -> null
        }?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        if (fallbackIntent != null) {
            return try {
                context.startActivity(fallbackIntent)
                ActionExecutionResult(true, "$trimmed খোলা হয়েছে।")
            } catch (e: Exception) {
                Log.e(TAG, "Fallback system intent failed", e)
                ActionExecutionResult(false, "$trimmed অ্যাপটি ডিভাইসে পাওয়া যায়নি।")
            }
        }

        return ActionExecutionResult(
            success = false,
            messageBn = "\"$trimmed\" অ্যাপটি এই ডিভাইসে ইনস্টল করা নেই।"
        )
    }

    fun makeCall(context: Context, target: String): ActionExecutionResult {
        val trimmed = target.trim()
        if (trimmed.isEmpty()) {
            return openDialerWithNumber(context, "", "ডায়ালার খোলা হয়েছে।")
        }

        val digitsOnly = trimmed.filter { it.isDigit() || it == '+' }
        val isPhoneNumber = digitsOnly.count { it.isDigit() } >= 3 &&
            trimmed.all { it.isDigit() || it in setOf('+', '-', ' ', '(', ')') }

        if (isPhoneNumber) {
            return openDialerWithNumber(context, digitsOnly, "$digitsOnly নম্বরে কল করার জন্য ডায়ালার খোলা হয়েছে।")
        }

        // Lookup by contact name if READ_CONTACTS is granted
        val hasContactsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasContactsPermission) {
            val foundNumber = findPhoneNumberByName(context, trimmed)
            if (!foundNumber.isNullOrBlank()) {
                return openDialerWithNumber(
                    context,
                    foundNumber,
                    "$trimmed ($foundNumber)-কে কল করার জন্য ডায়ালার খোলা হয়েছে।"
                )
            }
        }

        return ActionExecutionResult(
            success = false,
            messageBn = "\"$trimmed\"-এর নম্বর খুঁজে নিতে কন্ট্যাক্ট থেকে বাছাই করুন বা নম্বরটি লিখুন।",
            needsContactPicker = true
        )
    }

    fun resolvePickedContactAndDial(context: Context, contactUri: Uri): ActionExecutionResult {
        return try {
            val resolver = context.contentResolver
            var contactId: String? = null
            var displayName: String? = null

            resolver.query(
                contactUri,
                arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    contactId = cursor.getString(0)
                    displayName = cursor.getString(1)
                }
            }

            if (contactId != null) {
                var phoneNumber: String? = null
                resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                    arrayOf(contactId),
                    null
                )?.use { phoneCursor ->
                    if (phoneCursor.moveToFirst()) {
                        phoneNumber = phoneCursor.getString(0)
                    }
                }

                if (!phoneNumber.isNullOrBlank()) {
                    val label = displayName ?: phoneNumber!!
                    return openDialerWithNumber(
                        context,
                        phoneNumber!!,
                        "$label ($phoneNumber)-এর জন্য ডায়ালার খোলা হয়েছে।"
                    )
                }
            }

            ActionExecutionResult(false, "এই কন্ট্যাক্টে কোনো ফোন নম্বর পাওয়া যায়নি।")
        } catch (e: Exception) {
            Log.e(TAG, "Failed reading picked contact", e)
            ActionExecutionResult(false, "কন্ট্যাক্ট পড়া যায়নি। অনুগ্রহ করে সরাসরি নম্বর দিন।")
        }
    }

    private fun openDialerWithNumber(
        context: Context,
        number: String,
        successMessage: String
    ): ActionExecutionResult {
        val uri = if (number.isBlank()) Uri.parse("tel:") else Uri.parse("tel:${Uri.encode(number)}")
        val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(dialIntent)
            ActionExecutionResult(true, successMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch dialer", e)
            ActionExecutionResult(false, "ডিভাইসে ফোন ডায়ালার অ্যাপ পাওয়া যায়নি।")
        }
    }

    private fun findPhoneNumberByName(context: Context, queryName: String): String? {
        val lowerQuery = queryName.lowercase()
        return try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                null
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx).orEmpty() else ""
                    val num = if (numIdx >= 0) cursor.getString(numIdx).orEmpty() else ""
                    if (name.lowercase().contains(lowerQuery) && num.isNotBlank()) {
                        return@use num
                    }
                }
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying contacts", e)
            null
        }
    }

    fun getInstalledApps(context: Context): List<InstalledAppInfo> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(launcherIntent, 0)
            .map { resolveInfo ->
                InstalledAppInfo(
                    appName = resolveInfo.loadLabel(pm).toString(),
                    packageName = resolveInfo.activityInfo.packageName
                )
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }
}

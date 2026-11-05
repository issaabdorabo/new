package com.example.ui.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import com.example.ui.theme.*
object AttachmentUtils {

    // Global state for opening in-app viewer dialog seamlessly
    val activePreviewPath = MutableStateFlow<String?>(null)

    fun saveAttachment(context: Context, sourceUri: Uri, prefix: String = "doc"): String? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(sourceUri) ?: "application/octet-stream"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: run {
                if (mimeType.contains("pdf")) "pdf" else "jpg"
            }

            val attachmentsDir = File(context.filesDir, "attachments")
            if (!attachmentsDir.exists()) {
                attachmentsDir.mkdirs()
            }

            val uniqueFileName = "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$extension"
            val destinationFile = File(attachmentsDir, uniqueFileName)

            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Opens the attachment immediately inside the app via in-app preview dialog,
     * ensuring that PDF documents, images, and spreadsheets ALWAYS open regardless
     * of external applications installed on the user device.
     */
    fun openAttachment(context: Context, filePath: String?) {
        if (filePath.isNullOrBlank()) return
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "الملف غير موجود في الجهاز", Toast.LENGTH_SHORT).show()
            return
        }

        // Trigger in-app viewer dialog for flawless direct presentation
        activePreviewPath.value = filePath
    }

    /**
     * Tries to open the file in an external app using FileProvider and properly granted URI permissions.
     */
    fun openExternal(context: Context, filePath: String?) {
        if (filePath.isNullOrBlank()) return
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "الملف غير موجود في الجهاز", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val extension = file.extension.lowercase()
            val mimeType = when (extension) {
                "pdf" -> "application/pdf"
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "csv" -> "text/csv"
                else -> "*/*"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Grant read permission to all matching activities
            val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "فتح المرفق")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "لا يوجد تطبيق خارجي لفتح هذا الملف، تم فتح المعاينة داخل التطبيق", Toast.LENGTH_SHORT).show()
            activePreviewPath.value = filePath
        }
    }

    fun shareAttachment(context: Context, filePath: String?) {
        if (filePath.isNullOrBlank()) return
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Toast.makeText(context, "الملف غير موجود", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val extension = file.extension.lowercase()
            val mimeType = when (extension) {
                "pdf" -> "application/pdf"
                "png", "jpg", "jpeg" -> "image/*"
                "csv" -> "text/csv"
                else -> "*/*"
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "مشاركة المرفق").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر مشاركة المرفق: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun getDisplayName(context: Context, filePath: String?): String {
        return getDisplayName(filePath)
    }

    fun getDisplayName(filePath: String?): String {
        if (filePath.isNullOrBlank()) return ""
        val file = File(filePath)
        val ext = file.extension.uppercase()
        return if (ext == "PDF") "مستند PDF" else "صورة مرفقة ($ext)"
    }

    fun isPdf(filePath: String?): Boolean {
        if (filePath.isNullOrBlank()) return false
        return filePath.endsWith(".pdf", ignoreCase = true)
    }
}

package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object AttachmentUtils {

    fun saveAttachment(context: Context, sourceUri: Uri, prefix: String = "doc"): String? {
        return try {
            try {
                context.contentResolver.takePersistableUriPermission(sourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // Ignore if not a persistable URI
            }

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
            sourceUri.toString()
        }
    }

    fun openAttachment(context: Context, filePath: String?) {
        if (filePath.isNullOrBlank()) {
            Toast.makeText(context, "لا يوجد مرفق محدد", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val isContentUri = filePath.startsWith("content://") || filePath.startsWith("file://")
            val uri: Uri = if (isContentUri) {
                Uri.parse(filePath)
            } else {
                val file = File(filePath)
                if (file.exists()) {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                } else {
                    Uri.parse(filePath)
                }
            }

            val extension = if (filePath.contains(".")) filePath.substringAfterLast(".").lowercase() else ""
            val mimeType = when {
                extension == "pdf" || filePath.contains("pdf", ignoreCase = true) -> "application/pdf"
                extension in listOf("png", "jpg", "jpeg", "webp") || filePath.contains("image", ignoreCase = true) -> "image/*"
                else -> context.contentResolver.getType(uri) ?: "*/*"
            }

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(viewIntent, "فتح المرفق").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val resInfoList = context.packageManager.queryIntentActivities(chooser, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                context.grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                context.startActivity(chooser)
            } catch (e: Exception) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(shareIntent, "مشاركة أو فتح المرفق").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر فتح المرفق: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareAttachment(context: Context, filePath: String?) {
        if (filePath.isNullOrBlank()) return
        try {
            val isContentUri = filePath.startsWith("content://") || filePath.startsWith("file://")
            val uri: Uri = if (isContentUri) {
                Uri.parse(filePath)
            } else {
                val file = File(filePath)
                if (file.exists()) {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                } else {
                    Uri.parse(filePath)
                }
            }

            val extension = if (filePath.contains(".")) filePath.substringAfterLast(".").lowercase() else ""
            val mimeType = when {
                extension == "pdf" || filePath.contains("pdf", ignoreCase = true) -> "application/pdf"
                extension in listOf("png", "jpg", "jpeg", "webp") || filePath.contains("image", ignoreCase = true) -> "image/*"
                else -> context.contentResolver.getType(uri) ?: "*/*"
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة المرفق").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر مشاركة المرفق", Toast.LENGTH_SHORT).show()
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

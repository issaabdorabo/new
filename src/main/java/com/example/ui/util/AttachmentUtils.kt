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

    fun openAttachment(context: Context, filePath: String) {
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
                else -> "*/*"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "فتح المرفق")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "لا يوجد تطبيق مناسب لفتح هذا الملف", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareAttachment(context: Context, filePath: String) {
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
                else -> "*/*"
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة المرفق"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "تعذر مشاركة المرفق", Toast.LENGTH_SHORT).show()
        }
    }

    fun getDisplayName(filePath: String): String {
        val file = File(filePath)
        val ext = file.extension.uppercase()
        return if (ext == "PDF") "مستند PDF" else "صورة مرفقة ($ext)"
    }

    fun isPdf(filePath: String): Boolean {
        return filePath.endsWith(".pdf", ignoreCase = true)
    }
}

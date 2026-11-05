package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TechCyan
import com.example.ui.util.AttachmentUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Universal In-App Document & Image Viewer Dialog.
 * Resolves the issue of opening PDF and Image attachments directly inside the app
 * without requiring third-party viewers to be pre-installed on the device.
 */
@Composable
fun AttachmentViewerDialog(
    filePath: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val file = remember(filePath) { File(filePath) }
    val extension = remember(filePath) { file.extension.lowercase() }
    val isPdf = extension == "pdf"
    val isImage = extension in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
    val isCsv = extension in listOf("csv", "txt")

    var pdfBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var csvLines by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(filePath) {
        isLoading = true
        errorMessage = null
        if (!file.exists()) {
            errorMessage = "الملف غير موجود في ذاكرة الجهاز"
            isLoading = false
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            try {
                when {
                    isPdf -> {
                        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                        val renderer = PdfRenderer(pfd)
                        val pages = mutableListOf<Bitmap>()
                        val totalPages = renderer.pageCount
                        for (i in 0 until minOf(totalPages, 20)) {
                            val page = renderer.openPage(i)
                            val width = page.width * 2
                            val height = page.height * 2
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)
                            canvas.drawColor(android.graphics.Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            pages.add(bitmap)
                            page.close()
                        }
                        renderer.close()
                        pfd.close()
                        pdfBitmaps = pages
                    }
                    isImage -> {
                        val opts = BitmapFactory.Options().apply {
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        }
                        val bmp = BitmapFactory.decodeFile(file.absolutePath, opts)
                        if (bmp != null) {
                            imageBitmap = bmp
                        } else {
                            errorMessage = "تعذر فك تشفير الصورة"
                        }
                    }
                    isCsv -> {
                        csvLines = file.readLines(Charsets.UTF_8).take(100)
                    }
                    else -> {
                        errorMessage = "نوع الملف ($extension) غير مدعوم للمعاينة المباشرة"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "خطأ أثناء قراءة الملف: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar with File Details and Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = when {
                                isPdf -> Icons.Default.PictureAsPdf
                                isCsv -> Icons.Default.TableChart
                                else -> Icons.Default.OpenInNew
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = file.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                            Text(
                                text = "${file.length() / 1024} KB • ${extension.uppercase()}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Share Button
                        IconButton(
                            onClick = { AttachmentUtils.shareAttachment(context, file.absolutePath) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        }

                        // External App Open Attempt Button
                        IconButton(
                            onClick = { AttachmentUtils.openExternal(context, file.absolutePath) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "فتح بتطبيق خارجي", tint = TechCyan, modifier = Modifier.size(18.dp))
                        }

                        // Close Dialog Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                HorizontalDivider()

                // Content Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = TechCyan)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("جاري تجهيز ومعاينة المستند...", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        errorMessage != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    color = Color(0xFFFECACA),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { AttachmentUtils.openExternal(context, file.absolutePath) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("محاولة الفتح عبر تطبيق خارجي")
                                }
                            }
                        }
                        isPdf && pdfBitmaps.isNotEmpty() -> {
                            // Scrollable rendered PDF pages
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                itemsIndexed(pdfBitmaps) { index, pageBmp ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(0.96f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF1E293B))
                                                    .padding(vertical = 4.dp, horizontal = 10.dp)
                                            ) {
                                                Text(
                                                    text = "صفحة ${index + 1} من ${pdfBitmaps.size}",
                                                    fontSize = 10.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Image(
                                                bitmap = pageBmp.asImageBitmap(),
                                                contentDescription = "صفحة PDF ${index + 1}",
                                                modifier = Modifier.fillMaxWidth(),
                                                contentScale = ContentScale.FillWidth
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        isImage && imageBitmap != null -> {
                            // Image view with smooth zoomable container
                            Image(
                                bitmap = imageBitmap!!.asImageBitmap(),
                                contentDescription = "معاينة المرفق",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        isCsv && csvLines.isNotEmpty() -> {
                            // CSV / Excel Text table viewer
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                itemsIndexed(csvLines) { idx, line ->
                                    val isHeader = idx == 0 || line.contains("نظام قطع غيار")
                                    val cells = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isHeader) Color(0xFF1E293B) else if (idx % 2 == 1) Color(0xFFF8FAFC) else Color.White
                                            )
                                            .padding(vertical = 6.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        cells.forEach { rawCell ->
                                            val clean = rawCell.trim().removeSurrounding("\"")
                                            Text(
                                                text = clean,
                                                fontSize = if (isHeader) 11.5.sp else 10.5.sp,
                                                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isHeader) Color.White else Color(0xFF0F172A),
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                }
                            }
                        }
                        else -> {
                            Text("لا توجد بيانات للعرض", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                // Bottom Footer Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إغلاق")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { AttachmentUtils.shareAttachment(context, file.absolutePath) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة الملف")
                    }
                }
            }
        }
    }
}

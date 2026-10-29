package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    /**
     * Exports tabular data into an Excel-compatible CSV file with UTF-8 BOM (Byte Order Mark),
     * ensuring Arabic characters display correctly in Microsoft Excel and Google Sheets.
     */
    fun exportToExcelCsv(
        context: Context,
        fileName: String,
        title: String,
        headers: List<String>,
        rows: List<List<String>>,
        summaryLines: List<String> = emptyList()
    ) {
        try {
            val safeFileName = if (fileName.endsWith(".csv", ignoreCase = true)) fileName else "$fileName.csv"
            val file = File(context.cacheDir, safeFileName)
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            FileOutputStream(file).use { fos ->
                // Write UTF-8 BOM so Excel recognises UTF-8 encoding
                fos.write(0xEF)
                fos.write(0xBB)
                fos.write(0xBF)

                val writer = fos.bufferedWriter(Charsets.UTF_8)
                writer.write("\"نظام قطع غيار Pro - $title\"\n")
                writer.write("\"تاريخ التصدير: $dateStr\"\n\n")

                // Write Headers
                val headerLine = headers.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
                writer.write("$headerLine\n")

                // Write Rows
                rows.forEach { row ->
                    val rowLine = row.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
                    writer.write("$rowLine\n")
                }

                // Write Summary
                if (summaryLines.isNotEmpty()) {
                    writer.write("\n\"--- ملخص التقرير ---\"\n")
                    summaryLines.forEach { line ->
                        writer.write("\"${line.replace("\"", "\"\"")}\"\n")
                    }
                }

                writer.flush()
            }

            shareFile(context, file, "application/vnd.ms-excel", "مشاركة ملف Excel: $title")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "فشل تصدير ملف Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Exports tabular data into a formatted multi-page PDF document using native Android PdfDocument,
     * complete with headers, table formatting, and totals.
     */
    fun exportToPdf(
        context: Context,
        fileName: String,
        title: String,
        subtitle: String,
        headers: List<String>,
        rows: List<List<String>>,
        summaryLines: List<String> = emptyList()
    ) {
        try {
            val safeFileName = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"
            val file = File(context.cacheDir, safeFileName)
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842 // Standard A4 dimensions in points
            var pageNumber = 1

            var currentY = 50f
            val margin = 36f
            val printableWidth = pageWidth - (margin * 2)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            fun drawHeader(c: Canvas) {
                // Top Brand bar
                paint.color = Color.parseColor("#1E3A8A") // Primary Dark Blue
                c.drawRect(margin, 30f, pageWidth - margin, 75f, paint)

                textPaint.color = Color.WHITE
                textPaint.textSize = 16f
                textPaint.isFakeBoldText = true
                textPaint.textAlign = Paint.Align.RIGHT
                c.drawText("قطع غيار Pro - إدارة المخزون والمبيعات", pageWidth - margin - 16f, 56f, textPaint)

                textPaint.textSize = 10f
                textPaint.isFakeBoldText = false
                textPaint.textAlign = Paint.Align.LEFT
                c.drawText("التاريخ: $dateStr", margin + 16f, 56f, textPaint)

                // Title
                textPaint.color = Color.parseColor("#0F172A")
                textPaint.textSize = 15f
                textPaint.isFakeBoldText = true
                textPaint.textAlign = Paint.Align.RIGHT
                c.drawText(title, pageWidth - margin, 100f, textPaint)

                if (subtitle.isNotBlank()) {
                    textPaint.color = Color.parseColor("#64748B")
                    textPaint.textSize = 10f
                    textPaint.isFakeBoldText = false
                    c.drawText(subtitle, pageWidth - margin, 116f, textPaint)
                }

                currentY = 135f
            }

            drawHeader(canvas)

            val columnCount = headers.size
            val colWidth = printableWidth / columnCount

            fun drawTableHeaders(c: Canvas, y: Float) {
                paint.color = Color.parseColor("#0F172A") // Table header background
                c.drawRoundRect(RectF(margin, y - 16f, pageWidth - margin, y + 10f), 4f, 4f, paint)

                textPaint.color = Color.WHITE
                textPaint.textSize = 10.5f
                textPaint.isFakeBoldText = true
                textPaint.textAlign = Paint.Align.CENTER

                for (i in headers.indices) {
                    val colX = pageWidth - margin - (i * colWidth) - (colWidth / 2f)
                    c.drawText(headers[i], colX, y, textPaint)
                }
            }

            drawTableHeaders(canvas, currentY)
            currentY += 24f

            // Draw data rows
            val rowHeight = 22f
            rows.forEachIndexed { rowIndex, rowData ->
                if (currentY + rowHeight > pageHeight - 60f) {
                    // Draw page number
                    textPaint.color = Color.parseColor("#94A3B8")
                    textPaint.textSize = 9f
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText("صفحة $pageNumber", pageWidth / 2f, pageHeight - 20f, textPaint)

                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    drawHeader(canvas)
                    drawTableHeaders(canvas, currentY)
                    currentY += 24f
                }

                // Alternate row background
                if (rowIndex % 2 == 1) {
                    paint.color = Color.parseColor("#F1F5F9")
                    canvas.drawRect(margin, currentY - 14f, pageWidth - margin, currentY + 8f, paint)
                }

                // Row border bottom
                paint.color = Color.parseColor("#E2E8F0")
                paint.strokeWidth = 0.5f
                paint.style = Paint.Style.STROKE
                canvas.drawLine(margin, currentY + 8f, pageWidth - margin, currentY + 8f, paint)
                paint.style = Paint.Style.FILL

                textPaint.color = Color.parseColor("#1E293B")
                textPaint.textSize = 9.5f
                textPaint.isFakeBoldText = false
                textPaint.textAlign = Paint.Align.CENTER

                for (colIdx in rowData.indices) {
                    val cellText = rowData[colIdx]
                    val colX = pageWidth - margin - (colIdx * colWidth) - (colWidth / 2f)
                    val truncated = if (cellText.length > 28) cellText.take(25) + "..." else cellText
                    canvas.drawText(truncated, colX, currentY, textPaint)
                }

                currentY += rowHeight
            }

            // Summary Section
            if (summaryLines.isNotEmpty()) {
                currentY += 16f
                if (currentY + (summaryLines.size * 18f) > pageHeight - 50f) {
                    textPaint.color = Color.parseColor("#94A3B8")
                    textPaint.textSize = 9f
                    textPaint.textAlign = Paint.Align.CENTER
                    canvas.drawText("صفحة $pageNumber", pageWidth / 2f, pageHeight - 20f, textPaint)

                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    drawHeader(canvas)
                }

                paint.color = Color.parseColor("#F8FAFC")
                val summaryHeight = (summaryLines.size * 18f) + 16f
                canvas.drawRoundRect(
                    RectF(margin, currentY, pageWidth - margin, currentY + summaryHeight),
                    8f,
                    8f,
                    paint
                )

                paint.color = Color.parseColor("#CBD5E1")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRoundRect(
                    RectF(margin, currentY, pageWidth - margin, currentY + summaryHeight),
                    8f,
                    8f,
                    paint
                )
                paint.style = Paint.Style.FILL

                var summaryY = currentY + 18f
                textPaint.color = Color.parseColor("#0F172A")
                textPaint.textSize = 10.5f
                textPaint.isFakeBoldText = true
                textPaint.textAlign = Paint.Align.RIGHT

                summaryLines.forEach { line ->
                    canvas.drawText(line, pageWidth - margin - 14f, summaryY, textPaint)
                    summaryY += 18f
                }
            }

            // Final Page Footer
            textPaint.color = Color.parseColor("#94A3B8")
            textPaint.textSize = 9f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("صفحة $pageNumber - تم الاستخراج عبر تطبيق قطع غيار Pro", pageWidth / 2f, pageHeight - 20f, textPaint)

            pdfDocument.finishPage(page)

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            shareFile(context, file, "application/pdf", "مشاركة مستند PDF: $title")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "فشل إنشاء مستند PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}

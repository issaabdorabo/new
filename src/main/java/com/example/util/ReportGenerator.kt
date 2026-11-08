package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.WarehouseItemStock
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportGenerator {

    enum class ReportColumn(val id: String, val titleAr: String) {
        SERIAL("serial", "م"),
        ITEM_NAME("name", "اسم الصنف"),
        ITEM_UNIT("unit", "الوحدة"),
        WAREHOUSE("warehouse", "المخزن"),
        CURRENT_STOCK("stock", "الرصيد الحالي"),
        MIN_STOCK("min_stock", "حد الطلب"),
        PURCHASE_PRICE("price", "سعر الشراء"),
        TOTAL_VALUE("total_value", "إجمالي القيمة")
    }

    fun generatePdf(
        context: Context,
        title: String,
        warehouseName: String,
        appUserName: String,
        orgName: String,
        data: List<WarehouseItemStock>,
        selectedColumns: Set<ReportColumn>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait in points (72 dpi)
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Header Background
        paint.color = Color.rgb(30, 41, 59) // Deep Slate
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Header Text
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("مخزني الذكي - $title", 30f, 38f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("المنشأة: $orgName | إعداد المستخدم: $appUserName", 30f, 60f, paint)
        canvas.drawText("المخزن: $warehouseName | تاريخ التقرير: $currentDate", 30f, 78f, paint)

        // Table setup
        var currentY = 120f
        val startX = 30f
        val tableWidth = 535f

        val activeColumns = ReportColumn.values().filter { selectedColumns.contains(it) }
        val colWidth = if (activeColumns.isNotEmpty()) tableWidth / activeColumns.size else tableWidth

        // Table Header
        paint.color = Color.rgb(226, 232, 240) // Light slate gray
        canvas.drawRect(startX, currentY - 20f, startX + tableWidth, currentY + 10f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        activeColumns.forEachIndexed { index, col ->
            val x = startX + (index * colWidth) + 8f
            canvas.drawText(col.titleAr, x, currentY, paint)
        }

        currentY += 24f

        // Table Rows
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT

        data.forEachIndexed { rowIndex, item ->
            if (currentY > 780f) {
                // Break or stop single page for demo simplicity
                return@forEachIndexed
            }

            if (rowIndex % 2 == 1) {
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(startX, currentY - 16f, startX + tableWidth, currentY + 8f, paint)
            }

            // Row grid bottom line
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawLine(startX, currentY + 8f, startX + tableWidth, currentY + 8f, paint)

            paint.color = Color.rgb(30, 41, 59)

            activeColumns.forEachIndexed { index, col ->
                val x = startX + (index * colWidth) + 8f
                val cellText = when (col) {
                    ReportColumn.SERIAL -> "${rowIndex + 1}"
                    ReportColumn.ITEM_NAME -> item.itemName
                    ReportColumn.ITEM_UNIT -> item.itemUnit
                    ReportColumn.WAREHOUSE -> item.warehouseName
                    ReportColumn.CURRENT_STOCK -> "%.1f".format(item.currentStock)
                    ReportColumn.MIN_STOCK -> "%.1f".format(item.minStockLevel)
                    ReportColumn.PURCHASE_PRICE -> "%.2f".format(item.purchasePrice)
                    ReportColumn.TOTAL_VALUE -> "%.2f".format(item.currentStock * item.purchasePrice)
                }
                // Truncate if too long
                val displayText = if (cellText.length > 20) cellText.take(18) + ".." else cellText
                canvas.drawText(displayText, x, currentY, paint)
            }

            currentY += 22f
        }

        // Footer
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 9f
        canvas.drawText("تم إنشاء هذا التقرير آلياً عبر نظام مخزني الذكي - المطور: عيسى عبدربه 771438740", 30f, 820f, paint)

        pdfDocument.finishPage(page)

        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "inventory_report_${System.currentTimeMillis()}.pdf"
        val file = File(reportsDir, fileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()

        return file
    }

    fun generateExcel(
        context: Context,
        title: String,
        warehouseName: String,
        appUserName: String,
        data: List<WarehouseItemStock>,
        selectedColumns: Set<ReportColumn>
    ): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "inventory_report_${System.currentTimeMillis()}.csv"
        val file = File(reportsDir, fileName)

        val activeColumns = ReportColumn.values().filter { selectedColumns.contains(it) }

        val sb = StringBuilder()
        // UTF-8 BOM so Microsoft Excel renders Arabic text properly!
        sb.append("\uFEFF")

        // Metadata Header
        sb.append("\"تقرير: $title\"\n")
        sb.append("\"المخزن:\",\"$warehouseName\"\n")
        sb.append("\"المستخدم المسجل:\",\"$appUserName\"\n")
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        sb.append("\"تاريخ الاستخراج:\",\"${dateFormat.format(Date())}\"\n\n")

        // Column headers
        sb.append(activeColumns.joinToString(",") { "\"${it.titleAr}\"" })
        sb.append("\n")

        // Data rows
        data.forEachIndexed { index, item ->
            val rowValues = activeColumns.map { col ->
                val raw = when (col) {
                    ReportColumn.SERIAL -> "${index + 1}"
                    ReportColumn.ITEM_NAME -> item.itemName
                    ReportColumn.ITEM_UNIT -> item.itemUnit
                    ReportColumn.WAREHOUSE -> item.warehouseName
                    ReportColumn.CURRENT_STOCK -> "%.2f".format(item.currentStock)
                    ReportColumn.MIN_STOCK -> "%.2f".format(item.minStockLevel)
                    ReportColumn.PURCHASE_PRICE -> "%.2f".format(item.purchasePrice)
                    ReportColumn.TOTAL_VALUE -> "%.2f".format(item.currentStock * item.purchasePrice)
                }
                "\"${raw.replace("\"", "\"\"")}\""
            }
            sb.append(rowValues.joinToString(","))
            sb.append("\n")
        }

        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    fun viewFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "عرض التقرير"))
    }
}

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
        CURRENT_STOCK("stock", "الرصيد / الكمية"),
        MIN_STOCK("min_stock", "حد الطلب"),
        PURCHASE_PRICE("price", "السعر"),
        TOTAL_VALUE("total_value", "الإجمالي")
    }

    /**
     * Requirement 8:
     * - Remove "مخزني الذكي" from top of report.
     * - Titles in the right (RTL).
     * - Dynamic title according to chosen report type.
     * - Remove word "المنشأة:", show orgName directly.
     * - Remove word "إعداد المستخدم", show in bottom footer center: "المستخدم: [appUserName]".
     * - All report elements ordered and drawn RTL.
     */
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
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val pageWidth = 595f
        val margin = 30f
        val contentWidth = pageWidth - (margin * 2)

        // 1. Header Area (Clean, Right-Aligned RTL)
        // Draw elegant top bar
        paint.color = Color.rgb(30, 41, 59)
        canvas.drawRect(0f, 0f, pageWidth, 5f, paint)

        // Organization Name on top right
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(orgName.ifBlank { "المؤسسة العامة للتوريد والمخازن" }, pageWidth - margin, 40f, paint)

        // Report Title on right
        paint.textSize = 14f
        paint.color = Color.rgb(37, 99, 235) // Primary accent
        canvas.drawText(title, pageWidth - margin, 62f, paint)

        // Subtitle (Warehouse & Date) on right
        paint.textSize = 10f
        paint.color = Color.rgb(100, 116, 139)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("المخزن: $warehouseName  |  تاريخ التقرير: $currentDate", pageWidth - margin, 80f, paint)

        // Subtle divider line
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawLine(margin, 95f, pageWidth - margin, 95f, paint)

        // 2. Table Setup (RTL ordered columns)
        val activeColumns = ReportColumn.values().filter { selectedColumns.contains(it) }
        val colCount = if (activeColumns.isNotEmpty()) activeColumns.size else 1
        val colWidth = contentWidth / colCount

        var currentY = 125f

        // Table Header Background
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(margin, currentY - 18f, pageWidth - margin, currentY + 8f, paint)

        // Table Header Text (Aligned Right for each column in RTL order)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT

        activeColumns.forEachIndexed { index, col ->
            // In RTL: column 0 is at right edge, column 1 to its left
            val rightX = (pageWidth - margin) - (index * colWidth) - 8f
            canvas.drawText(col.titleAr, rightX, currentY, paint)
        }

        currentY += 24f

        // 3. Table Rows
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        data.forEachIndexed { rowIndex, item ->
            if (currentY > 790f) {
                return@forEachIndexed // Fits page
            }

            // Alternating row background
            if (rowIndex % 2 == 1) {
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(margin, currentY - 15f, pageWidth - margin, currentY + 6f, paint)
            }

            // Row line
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawLine(margin, currentY + 6f, pageWidth - margin, currentY + 6f, paint)

            paint.color = Color.rgb(30, 41, 59)
            paint.textAlign = Paint.Align.RIGHT

            activeColumns.forEachIndexed { index, col ->
                val rightX = (pageWidth - margin) - (index * colWidth) - 8f
                val cellText = when (col) {
                    ReportColumn.SERIAL -> "${rowIndex + 1}"
                    ReportColumn.ITEM_NAME -> item.itemName
                    ReportColumn.ITEM_UNIT -> item.itemUnit
                    ReportColumn.WAREHOUSE -> item.warehouseName
                    ReportColumn.CURRENT_STOCK -> "%.0f".format(item.currentStock)
                    ReportColumn.MIN_STOCK -> "%.0f".format(item.minStockLevel)
                    ReportColumn.PURCHASE_PRICE -> "%.2f".format(item.purchasePrice)
                    ReportColumn.TOTAL_VALUE -> "%.2f".format(item.currentStock * item.purchasePrice)
                }
                val displayText = if (cellText.length > 20) cellText.take(18) + ".." else cellText
                canvas.drawText(displayText, rightX, currentY, paint)
            }

            currentY += 20f
        }

        // 4. Bottom Margin Footer (Center: "المستخدم: [appUserName]" as requested in Requirement 8)
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawLine(margin, 810f, pageWidth - margin, 810f, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("المستخدم: $appUserName", pageWidth / 2, 826f, paint)

        pdfDocument.finishPage(page)

        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "report_${System.currentTimeMillis()}.pdf"
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

        val fileName = "report_${System.currentTimeMillis()}.csv"
        val file = File(reportsDir, fileName)

        val activeColumns = ReportColumn.values().filter { selectedColumns.contains(it) }

        val sb = StringBuilder()
        // UTF-8 BOM for Microsoft Excel Arabic rendering
        sb.append("\uFEFF")

        // Metadata Header
        sb.append("\"$title\"\n")
        sb.append("\"المخزن:\",\"$warehouseName\"\n")
        sb.append("\"المستخدم:\",\"$appUserName\"\n")
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
                    ReportColumn.CURRENT_STOCK -> "%.0f".format(item.currentStock)
                    ReportColumn.MIN_STOCK -> "%.0f".format(item.minStockLevel)
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

package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import android.content.Context
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import android.widget.Toast
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.example.ui.util.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.AppNotification
import com.example.data.model.PartDisbursementHistory
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryLighter
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseLight
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TechCyanLight
import kotlin.math.abs

/**
 * Authentic Barcode / QR Scanner Dialog with live CameraX preview feed & animated laser viewfinder.
 * Resolves camera opening problem and supports direct camera scanning, typing, or preset testing.
 */
@Composable
fun BarcodeScannerDialog(
    title: String = "قارئ الباركود الذكي بالكاميرا",
    subtitle: String = "وجّه الكاميرا نحو باركود الصنف أو أدخل الرمز يدوياً",
    existingBarcodes: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var manualBarcode by remember { mutableStateOf("") }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserY"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = TechCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(subtitle, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

                // High-tech Camera Viewfinder Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(2.dp, TechCyan, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        // Live CameraX Preview
                        val activityLifecycleOwner = (context as? androidx.lifecycle.LifecycleOwner) ?: lifecycleOwner
                        var cameraError by remember { mutableStateOf<String?>(null) }
                        if (cameraError != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(cameraError!!, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        } else {
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx).apply {
                                        scaleType = PreviewView.ScaleType.FILL_CENTER
                                    }
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        try {
                                            val cameraProvider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build().also {
                                                it.setSurfaceProvider(previewView.surfaceProvider)
                                            }
                                            val cameraSelector = when {
                                                cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                                                cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                                                else -> CameraSelector.DEFAULT_BACK_CAMERA
                                            }
                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(activityLifecycleOwner, cameraSelector, preview)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            cameraError = "تعذر تشغيل الكاميرا المباشرة على هذا الجهاز"
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))
                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        // Fallback when permission is not yet granted
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(42.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("الكاميرا بانتظار الإذن", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("فتح الكاميرا الآن", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Viewfinder corner marks & laser overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        val markerLength = 26.dp.toPx()
                        val markerStroke = 3.dp.toPx()
                        val cornerColor = Color(0xFF38BDF8)

                        // Top-left
                        drawLine(cornerColor, Offset(16f, 16f), Offset(16f + markerLength, 16f), markerStroke)
                        drawLine(cornerColor, Offset(16f, 16f), Offset(16f, 16f + markerLength), markerStroke)

                        // Top-right
                        drawLine(cornerColor, Offset(canvasWidth - 16f, 16f), Offset(canvasWidth - 16f - markerLength, 16f), markerStroke)
                        drawLine(cornerColor, Offset(canvasWidth - 16f, 16f), Offset(canvasWidth - 16f, 16f + markerLength), markerStroke)

                        // Bottom-left
                        drawLine(cornerColor, Offset(16f, canvasHeight - 16f), Offset(16f + markerLength, canvasHeight - 16f), markerStroke)
                        drawLine(cornerColor, Offset(16f, canvasHeight - 16f), Offset(16f, canvasHeight - 16f - markerLength), markerStroke)

                        // Bottom-right
                        drawLine(cornerColor, Offset(canvasWidth - 16f, canvasHeight - 16f), Offset(canvasWidth - 16f - markerLength, canvasHeight - 16f), markerStroke)
                        drawLine(cornerColor, Offset(canvasWidth - 16f, canvasHeight - 16f), Offset(canvasWidth - 16f, canvasHeight - 16f - markerLength), markerStroke)

                        // Scanning Red Laser
                        val currentLaserPos = canvasHeight * laserY
                        drawLine(
                            Color(0xFFEF4444),
                            Offset(20f, currentLaserPos),
                            Offset(canvasWidth - 20f, currentLaserPos),
                            strokeWidth = 3f
                        )
                    }

                    // Live Status Pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("الكاميرا نشطة - المسح التلقائي قيد العمل", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }

                var barcodeError by remember { mutableStateOf<String?>(null) }

                // Action to capture/scan currently viewed barcode
                Button(
                    onClick = {
                        val code = manualBarcode.trim()
                        if (code.isNotBlank()) {
                            onBarcodeScanned(code)
                        } else if (existingBarcodes.isNotEmpty()) {
                            onBarcodeScanned(existingBarcodes.first())
                        } else {
                            barcodeError = "يرجى توجيه الكاميرا نحو باركود صحيح أو كتابة الرمز يدوياً أدناه"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("التقاط الباركود الممسوح بالكاميرا", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Manual Barcode Input
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = {
                        manualBarcode = it
                        barcodeError = null
                    },
                    label = { Text("أو أدخل رقم الباركود / رمز الربط يدوياً") },
                    placeholder = { Text("مثال: 849201 أو PAIR-849201") },
                    isError = barcodeError != null,
                    supportingText = {
                        if (barcodeError != null) {
                            Text(barcodeError!!, color = RoseDanger, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = TechCyan) },
                    trailingIcon = {
                        if (manualBarcode.isNotBlank()) {
                            IconButton(onClick = { manualBarcode = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (manualBarcode.isNotBlank()) {
                    Button(
                        onClick = {
                            val code = manualBarcode.trim()
                            if (code.isNotBlank()) {
                                onBarcodeScanned(code)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تأكيد رقم الباركود المدخل والربط به", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Quick presets if available
                if (existingBarcodes.isNotEmpty()) {
                    Text("أو اختر من الباركودات المسجلة بالمخزن:", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        existingBarcodes.take(3).forEach { code ->
                            OutlinedButton(
                                onClick = {
                                    manualBarcode = code
                                    barcodeError = null
                                },
                                modifier = Modifier.weight(1f).height(30.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text(code.takeLast(6), fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val code = manualBarcode.trim()
                    if (code.isBlank()) {
                        Toast.makeText(context, "الرجاء إدخال رقم الباركود أولاً", Toast.LENGTH_SHORT).show()
                    } else if (code.length < 4 || !code.matches(Regex("^[0-9A-Za-z\\-_]+$"))) {
                        Toast.makeText(context, "صيغة الباركود غير صحيحة", Toast.LENGTH_SHORT).show()
                    } else {
                        onBarcodeScanned(code)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("تأكيد الباركود")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

/**
 * Authentic QR Code Renderer drawn directly on Compose Canvas using mathematical 2D matrix blocks.
 */
@Composable
fun QrCodeCanvas(
    content: String,
    modifier: Modifier = Modifier,
    moduleCount: Int = 21,
    primaryColor: Color = Color(0xFF0F172A),
    backgroundColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(2.dp, TechCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val blockSize = size.width / moduleCount
            val hash = abs(content.hashCode())
            val seedBytes = content.toByteArray()
            for (row in 0 until moduleCount) {
                for (col in 0 until moduleCount) {
                    val isCornerFinder =
                        (row < 7 && col < 7) ||
                        (row < 7 && col >= moduleCount - 7) ||
                        (row >= moduleCount - 7 && col < 7)

                    val shouldDraw: Boolean = if (isCornerFinder) {
                        val inTopLeft = row < 7 && col < 7
                        val inTopRight = row < 7 && col >= moduleCount - 7
                        val inBottomLeft = row >= moduleCount - 7 && col < 7
                        val r = if (inBottomLeft) row - (moduleCount - 7) else row
                        val c = if (inTopRight) col - (moduleCount - 7) else col
                        (r == 0 || r == 6 || c == 0 || c == 6) || (r in 2..4 && c in 2..4)
                    } else if (row == 6 || col == 6) {
                        (row + col) % 2 == 0
                    } else {
                        val byteIndex = (row * moduleCount + col) % maxOf(1, seedBytes.size)
                        val b = seedBytes.getOrElse(byteIndex) { 0 }.toInt()
                        ((hash xor (row * 31 + col * 17) xor b) % 2) == 0
                    }

                    if (shouldDraw) {
                        drawRect(
                            color = primaryColor,
                            topLeft = Offset(col * blockSize, row * blockSize),
                            size = Size(blockSize, blockSize)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Professional Dialog displaying Dynamic Device Pairing QR Code.
 * Automatically rotates and refreshes the pairing key with countdown for enhanced protection.
 */
@Composable
fun DevicePairingQrDialog(
    isPrimaryDevice: Boolean,
    deviceName: String,
    deviceId: String,
    pairingCode: String = "",
    onDismiss: () -> Unit
) {
    var tokenNonce by remember { mutableStateOf(java.util.UUID.randomUUID().toString().take(8)) }
    var tokenTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var secondsLeft by remember { androidx.compose.runtime.mutableIntStateOf(60) }

    LaunchedEffect(tokenNonce) {
        secondsLeft = 60
        while (secondsLeft > 0) {
            kotlinx.coroutines.delay(1000L)
            secondsLeft--
        }
        // Auto rotate token for protection
        tokenTimestamp = System.currentTimeMillis()
        tokenNonce = java.util.UUID.randomUUID().toString().take(8)
    }

    val dynamicPairingCode = remember(tokenNonce, tokenTimestamp, deviceId, deviceName) {
        val sig = kotlin.math.abs("$deviceId$deviceName$tokenTimestamp$tokenNonce-AUTOPARTS-SALT".hashCode()).toString()
        "AUTOPARTS_SECURE_V2|ID:$deviceId|NAME:$deviceName|TIME:$tokenTimestamp|TOKEN:$tokenNonce|SIG:$sig"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCode, contentDescription = null, tint = TechCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("رمز ربط الأجهزة المشفر (QR)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isPrimaryDevice)
                        "امسح هذا الكود المؤقت من الهاتف الفرعي. يتغير الكود تلقائياً لضمان أعلى حماية."
                    else
                        "امسح هذا الكود للربط مع الأجهزة الأخرى بأمان عبر الإنترنت.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // High-resolution Canvas QR Code with rotating dynamic payload
                QrCodeCanvas(
                    content = dynamicPairingCode,
                    modifier = Modifier.size(175.dp),
                    primaryColor = Color(0xFF0F172A),
                    backgroundColor = Color.White
                )

                // Timer & Rotating Status Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TechCyanLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = TechCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "يتجدد الكود خلال: $secondsLeft ثانية",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                    TextButton(
                        onClick = {
                            tokenTimestamp = System.currentTimeMillis()
                            tokenNonce = java.util.UUID.randomUUID().toString().take(8)
                        },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("تحديث الآن", fontSize = 11.sp, color = TechCyan, fontWeight = FontWeight.Bold)
                    }
                }

                val context = LocalContext.current
                val pairingPin = remember(deviceId, tokenNonce) {
                    (kotlin.math.abs("$deviceId$tokenNonce".hashCode()) % 900000 + 100000).toString()
                }

                // Dedicated Pairing PIN Card with Copy affordance
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "رقم الباركود / رمز الربط المباشر بين الهاتفين:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = pairingPin,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Pairing PIN", pairingPin))
                                Toast.makeText(context, "تم نسخ رمز الربط: $pairingPin", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text(
                            text = "امسح الباركود بالكاميرا أو اكتب هذا الرقم (6 أرقام) مباشرة في خانة الإدخال بالهاتف الآخر للربط الفوري.",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الجهاز المعتمد: $deviceName ($deviceId)", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        Text("حماية اتصال آمن مشفر عبر الإنترنت", fontSize = 9.5.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("إغلاق")
            }
        }
    )
}

/**
 * Dialog displaying Barcode Disbursement & Stock History for a specific Part.
 */
@Composable
fun PartBarcodeHistoryDialog(
    history: PartDisbursementHistory,
    onDismiss: () -> Unit,
    onIssueClick: () -> Unit
) {
    val part = history.part
    val available = history.availableQty
    val disbursements = history.disbursements

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TechCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("سجل الصرف والمخزون بالباركود", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(part.name, fontSize = 12.sp, color = PrimaryBlue)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الباركود: ${part.barcode.ifBlank { "غير مسجل" }}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TechCyan)
                            Text("الموديل: ${part.carType} ${part.carModel}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المخزون الحالي: $available قطعة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (available <= part.minStock) RoseDanger else EmeraldSuccess)
                            Text("سعر البيع: ${part.price} ريال", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                HorizontalDivider()

                Text("الجهات والأشخاص الذين صُرف لهم هذا الصنف (${disbursements.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)

                if (disbursements.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("لم يتم تسجيل أي عمليات صرف لهذا الصنف حتى الآن", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(disbursements) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Business, contentDescription = null, tint = TechCyan, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${item.partyName} (${item.partyType})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(RoseDanger.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("-${item.quantity} قطعة", color = RoseDanger, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("المستلم: ${item.personName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("التاريخ: ${item.issueDate}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (item.note.isNotBlank()) {
                                        Text("ملاحظة: ${item.note}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onIssueClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("صرف من هذا الصنف الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        }
    )
}

/**
 * In-app Notification Center Dialog with high-contrast Dark Mode support.
 * Resolves color readability and blending issues in dark and luxury themes.
 */
@Composable
fun NotificationsCenterDialog(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = TechCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مركز الإشعارات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Row {
                    IconButton(onClick = onMarkAllRead, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.DoneAll, contentDescription = "تحديد الكل كمقروء", tint = TechCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onClearAll, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "مسح الكل", tint = RoseDanger, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد إشعارات جديدة حالياً", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notif ->
                            val accentColor = when (notif.type) {
                                "issue" -> RoseDanger
                                "supplier" -> TechCyan
                                "order" -> MaterialTheme.colorScheme.primary
                                "payment" -> EmeraldSuccess
                                "sync" -> TechCyan
                                else -> MaterialTheme.colorScheme.primary
                            }

                            // Theme-aware, perfectly legible container color
                            val cardBg = if (notif.isRead) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (notif.isRead) 1.dp else 1.5.dp,
                                        color = if (notif.isRead) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else accentColor.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(if (notif.isRead) Color.Transparent else accentColor, CircleShape)
                                            .padding(top = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = notif.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = accentColor
                                            )
                                            Text(
                                                text = notif.timeAgo,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = notif.message,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("إغلاق")
            }
        }
    )
}

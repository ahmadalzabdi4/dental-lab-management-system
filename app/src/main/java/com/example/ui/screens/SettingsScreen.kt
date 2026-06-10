package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BackupData
import com.example.data.BackupHelper
import com.example.data.model.DentalCaseWithDetails
import com.example.data.model.Doctor
import com.example.data.model.Expense
import com.example.data.model.Technician
import com.example.ui.theme.*
import com.example.ui.viewmodel.DentalLabViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DentalLabViewModel,
    cases: List<DentalCaseWithDetails>,
    doctors: List<Doctor>,
    technicians: List<Technician>,
    expenses: List<Expense>
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showResetConfirm by remember { mutableStateOf(false) }
    var showPrepopulateConfirm by remember { mutableStateOf(false) }
    var showPasteImportDialog by remember { mutableStateOf(false) }
    var showVerifyImportDialog by remember { mutableStateOf<BackupData?>(null) }

    // ملف مستكشف لاستيراد JSON
    val pickJsonFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val contentResolver = context.contentResolver
                val stringBuilder = StringBuilder()
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line)
                        }
                    }
                }
                val rawJson = stringBuilder.toString()
                if (rawJson.isNotBlank()) {
                    val parsedBackup = BackupHelper.importFromJsonString(rawJson)
                    showVerifyImportDialog = parsedBackup
                } else {
                    Toast.makeText(context, "الملف فارغ ولا يحتوي على نصوص", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "خطأ في قراءة ملف النسخة: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // بطاقة ترويسة معلومات قاعدة البيانات والبرنامج
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SapphireLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = SapphirePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "نظام النسخ الاحتياطي والإعدادات",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SapphireDark
                            )
                        )
                    }

                    Text(
                        text = "هنا يمكنك نسخ كافة بيانات المختبر الطبي بشكل مشفر وآمن، ومشاركة الملف مع الأجهزة الأخرى لحماية مدخلاتك من الضياع والتلف في أي وقت.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    HorizontalDivider(color = SapphirePrimary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                    // إحصائيات البيانات
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatLabel(title = "الأطباء", value = "${doctors.size}")
                        StatLabel(title = "الفنيين", value = "${technicians.size}")
                        StatLabel(title = "الطلبات", value = "${cases.size}")
                        StatLabel(title = "المصروفات", value = "${expenses.size}")
                    }
                }
            }
        }

        // قسم أخذ النسخ الاحتياطية
        item {
            Text(
                text = "إشهار وتصدير البيانات",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SapphireDark,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsActionRow(
                        icon = Icons.Default.Share,
                        iconTint = SapphirePrimary,
                        title = "تصدير ومشاركة ملف النسخة الاحتياطية",
                        subtitle = "حفظ البيانات كملف JSON ومشاركته عبر الواتساب أو البريد أو التخزين الإلكتروني.",
                        onClick = {
                            try {
                                val currentCasesList = cases.map { it.dentalCase }
                                val backup = BackupData(
                                    doctors = doctors,
                                    technicians = technicians,
                                    cases = currentCasesList,
                                    expenses = expenses
                                )
                                val jsonStr = BackupHelper.exportToJsonString(backup)

                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, jsonStr)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "تصدير ومشاركة النسخة الاحتياطية")
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "خطأ أثناء محاولة التصدير: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    )

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

                    SettingsActionRow(
                        icon = Icons.Default.ContentCopy,
                        iconTint = SapphirePrimary,
                        title = "نسخ نص احتياطي خام للذاكرة",
                        subtitle = "نسخ نص البيانات (JSON) كاملاً ولصقه في مستنداتك الخارجية مباشرة.",
                        onClick = {
                            try {
                                val currentCasesList = cases.map { it.dentalCase }
                                val backup = BackupData(
                                    doctors = doctors,
                                    technicians = technicians,
                                    cases = currentCasesList,
                                    expenses = expenses
                                )
                                val jsonStr = BackupHelper.exportToJsonString(backup)
                                clipboardManager.setText(AnnotatedString(jsonStr))
                                Toast.makeText(context, "تم نسخ النص الاحتياطي بنجاح!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "خطأ في نسخ النص: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }

        // قسم استعادة النسخ الاحتياطية
        item {
            Text(
                text = "طرق استيراد واسترجاع البيانات",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SapphireDark,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsActionRow(
                        icon = Icons.Default.CloudUpload,
                        iconTint = ColorReady,
                        title = "اختيار واستيراد ملف نسخة احتياطية",
                        subtitle = "اختر ملف نسختك الاحتياطية بصيغة (JSON) من الهاتف وسنقوم بدمجها.",
                        onClick = {
                            try {
                                pickJsonFileLauncher.launch("text/*")
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر تشغيل مستكشف الملفات: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    )

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

                    SettingsActionRow(
                        icon = Icons.Default.AssignmentReturned,
                        iconTint = ColorInWork,
                        title = "لصق نص احتياطي واستعادته يدوياً",
                        subtitle = "إذا قمت بحفظ نص البيانات الاحتياطي في الملاحظات، الصقه هنا للاسترجاع الفوري.",
                        onClick = { showPasteImportDialog = true }
                    )
                }
            }
        }

        // إعدادات المطور وتحكم البيانات الكلي
        item {
            Text(
                text = "أدوات التحكم الشاملة والمصنع",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SapphireDark,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsActionRow(
                        icon = Icons.Default.Refresh,
                        iconTint = SapphirePrimary,
                        title = "تصفير وإعادة تحميل البيانات التجريبية للمعمل",
                        subtitle = "حذف البيانات الحالية بشكل كامل وإعادة تعذيتها بالبيانات الافتراضية الذكية.",
                        onClick = { showPrepopulateConfirm = true }
                    )

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

                    SettingsActionRow(
                        icon = Icons.Default.DeleteForever,
                        iconTint = ColorReturned,
                        title = "حذف وتصفير كافة محتويات البرنامج",
                        subtitle = "انتبه! هذا الخيار يحذف كل السجلات والطلبات والفنيين نهائياً ويبدأ من الصفر.",
                        onClick = { showResetConfirm = true }
                    )
                }
            }
        }

        // قسم معلومات المطور المعتمد للنظام
        item {
            Text(
                text = "معلومات مطور ومبرمج النظام",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SapphireDark,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SapphireLight),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SapphirePrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = SapphirePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "مطور النظام والحلول الرقمية",
                                fontSize = 11.sp,
                                color = TextTertiary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "أحمد الزبدي",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SapphireDark
                            )
                        }
                    }

                    HorizontalDivider(color = SapphirePrimary.copy(alpha = 0.12f))

                    // رقم الهاتف
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:770667143"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "تعذر تشغيل الاتصال: 770667143", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = SapphirePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "رقم الهاتف للتواصل المباشر", fontSize = 10.sp, color = TextTertiary)
                            Text(text = "770667143", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                        }
                        Text(text = "اتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SapphirePrimary)
                    }

                    // البريد الإلكتروني
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:ahmadzabad.new@gmail.com"))
                                    context.startActivity(emailIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "البريد: ahmadzabad.new@gmail.com", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = SapphirePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "البريد الإلكتروني المعتمد", fontSize = 10.sp, color = TextTertiary)
                            Text(text = "ahmadzabad.new@gmail.com", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                        }
                        Text(text = "إرسال رسالة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SapphirePrimary)
                    }
                }
            }
        }

        // تذييل معلومات المطورين
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "معمل العلوي لتقنية وتصميم الأسنان الرقمي",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = "الإصدار 1.0.0",
                    fontSize = 10.sp,
                    color = TextTertiary
                )
            }
        }
    }

    // 1. دايلوج تأكيد تصفير البيانات كلياً
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("تصفير وحذف كافة البيانات؟", fontWeight = FontWeight.Bold, color = ColorReturned) },
            text = { Text("هل أنت متأكد تماماً من رغبتك في حذف كل الحالات، الأطباء، الفنيين، والمصروفات بالكامل؟ هذه العملية غير قابلة للتراجع وتؤدي لمحو السجلات دفعة واحدة.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData {
                            Toast.makeText(context, "تم تصفير محركات البيانات للمعمل بنجاح", Toast.LENGTH_SHORT).show()
                        }
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorReturned)
                ) {
                    Text("نعم، احذف كلياً", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirm = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }

    // 2. دايلوج تأكيد تصفير وإعادة تعبئة البيانات التجريبية
    if (showPrepopulateConfirm) {
        AlertDialog(
            onDismissRequest = { showPrepopulateConfirm = false },
            title = { Text("إعادة التهيأة وتغذية البيانات؟", fontWeight = FontWeight.Bold, color = SapphirePrimary) },
            text = { Text("سيؤدي هذا الإجراء لحذف كافة التعديلات والبيانات الحالية وتغذية البرنامج من جديد بالأطباء والفنيين النموذجيين والعمليات المالية والطلبات والديون الافتراضية.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAndPrepopulate {
                            Toast.makeText(context, "تمت إعادة التهيئة بنجاح", Toast.LENGTH_SHORT).show()
                        }
                        showPrepopulateConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary)
                ) {
                    Text("نعم، ابدأ التهيئة", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPrepopulateConfirm = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }

    // 3. دايلوج لصق كود النسخ الاحتياطية يدوياً
    if (showPasteImportDialog) {
        var rawInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showPasteImportDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "لصق نص البيانات الاحتياطي",
                        fontWeight = FontWeight.Bold,
                        color = SapphireDark,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "الرجاء لصق نص الكود الكامل (JSON) في الصندوق أدناه ليتم تحليله واستعادة جدول أعمال المعمل مباشرة.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = rawInput,
                        onValueChange = { rawInput = it },
                        label = { Text("المحتوى النصي للبيانات", fontSize = 12.sp) },
                        placeholder = { Text("على شكل { ... }", fontSize = 12.sp) },
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (rawInput.isNotBlank()) {
                                    try {
                                        val parsed = BackupHelper.importFromJsonString(rawInput.trim())
                                        showVerifyImportDialog = parsed
                                        showPasteImportDialog = false
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "النص الذي أدخلته غير مطابق لمعايير قواعد البيانات: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("تحليل ومطالعة", color = Color.White)
                        }
                        OutlinedButton(
                            onClick = { showPasteImportDialog = false },
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text("إلغاء", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    // 4. دايلوج معاينة وتأكيد محتويات النسخة المستوردة لمنع التلاعب
    if (showVerifyImportDialog != null) {
        val backup = showVerifyImportDialog!!
        Dialog(onDismissRequest = { showVerifyImportDialog = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "مراجعة محتوى النسخة المرفقة",
                        fontWeight = FontWeight.Bold,
                        color = ColorReady,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "تم العثور على جداول بيانات بنجاح! يرجى مراجعة إحصائيات المواد أدناه قبل استبدال قاعدة بيانات معمل العلوي الحالية:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // عرض معلومات المطابقة
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElectricIce.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VerifyCountRow("عدد الأطباء المسجلين:", "${backup.doctors.size} أطباء")
                        VerifyCountRow("عدد الفنيين والشركاء كلياً:", "${backup.technicians.size} فنيين")
                        VerifyCountRow("إجمالي طلبات المرضى والحالات:", "${backup.cases.size} حالات")
                        VerifyCountRow("كافة المصاريف والحسابات المقيّدة:", "${backup.expenses.size} قيود")
                    }

                    Text(
                        text = "تحذير: سيتم حذف كافة السجلات والطلبات الحالية واستبدالها بهذه النسخة بالكامل. يرجى تأكيد العملية.",
                        fontSize = 11.sp,
                        color = ColorReturned,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.restoreBackupData(
                                    backupData = backup,
                                    onSuccess = {
                                        Toast.makeText(context, "تمت استعادة البيانات والدفاتر الحسابية للمعمل بنجاح تام!", Toast.LENGTH_LONG).show()
                                        showVerifyImportDialog = null
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, "فشلت عملية الاستيراد: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorReady),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text("تأكيد دمج واستعادة كود النسخة", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showVerifyImportDialog = null },
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Text("إلغاء", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatLabel(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = title, fontSize = 11.sp, color = TextTertiary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SapphireDark)
    }
}

@Composable
fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconTint.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SapphireDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun VerifyCountRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 12.sp, color = SapphireDark, fontWeight = FontWeight.Bold)
    }
}

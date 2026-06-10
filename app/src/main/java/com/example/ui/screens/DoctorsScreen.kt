package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DentalCaseWithDetails
import com.example.data.model.Doctor
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorsScreen(
    doctors: List<Doctor>,
    cases: List<DentalCaseWithDetails>,
    onAddDoctor: (Doctor) -> Unit,
    onUpdateDoctor: (Doctor) -> Unit,
    onDeleteDoctor: (Doctor) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var doctorToEdit by remember { mutableStateOf<Doctor?>(null) }
    var doctorToShowAccountStatement by remember { mutableStateOf<Doctor?>(null) }

    val filteredDoctors = doctors.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.clinicName.contains(searchQuery, ignoreCase = true)
    }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("ar", "YE")).apply { 
        maximumFractionDigits = 0
    } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SapphirePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة طبيب جديد", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // شريط البحث
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث عن طبيب أو عيادة...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SapphirePrimary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SapphirePrimary,
                    unfocusedBorderColor = BorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // عدد المستفيدين
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الأطباء المتعاملون (${doctors.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SapphireDark)
                )
            }

            // قائمة الأطباء
            if (filteredDoctors.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = TextTertiary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "لا توجد أطباء أو عيادات مطابقة للبحث",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredDoctors) { doc ->
                        // حساب إحصائيات الطبيب
                        val doctorCases = cases.filter { it.dentalCase.doctorId == doc.id }
                        val totalCasesCount = doctorCases.size
                        val totalBill = doctorCases.sumOf { it.dentalCase.price }
                        val totalPaid = doctorCases.sumOf { it.dentalCase.paidAmount }
                        val remainingDebt = totalBill - totalPaid

                        DoctorCard(
                            doctor = doc,
                            totalCases = totalCasesCount,
                            totalBill = totalBill,
                            remainingDebt = remainingDebt,
                            currencyFormatter = currencyFormatter,
                            onEditClick = { doctorToEdit = doc },
                            onDeleteClick = { onDeleteDoctor(doc) },
                            onStatementClick = { doctorToShowAccountStatement = doc }
                        )
                    }
                }
            }
        }
    }

    // مودال إضافة طبيب
    if (showAddDialog) {
        AddEditDoctorDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newDoc ->
                onAddDoctor(newDoc)
                showAddDialog = false
            }
        )
    }

    // مودال تعديل طبيب
    if (doctorToEdit != null) {
        AddEditDoctorDialog(
            doctorToEdit = doctorToEdit,
            onDismiss = { doctorToEdit = null },
            onSave = { updatedDoc ->
                onUpdateDoctor(updatedDoc)
                doctorToEdit = null
            }
        )
    }

    // كشف الحساب المتكامل للطبيب
    if (doctorToShowAccountStatement != null) {
        val doc = doctorToShowAccountStatement!!
        val doctorCases = cases.filter { it.dentalCase.doctorId == doc.id }
        AccountStatementDialog(
            doctor = doc,
            doctorCases = doctorCases,
            currencyFormatter = currencyFormatter,
            onDismiss = { doctorToShowAccountStatement = null }
        )
    }
}

@Composable
fun DoctorCard(
    doctor: Doctor,
    totalCases: Int,
    totalBill: Double,
    remainingDebt: Double,
    currencyFormatter: NumberFormat,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStatementClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // بيانات الطبيب الأساسية
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = SapphirePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = doctor.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SapphireDark
                            )
                        )
                    }
                    Text(
                        text = "العيادة: ${doctor.clinicName}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(start = 26.dp, top = 2.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(SapphirePrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = SapphirePrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(ColorReturned.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ColorReturned, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // الهاتف والعنوان
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 26.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = doctor.phone, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = doctor.address, fontSize = 13.sp, color = TextSecondary)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderColor.copy(alpha = 0.5f))

            // التفاصيل المحاسبية للحساب
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "إجمالي الأعمال: $totalCases حالات", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "المسحوبات: ${currencyFormatter.format(totalBill)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row {
                        Text(
                            text = "المديونية المترتبة: ",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = currencyFormatter.format(remainingDebt),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (remainingDebt > 0) ColorReturned else ColorReady
                        )
                    }
                }

                // فتح كشف الحساب
                Button(
                    onClick = onStatementClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireLight, contentColor = SapphireDark),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("كشف كلي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddEditDoctorDialog(
    doctorToEdit: Doctor? = null,
    onDismiss: () -> Unit,
    onSave: (Doctor) -> Unit
) {
    var fullName by remember { mutableStateOf(doctorToEdit?.fullName ?: "") }
    var clinicName by remember { mutableStateOf(doctorToEdit?.clinicName ?: "") }
    var phone by remember { mutableStateOf(doctorToEdit?.phone ?: "") }
    var address by remember { mutableStateOf(doctorToEdit?.address ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (doctorToEdit == null) "تسجيل طبيب أسنان جديد" else "تعديل تفاصيل الطبيب",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SapphireDark),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("الاسم الكامل للطبيب/العيادة د.", fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clinicName,
                    onValueChange = { clinicName = it },
                    label = { Text("اسم العيادة التابعة", fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف التواصل", fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("عنوان العيادة / المدينة بالتفصيل ", fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (fullName.isBlank() || phone.isBlank()) return@Button
                            val doc = Doctor(
                                id = doctorToEdit?.id ?: 0L,
                                fullName = fullName,
                                clinicName = clinicName,
                                phone = phone,
                                address = address,
                                createdAt = doctorToEdit?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(doc)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ البيانات", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun showDatePicker(context: Context, initialDate: Long?, onDateSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    if (initialDate != null) {
        calendar.timeInMillis = initialDate
    }
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val resultCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(resultCal.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@Composable
fun AccountStatementDialog(
    doctor: Doctor,
    doctorCases: List<DentalCaseWithDetails>,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale("ar")) }

    val filteredCases = remember(doctorCases, startDate, endDate) {
        doctorCases.filter { caseWithDetails ->
            val date = caseWithDetails.dentalCase.createdAt
            val matchStart = startDate?.let { date >= it } ?: true
            val matchEnd = endDate?.let { 
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = it
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                date <= calendar.timeInMillis
            } ?: true
            matchStart && matchEnd
        }.sortedByDescending { it.dentalCase.createdAt }
    }

    val totalBill = filteredCases.sumOf { it.dentalCase.price }
    val totalPaid = filteredCases.sumOf { it.dentalCase.paidAmount }
    val remainingDebt = totalBill - totalPaid

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "كشف حساب ذكي للأعمال المفصلة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SapphireDark),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // فلاتر التاريخ تفاعلية
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BorderColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "من تاريخ", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                .clickable {
                                    showDatePicker(context, startDate) { date ->
                                        startDate = date
                                    }
                                },
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = startDate?.let { dateFormatter.format(Date(it)) } ?: "البداية",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = if (startDate != null) SapphirePrimary else TextSecondary
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "إلى تاريخ", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                .clickable {
                                    showDatePicker(context, endDate) { date ->
                                        endDate = date
                                    }
                                },
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = endDate?.let { dateFormatter.format(Date(it)) } ?: "اليوم",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = if (endDate != null) SapphirePrimary else TextSecondary
                            )
                        }
                    }

                    if (startDate != null || endDate != null) {
                        IconButton(
                            onClick = {
                                startDate = null
                                endDate = null
                            },
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .size(28.dp)
                                .background(ColorReturned.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "تصفية",
                                tint = ColorReturned,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // ترويسة كشف حساب
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SapphireLight, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "د. ${doctor.fullName}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SapphireDark)
                    Text(text = "العيادة: ${doctor.clinicName}", fontSize = 13.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "إجمالي المطالبات", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = currencyFormatter.format(totalBill), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SapphireDark)
                        }
                        Column {
                            Text(text = "إجمالي دفعاتك", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = currencyFormatter.format(totalPaid), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorReady)
                        }
                        Column {
                            Text(text = "صافي المتبقي", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = currencyFormatter.format(remainingDebt), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = ColorReturned)
                        }
                    }
                }

                Text(
                    text = "دفتر قيود أعمال تركيبات الأسنان (${filteredCases.size}):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SapphireDark
                )

                // قائمة الأعمال لكشف الحساب
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredCases.isEmpty()) {
                        item {
                            Text(
                                text = "لا توجد أعمال سابقة مسجلة بالفترة المحددة.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        items(filteredCases) { caseWithDetails ->
                            val c = caseWithDetails.dentalCase
                            Surface(
                                color = ElectricIce.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "المريض: ${c.patientName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SapphireDark)
                                        Text(text = "${c.workType} (${c.material}) | لون ${c.shade}", fontSize = 11.sp, color = TextSecondary)
                                        Text(text = "تاريخ الاستلام: ${dateFormatter.format(Date(c.createdAt))}", fontSize = 10.sp, color = TextTertiary)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "السعر: ${currencyFormatter.format(c.price)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                                        Text(text = "المدفوع: ${currencyFormatter.format(c.paidAmount)}", fontSize = 11.sp, color = ColorReady, fontWeight = FontWeight.Bold)
                                        if (c.remainingAmount > 0) {
                                            Text(text = "باقي: ${currencyFormatter.format(c.remainingAmount)}", fontSize = 11.sp, color = ColorReturned, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                              }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // أزرار العمليات والخيارات
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { shareStatementWhatsApp(context, doctor, filteredCases, startDate, endDate) },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorReady),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة واتساب", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { printStatementHtml(context, doctor, filteredCases, startDate, endDate) },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طباعة كشف (PDF)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun shareStatementWhatsApp(context: Context, doctor: Doctor, doctorCases: List<DentalCaseWithDetails>, startDate: Long?, endDate: Long?) {
    val totalBill = doctorCases.sumOf { it.dentalCase.price }
    val totalPaid = doctorCases.sumOf { it.dentalCase.paidAmount }
    val remainingDebt = totalBill - totalPaid
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ar", "YE")).apply { 
        maximumFractionDigits = 0
    }
    val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
    val periodStr = if (startDate != null || endDate != null) {
        val startStr = startDate?.let { dateFormatter.format(Date(it)) } ?: "البداية"
        val endStr = endDate?.let { dateFormatter.format(Date(it)) } ?: "اليوم"
        "من $startStr إلى $endStr"
    } else {
        "كافة الأوقات"
    }
    
    val sb = java.lang.StringBuilder()
    sb.append("🦷 *معمل العلوي لتقنية الأسنان* 🦷\n")
    sb.append("للتصميم الرقمي والتعويضات السنية المتقدمة\n")
    sb.append("-----------------------------------------\n")
    sb.append("📋 *كشف حساب تفصيلي للأعمال والذمم* \n")
    sb.append("-----------------------------------------\n")
    sb.append("👤 *الطبيب:* د. ${doctor.fullName}\n")
    sb.append("🏥 *المستوصف/العيادة:* ${doctor.clinicName.ifEmpty { "غير محدد" }}\n")
    sb.append("📞 *الهاتف:* ${doctor.phone}\n")
    sb.append("📆 *الفترة المحددة:* $periodStr\n")
    sb.append("-----------------------------------------\n")
    sb.append("📊 *الخلاصة المالية المعملية:*\n")
    sb.append("💰 *إجمالي المسحوبات:* ${currencyFormatter.format(totalBill)}\n")
    sb.append("✅ *إجمالي الدفعات المسلمة:* ${currencyFormatter.format(totalPaid)}\n")
    sb.append("🚨 *صافي المديونية المتبقية:* ${currencyFormatter.format(remainingDebt)}\n")
    sb.append("-----------------------------------------\n")
    sb.append("📝 *بيان القيود والحالات تفصيلياً (${doctorCases.size} حالة):*\n\n")
    
    if (doctorCases.isEmpty()) {
        sb.append("لا توجد أعمال معملية مقيدة حالياً في هذه الفترة المحددة.\n")
    } else {
        doctorCases.forEachIndexed { index, caseWithDetails ->
            val c = caseWithDetails.dentalCase
            val unitPrice = if (c.teethCount > 0) c.price / c.teethCount else c.price
            sb.append("${index + 1}. المريض: *${c.patientName}*\n")
            sb.append("   🦷 العمل: ${c.workType} (${c.material}) | لون: ${c.shade}\n")
            sb.append("   📦 عدد الوحدات: ${c.teethCount} | سعر القطعة: ${currencyFormatter.format(unitPrice)}\n")
            sb.append("   💵 إجمالي الحساب: ${currencyFormatter.format(c.price)} | المدفوع: ${currencyFormatter.format(c.paidAmount)}\n")
            if (c.remainingAmount > 0) {
                sb.append("   ⚠️ المتبقي: ${currencyFormatter.format(c.remainingAmount)}\n")
            }
            sb.append("   📅 تاريخ: ${dateFormatter.format(Date(c.createdAt))}\n")
            sb.append("   -----------------\n")
        }
    }
    
    sb.append("\nنشكركم جزيل الشكر لتعاملكم الراقي معنا ✨\n")
    sb.append("إدارة معمل العلوي لتقنية الأسنان")
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "إرسال كشف الحساب عبر"))
}

fun printStatementHtml(context: Context, doctor: Doctor, doctorCases: List<DentalCaseWithDetails>, startDate: Long?, endDate: Long?) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ar", "YE")).apply { 
        maximumFractionDigits = 0
    }
    val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
    val periodStr = if (startDate != null || endDate != null) {
        val startStr = startDate?.let { dateFormatter.format(Date(it)) } ?: "البداية"
        val endStr = endDate?.let { dateFormatter.format(Date(it)) } ?: "اليوم"
        "من $startStr إلى $endStr"
    } else {
        "كافة الأوقات"
    }
    
    val listRowsHtml = java.lang.StringBuilder()
    doctorCases.forEach { caseWithDetails ->
        val c = caseWithDetails.dentalCase
        val unitPrice = if (c.teethCount > 0) c.price / c.teethCount else c.price
        val formattedDate = dateFormatter.format(Date(c.createdAt))
        listRowsHtml.append("""
            <tr style="border-bottom: 1px solid #cbd5e1; font-weight: 500;">
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1;">$formattedDate</td>
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1; font-weight: bold; color: #191C1E;">${c.patientName}</td>
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1;">${c.workType} (${c.material})</td>
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1; font-weight: bold;">${c.teethCount}</td>
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1;">${currencyFormatter.format(unitPrice)}</td>
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1; font-weight: bold;">${currencyFormatter.format(c.price)}</td>
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1; color: #16a34a; font-weight: bold;">${currencyFormatter.format(c.paidAmount)}</td>
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1; color: #dc2626; font-weight: bold;">${currencyFormatter.format(c.remainingAmount)}</td>
            </tr>
        """)
    }
    
    val totalTeethCount = doctorCases.sumOf { it.dentalCase.teethCount }
    val totalBill = doctorCases.sumOf { it.dentalCase.price }
    val totalPaid = doctorCases.sumOf { it.dentalCase.paidAmount }
    val remainingDebt = totalBill - totalPaid
    
    val html = """
        <html dir="rtl">
        <head>
            <meta charset="utf-8">
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    margin: 0;
                    padding: 20px;
                    color: #191C1E;
                    background-color: #ffffff;
                }
                .statement-box {
                    max-width: 1000px;
                    margin: auto;
                    padding: 24px;
                    border: 1px solid #e3e8ec;
                    border-radius: 12px;
                    box-shadow: 0 4px 6px rgba(0,0,0,0.02);
                }
                .footer-box {
                    margin-top: 40px;
                    width: 100%;
                }
                @media print {
                    body {
                        padding: 0;
                        background: none;
                    }
                    .statement-box {
                        border: none;
                        box-shadow: none;
                        padding: 0;
                    }
                }
            </style>
        </head>
        <body>
            <div class="statement-box">
                <!-- الذهب الدافئ والأزرق الملكي - ترويسة معمل العلوي الرسمية -->
                <div style="display: flex; align-items: center; justify-content: space-between; width: 100%; border-bottom: 3px double #006684; padding-bottom: 12px; margin-bottom: 20px;">
                    <div style="text-align: right; width: 33%;">
                        <div style="font-size: 20px; font-weight: 800; color: #006684; margin: 0;">معمل العلوي</div>
                        <div style="font-size: 14px; font-weight: bold; color: #191C1E; margin-top: 2px;">لتقنية الأسنان</div>
                        <div style="font-size: 11px; color: #555;">776020795</div>
                    </div>
                    <div style="text-align: center; width: 34%; display: flex; flex-direction: column; align-items: center; justify-content: center;">
                        <svg viewBox="0 0 100 100" width="55" height="55" style="fill: none; stroke: #006684; stroke-width: 3.5; stroke-linecap: round; stroke-linejoin: round;">
                            <path d="M 50 15 C 38 10, 15 20, 20 45 C 25 65, 35 70, 40 85 C 42 90, 48 90, 48 85 L 48 65 L 52 65 L 52 85 C 52 90, 58 90, 60 85 C 65 70, 75 65, 80 45 C 85 20, 62 10, 50 15 Z" fill="#E1F4FF" />
                            <path d="M 35 30 Q 50 20 65 30" />
                        </svg>
                        <div style="font-size: 8px; font-weight: bold; color: #006684; letter-spacing: 1px; margin-top: 5px;">DENTAL LAB AL-ALAWI</div>
                    </div>
                    <div style="text-align: left; width: 33%;" dir="ltr">
                        <div style="font-size: 18px; font-weight: 800; color: #006684; margin: 0;">Al-Alawi Dental</div>
                        <div style="font-size: 12px; font-weight: bold; color: #191C1E; margin-top: 2px;">Technology Lab</div>
                        <div style="font-size: 10px; color: #555;">YS-776020795</div>
                    </div>
                </div>

                <div style="text-align: center; margin: 25px 0;">
                    <span style="font-size: 20px; font-weight: 900; color: #006684; background-color: #e1f4ff; padding: 8px 30px; border-radius: 8px; border: 2px solid #006684;">كـشـف حـسـاب مـالـي تـفـصـيـلـي</span>
                </div>

                <table style="width: 100%; margin-bottom: 25px; border-collapse: collapse; font-size: 14px;">
                    <tr>
                        <td style="width: 50%; padding: 8px; border-bottom: 2px dashed #E1F4FF;">
                            <b style="color: #006684;">مستشفى / مستوصف:</b> <span style="font-size: 15px; font-weight: bold; color: #191C1E;">${doctor.clinicName.ifEmpty { "مستشفى/مستوصف غير حدد" }}</span>
                        </td>
                        <td style="width: 50%; padding: 8px; border-bottom: 2px dashed #E1F4FF;" dir="rtl">
                            <b style="color: #006684;">عيادة الدكتور/ة:</b> <span style="font-size: 16px; font-weight: bold; color: #191C1E;">د. ${doctor.fullName}</span>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; border-bottom: 2px dashed #E1F4FF;">
                            <b style="color: #006684;">رقم هاتف الاتصال:</b> <span style="color: #191C1E;">${doctor.phone}</span>
                        </td>
                        <td style="padding: 8px; border-bottom: 2px dashed #E1F4FF;" dir="rtl">
                            <b style="color: #006684;">الفترة المحددة:</b> <span style="font-size: 14px; font-weight: bold; color: #006684;">$periodStr</span>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; border-bottom: 2px dashed #E1F4FF;">
                            <b style="color: #006684;">تاريخ طباعة التقرير:</b> <span style="color: #191C1E;">${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar")).format(Date())}</span>
                        </td>
                        <td style="padding: 8px; border-bottom: 2px dashed #E1F4FF;" dir="rtl">
                            <b style="color: #006684;">حالة الحساب الإجمالية بالفترة:</b> <span style="font-weight: bold; color: ${if (remainingDebt > 0) "#dc2626" else "#16a34a"};">${if (remainingDebt > 0) "متبقي ذمم مالية معلقة" else "خالص ومسدد للكامل"}</span>
                        </td>
                    </tr>
                </table>

                <table style="width: 100%; border-collapse: collapse; margin-bottom: 30px; font-size: 12px; text-align: center; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 5px rgba(0,0,0,0.02);">
                    <thead>
                        <tr style="background-color: #006684; color: #ffffff;">
                            <th style="padding: 12px 6px; border: 1px solid #cbd5e1; font-weight: bold;">تاريخ الحالة</th>
                            <th style="padding: 12px 6px; border: 1px solid #cbd5e1; font-weight: bold;">اسم المريض</th>
                            <th style="padding: 12px 6px; border: 1px solid #cbd5e1; font-weight: bold;">نوع العمل والمادة</th>
                            <th style="padding: 12px 6px; border: 1px solid #cbd5e1; font-weight: bold;">عدد القطع</th>
                            <th style="padding: 12px 6px; border: 1px solid #cbd5e1; font-weight: bold;">سعر القطعة</th>
                            <th style="padding: 12px 6px; border: 1px solid #cbd5e1; font-weight: bold;">إجمالي السعر</th>
                            <th style="padding: 12px 10px; border: 1px solid #cbd5e1; font-weight: bold; background-color: #16a34a; color: white;">المسلم (المدفوع)</th>
                            <th style="padding: 12px 10px; border: 1px solid #cbd5e1; font-weight: bold; background-color: #dc2626; color: white;">الباقي</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${if (listRowsHtml.isEmpty()) "<tr><td colspan='8' style='padding: 20px; color: gray;'>لا توجد أعمال معملية بالفترة المحددة</td></tr>" else listRowsHtml.toString()}
                        <tr style="background-color: #e1f4ff; font-weight: bold; border-top: 3px double #006684;">
                            <td colspan="3" style="padding: 12px; border: 1px solid #cbd5e1; font-size: 14px; color: #006684; text-align: center;">الإجمالي العام الكلي لهذه الفترة المحددة</td>
                            <td style="padding: 12px; border: 1px solid #cbd5e1; font-size: 14px; text-align: center;">$totalTeethCount وحدات</td>
                            <td style="padding: 12px; border: 1px solid #cbd5e1; background-color: #f8fafc;">-</td>
                            <td style="padding: 12px; border: 1px solid #cbd5e1; font-size: 14px;">${currencyFormatter.format(totalBill)}</td>
                            <td style="padding: 12px; border: 1px solid #cbd5e1; font-size: 14px; color: #16a34a;">${currencyFormatter.format(totalPaid)}</td>
                            <td style="padding: 12px; border: 1px solid #cbd5e1; font-size: 15px; color: #dc2626;">${currencyFormatter.format(remainingDebt)}</td>
                        </tr>
                    </tbody>
                </table>

                <table class="footer-box" style="border-collapse: collapse;">
                    <tr>
                        <td style="width: 50%; vertical-align: bottom; font-size: 13px; color: #40484C; border-top: 1px dashed #cbd5e1; padding-top: 15px;">
                            <b>معمل العلوي لتقنية الأسنان</b><br>
                            نشكركم على حسن ثقتكم ودوام تواصلكم المالي والمهني معنا.<br>
                            يُرجى مراجعة إرسال الإيصالات في حال تسليم مبالغ دفعات.
                        </td>
                        <td style="width: 50%; text-align: left; vertical-align: bottom; font-weight: bold; font-size: 14px; color: #191C1E; padding-top: 15px;">
                            <div style="margin-bottom: 50px;">إدارة المعمل</div>
                            <div>توقيع وختم المعمل المعتمَد: _________________</div>
                        </td>
                    </tr>
                </table>
            </div>
        </body>
        </html>
    """.trimIndent()
    
    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "معمل_العلوي_كشف_حساب_${doctor.fullName}"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
}

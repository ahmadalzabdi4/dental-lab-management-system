package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DentalCase
import com.example.data.model.DentalCaseWithDetails
import com.example.data.model.Doctor
import com.example.data.model.Technician
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasesScreen(
    cases: List<DentalCaseWithDetails>,
    doctors: List<Doctor>,
    technicians: List<Technician>,
    onAddCase: (DentalCase) -> Unit,
    onUpdateCase: (DentalCase) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onAddPayment: (Long, Double) -> Unit,
    onDeleteCase: (DentalCase) -> Unit,
    onAddDoctor: (Doctor) -> Unit,
    onAddTechnician: (Technician) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("الكل") }

    var showAddDialog by remember { mutableStateOf(false) }
    var showPaymentDialogForCaseId by remember { mutableStateOf<Long?>(null) }
    var showStatusDialogForCaseId by remember { mutableStateOf<Long?>(null) }
    var caseToEdit by remember { mutableStateOf<DentalCaseWithDetails?>(null) }
    var showInvoiceForCase by remember { mutableStateOf<DentalCaseWithDetails?>(null) }

    // تصفية الحالات حسب البحث وحالة العمل
    val filteredCases = cases.filter { caseWithDetails ->
        val matchesSearch = caseWithDetails.dentalCase.patientName.contains(searchQuery, ignoreCase = true) ||
                (caseWithDetails.doctorName?.contains(searchQuery, ignoreCase = true) ?: false)
        val matchesStatus = selectedStatusFilter == "الكل" || 
                caseWithDetails.dentalCase.status == selectedStatusFilter
        matchesSearch && matchesStatus
    }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("ar", "YE")).apply { 
        maximumFractionDigits = 0
    } }
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale("ar")) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SapphirePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp) // ارتفاع الألسنة السفلية لتجنب تداخل الأزرار
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة حالة جديدة", modifier = Modifier.size(28.dp))
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
            // شريط البحث والفرز
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث باسم المريض أو الطبيب...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SapphirePrimary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SapphirePrimary,
                    unfocusedBorderColor = BorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ألسنة تصفية سريعة بالحالات
            val statusFilters = listOf("الكل", "جديد", "قيد العمل", "جاهز للتسليم", "تم التسليم", "مرتجع")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusFilters) { status ->
                    val isSelected = selectedStatusFilter == status
                    val statusColor = when (status) {
                        "جديد" -> ColorNew
                        "قيد العمل" -> ColorInWork
                        "جاهز للتسليم" -> ColorReady
                        "تم التسليم" -> ColorDelivered
                        "مرتجع" -> ColorReturned
                        else -> SapphirePrimary
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = status },
                        label = { Text(status, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = statusColor,
                            selectedLabelColor = Color.White,
                            containerColor = statusColor.copy(alpha = 0.08f),
                            labelColor = statusColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = statusColor,
                            selectedBorderColor = statusColor
                        )
                    )
                }
            }

            // قائمة الحالات
            if (filteredCases.isEmpty()) {
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
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = TextTertiary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "لا توجد أعمال مطابقة للبحث",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredCases, key = { it.dentalCase.id }) { caseWithDetails ->
                        CaseCard(
                            caseWithDetails = caseWithDetails,
                            currencyFormatter = currencyFormatter,
                            dateFormatter = dateFormatter,
                            onAddPaymentClick = { showPaymentDialogForCaseId = caseWithDetails.dentalCase.id },
                            onStatusClick = { showStatusDialogForCaseId = caseWithDetails.dentalCase.id },
                            onEditClick = { caseToEdit = caseWithDetails },
                            onDeleteClick = { onDeleteCase(caseWithDetails.dentalCase) },
                            onInvoiceClick = { showInvoiceForCase = caseWithDetails }
                        )
                    }
                }
            }
        }
    }

    // مودال إضافة حالة جديدة
    if (showAddDialog) {
        AddEditCaseDialog(
            doctors = doctors,
            technicians = technicians,
            onDismiss = { showAddDialog = false },
            onSave = { newCase ->
                onAddCase(newCase)
                showAddDialog = false
            },
            onAddDoctor = onAddDoctor,
            onAddTechnician = onAddTechnician
        )
    }

    // مودال تعديل حالة
    if (caseToEdit != null) {
        AddEditCaseDialog(
            caseToEdit = caseToEdit,
            doctors = doctors,
            technicians = technicians,
            onDismiss = { caseToEdit = null },
            onSave = { updatedCase ->
                onUpdateCase(updatedCase)
                caseToEdit = null
            },
            onAddDoctor = onAddDoctor,
            onAddTechnician = onAddTechnician
        )
    }

    // مودال تسجيل دفعة مالية
    if (showPaymentDialogForCaseId != null) {
        val caseId = showPaymentDialogForCaseId!!
        val targetCase = cases.find { it.dentalCase.id == caseId }?.dentalCase
        if (targetCase != null) {
            PaymentDialog(
                dentalCase = targetCase,
                currencyFormatter = currencyFormatter,
                onDismiss = { showPaymentDialogForCaseId = null },
                onConfirm = { amount ->
                    onAddPayment(caseId, amount)
                    showPaymentDialogForCaseId = null
                }
            )
        }
    }

    // مودال تغيير الحالة
    if (showStatusDialogForCaseId != null) {
        val caseId = showStatusDialogForCaseId!!
        val currentStatus = cases.find { it.dentalCase.id == caseId }?.dentalCase?.status ?: "جديد"
        StatusSelectionDialog(
            currentStatus = currentStatus,
            onDismiss = { showStatusDialogForCaseId = null },
            onSelect = { newStatus ->
                onUpdateStatus(caseId, newStatus)
                showStatusDialogForCaseId = null
            }
        )
    }

    if (showInvoiceForCase != null) {
        InvoiceDialog(
            caseWithDetails = showInvoiceForCase!!,
            onDismiss = { showInvoiceForCase = null }
        )
    }
}

@Composable
fun CaseCard(
    caseWithDetails: DentalCaseWithDetails,
    currencyFormatter: NumberFormat,
    dateFormatter: SimpleDateFormat,
    onAddPaymentClick: () -> Unit,
    onStatusClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onInvoiceClick: () -> Unit
) {
    val dentalCase = caseWithDetails.dentalCase
    val statusColor = when (dentalCase.status) {
        "جديد" -> ColorNew
        "قيد العمل" -> ColorInWork
        "جاهز للتسليم", "جاهز" -> ColorReady
        "تم التسليم" -> ColorDelivered
        "مرتجع" -> ColorReturned
        else -> SapphirePrimary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // الصف الأول: اسم المريض والحالة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dentalCase.patientName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SapphireDark
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${caseWithDetails.doctorName ?: "طبيب غير معروف"} | ${caseWithDetails.doctorClinic ?: ""}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    contentColor = statusColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onStatusClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = dentalCase.status,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تغيير الحالة",
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderColor.copy(alpha = 0.5f))

            // معلومات الأسنان والعمل
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = SapphirePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "نوع العمل: ", fontSize = 13.sp, color = TextSecondary)
                        Text(text = dentalCase.workType, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BorderOuter, contentDescription = null, tint = SapphirePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "الأسنان: ", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "${dentalCase.teethNumbers} (${dentalCase.teethCount} وحدات)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = ZirconiaWarm, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "اللون والمادة: ", fontSize = 13.sp, color = TextSecondary)
                        Text(text = "${dentalCase.shade} - ${dentalCase.material}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                    }
                }

                Column(
                    modifier = Modifier.widthIn(max = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = SapphirePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "الفني: ", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            text = caseWithDetails.technicianName ?: "غير محدد",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SapphireDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SapphirePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "التسليم: ", fontSize = 13.sp, color = TextSecondary)
                        Text(text = dateFormatter.format(Date(dentalCase.dueDate)), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ZirconiaWarm)
                    }
                }
            }

            if (dentalCase.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = ElectricIce.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ملاحظات المعمل: ${dentalCase.notes}",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderColor.copy(alpha = 0.5f))

            // البيانات المالية للبطاقة والأزرار السريعة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "الحساب: ${currencyFormatter.format(dentalCase.price)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "المدفوع: ${currencyFormatter.format(dentalCase.paidAmount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorReady
                        )
                    }
                    if (dentalCase.remainingAmount > 0) {
                        Text(
                            text = "المتبقي: ${currencyFormatter.format(dentalCase.remainingAmount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorReturned
                        )
                    } else {
                        Text(
                            text = "تم السداد بالكامل",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorReady
                        )
                    }
                }

                // أزرار العمليات والخيارات
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onInvoiceClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(SapphirePrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "عرض الفاتورة والمشاركة", tint = SapphirePrimary, modifier = Modifier.size(18.dp))
                    }

                    if (dentalCase.remainingAmount > 0) {
                        IconButton(
                            onClick = onAddPaymentClick,
                            modifier = Modifier
                                .size(36.dp)
                                .background(ColorReady.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = "دفع مبلغ", tint = ColorReady, modifier = Modifier.size(18.dp))
                        }
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(SapphirePrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل الحالة", tint = SapphirePrimary, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(ColorReturned.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف الحالة", tint = ColorReturned, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCaseDialog(
    caseToEdit: DentalCaseWithDetails? = null,
    doctors: List<Doctor>,
    technicians: List<Technician>,
    onDismiss: () -> Unit,
    onSave: (DentalCase) -> Unit,
    onAddDoctor: (Doctor) -> Unit,
    onAddTechnician: (Technician) -> Unit
) {
    var patientName by remember { mutableStateOf(caseToEdit?.dentalCase?.patientName ?: "") }
    var selectedDoctorId by remember { mutableStateOf(caseToEdit?.dentalCase?.doctorId ?: (doctors.firstOrNull()?.id ?: -1L)) }
    var selectedTechnicianId by remember { mutableStateOf(caseToEdit?.dentalCase?.technicianId ?: -1L) }
    var teethNumbers by remember { mutableStateOf(caseToEdit?.dentalCase?.teethNumbers ?: "") }
    var teethCount by remember { mutableStateOf(caseToEdit?.dentalCase?.teethCount?.toString() ?: "1") }
    var workType by remember { mutableStateOf(caseToEdit?.dentalCase?.workType ?: "تاج (Crown)") }
    var shade by remember { mutableStateOf(caseToEdit?.dentalCase?.shade ?: "A2") }
    var material by remember { mutableStateOf(caseToEdit?.dentalCase?.material ?: "زيركون (Zirconia)") }
    var price by remember { mutableStateOf(caseToEdit?.dentalCase?.price?.toString() ?: "") }
    var paidAmount by remember { mutableStateOf(caseToEdit?.dentalCase?.paidAmount?.toString() ?: "0") }
    var notes by remember { mutableStateOf(caseToEdit?.dentalCase?.notes ?: "") }
    var daysSlider by remember { mutableStateOf(4f) } // الإمداد بالتسليم التلقائي (أيام)

    val workTypes = listOf("تاج (Crown)", "جسر (Bridge)", "فينير (Veneer)", "طقم كامل (Full Denture)", "طقم جزئي (Partial)", "حشوة مصبوبة (Inlay)", "زرعة سنيّة (Implant)")
    val shades = listOf("A1", "A2", "A3", "A3.5", "A4", "B1", "B2", "B3", "B4", "C1", "C2", "C3", "C4", "D2", "D3", "D4", "BL1", "BL2", "BL3", "BL4")
    val materials = listOf("زيركون (Zirconia)", "إيماكس (E-Max)", "بورسلين (PFM/خزف)", "أكريل (Acrylic)", "فيتاليوم (Vitallium)", "مؤقت (Temporary)")

    var docExpanded by remember { mutableStateOf(false) }
    var techExpanded by remember { mutableStateOf(false) }
    var workExpanded by remember { mutableStateOf(false) }
    var shadeExpanded by remember { mutableStateOf(false) }
    var materialExpanded by remember { mutableStateOf(false) }

    var showQuickAddDoc by remember { mutableStateOf(false) }
    var showQuickAddTech by remember { mutableStateOf(false) }

    var recentlyAddedDocName by remember { mutableStateOf<String?>(null) }
    var recentlyAddedTechName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(doctors) {
        recentlyAddedDocName?.let { name ->
            doctors.find { it.fullName == name }?.let { matched ->
                selectedDoctorId = matched.id
                recentlyAddedDocName = null
            }
        }
    }

    LaunchedEffect(technicians) {
        recentlyAddedTechName?.let { name ->
            technicians.find { it.fullName == name }?.let { matched ->
                selectedTechnicianId = matched.id
                recentlyAddedTechName = null
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = if (caseToEdit == null) "إضافة عمل/حالة جديدة" else "تعديل تفاصيل الحالة",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SapphireDark
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // اسم المريض
                item {
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        label = { Text("اسم المريض بالكامل", fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // اختيار الطبيب
                item {
                    Text("الطبيب وعيادة الأسنان المعالجة", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = SapphireDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    val currentDocName = doctors.find { it.id == selectedDoctorId }?.let { "${it.fullName} - ${it.clinicName}" } ?: "اختر طبيباً..."
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { docExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SapphireLight, contentColor = SapphireDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(currentDocName, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                            DropdownMenu(
                                expanded = docExpanded,
                                onDismissRequest = { docExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                doctors.forEach { doc ->
                                    DropdownMenuItem(
                                        text = { Text("${doc.fullName} (${doc.clinicName})", fontSize = 14.sp) },
                                        onClick = {
                                            selectedDoctorId = doc.id
                                            docExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { showQuickAddDoc = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(SapphirePrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "أضف طبيب سريع", tint = SapphirePrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // أرقام الأسنان وعددها
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = teethNumbers,
                            onValueChange = { teethNumbers = it },
                            label = { Text("أرقام الأسنان (مثل: 12, 11)", fontSize = 12.sp) },
                            placeholder = { Text("عبر الفاصلة", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = teethCount,
                            onValueChange = { scale ->
                                if (scale.all { it.isDigit() }) teethCount = scale
                            },
                            label = { Text("العدد", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }

                // طبيعة ونوع العمل
                item {
                    Text("طبيعة العمل المطلوب", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = SapphireDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { workExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricIce, contentColor = SapphireDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(workType, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = workExpanded,
                            onDismissRequest = { workExpanded = false }
                        ) {
                            workTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, fontSize = 14.sp) },
                                    onClick = {
                                        workType = type
                                        workExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // اللون والمادة المستخدمة
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("اللون (Shade)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { shadeExpanded = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIce, contentColor = SapphireDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(shade, fontSize = 14.sp)
                                }
                                DropdownMenu(
                                    expanded = shadeExpanded,
                                    onDismissRequest = { shadeExpanded = false }
                                ) {
                                    shades.forEach { sh ->
                                        DropdownMenuItem(
                                            text = { Text(sh) },
                                            onClick = {
                                                shade = sh
                                                shadeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("المادة التكوينية", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { materialExpanded = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIce, contentColor = SapphireDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(material, fontSize = 14.sp)
                                }
                                DropdownMenu(
                                    expanded = materialExpanded,
                                    onDismissRequest = { materialExpanded = false }
                                ) {
                                    materials.forEach { mat ->
                                        DropdownMenuItem(
                                            text = { Text(mat) },
                                            onClick = {
                                                material = mat
                                                materialExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // المبالغ المالية
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("السعر الكلي (ريال)", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = paidAmount,
                            onValueChange = { paidAmount = it },
                            label = { Text("مدفوع حالياً (ريال)", fontSize = 12.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // فني تقنية الأسنان المكلف بالعمل
                item {
                    Text("تعميم الحالة على الفني المكلف", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = SapphireDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    val currentTechName = technicians.find { it.id == selectedTechnicianId }?.fullName ?: "غير مخصص لفني محدد"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { techExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SapphireLight, contentColor = SapphireDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(currentTechName, fontSize = 13.sp, maxLines = 1)
                            }
                            DropdownMenu(
                                expanded = techExpanded,
                                onDismissRequest = { techExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("غير مخصص لفني محدد") },
                                    onClick = {
                                        selectedTechnicianId = -1L
                                        techExpanded = false
                                    }
                                )
                                technicians.forEach { tech ->
                                    DropdownMenuItem(
                                        text = { Text("${tech.fullName} (${tech.specialty})") },
                                        onClick = {
                                            selectedTechnicianId = tech.id
                                            techExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { showQuickAddTech = true },
                            modifier = Modifier
                                .size(40.dp)
                                .background(SapphirePrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "أضف فني سريع", tint = SapphirePrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // اختيار تاريخ الاستحقاق عبر شريط التمرير بدلاً من المكونات المعقدة المنهارة
                item {
                    Text("ميعاد الاستحقاق والتسليم بالأيام", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Slider(
                            value = daysSlider,
                            onValueChange = { daysSlider = it },
                            valueRange = 1f..14f,
                            steps = 13,
                            colors = SliderDefaults.colors(thumbColor = SapphirePrimary, activeTrackColor = SapphirePrimary),
                            modifier = Modifier.weight(1.3f)
                        )
                        Text(
                            text = "خلال ${daysSlider.toInt()} أيام",
                            fontWeight = FontWeight.Bold,
                            color = SapphirePrimary,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(0.7f),
                            textAlign = TextAlign.End
                        )
                    }
                }

                // ملاحظات إضافية
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات أو توصيات طبية للطبيب فني المعمل", fontSize = 12.sp) },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // أزرار الحفظ والإلغاء
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (patientName.isBlank() || price.isBlank() || selectedDoctorId == -1L) return@Button

                                val finalPrice = price.toDoubleOrNull() ?: 0.0
                                val finalPaid = paidAmount.toDoubleOrNull() ?: 0.0
                                val finalTeethCount = teethCount.toIntOrNull() ?: 1
                                val dueTime = System.currentTimeMillis() + (daysSlider.toInt() * 24 * 60 * 60 * 1000L)

                                val finalCase = DentalCase(
                                    id = caseToEdit?.dentalCase?.id ?: 0L,
                                    doctorId = selectedDoctorId,
                                    technicianId = selectedTechnicianId,
                                    patientName = patientName,
                                    teethNumbers = teethNumbers.ifBlank { "غير محدد" },
                                    teethCount = finalTeethCount,
                                    workType = workType,
                                    shade = shade,
                                    material = material,
                                    status = caseToEdit?.dentalCase?.status ?: "جديد",
                                    price = finalPrice,
                                    paidAmount = finalPaid,
                                    createdAt = caseToEdit?.dentalCase?.createdAt ?: System.currentTimeMillis(),
                                    dueDate = dueTime,
                                    notes = notes
                                )
                                onSave(finalCase)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("حفظ العمل", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text("إلغاء", color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // دايلوج إضافة طبيب سريع
    if (showQuickAddDoc) {
        Dialog(onDismissRequest = { showQuickAddDoc = false }) {
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
                        text = "تسجيل طبيب جديد سريع",
                        fontWeight = FontWeight.Bold,
                        color = SapphireDark,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    var qDocName by remember { mutableStateOf("") }
                    var qDocClinic by remember { mutableStateOf("") }
                    var qDocPhone by remember { mutableStateOf("") }
                    
                    OutlinedTextField(
                        value = qDocName,
                        onValueChange = { qDocName = it },
                        label = { Text("اسم الطبيب بالكامل", fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = qDocClinic,
                        onValueChange = { qDocClinic = it },
                        label = { Text("اسم العيادة", fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = qDocPhone,
                        onValueChange = { qDocPhone = it },
                        label = { Text("رقم الهاتف", fontSize = 13.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (qDocName.isNotBlank() && qDocClinic.isNotBlank()) {
                                    val newDoc = Doctor(
                                        fullName = qDocName,
                                        clinicName = qDocClinic,
                                        phone = qDocPhone.ifBlank { "غير مسجل" },
                                        address = "غير محدد"
                                    )
                                    recentlyAddedDocName = qDocName
                                    onAddDoctor(newDoc)
                                    showQuickAddDoc = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("إضافة وحفظ", color = Color.White)
                        }
                        OutlinedButton(
                            onClick = { showQuickAddDoc = false },
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text("إلغاء", color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // دايلوج إضافة فني سريع
    if (showQuickAddTech) {
        Dialog(onDismissRequest = { showQuickAddTech = false }) {
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
                        text = "تسجيل فني جديد سريع",
                        fontWeight = FontWeight.Bold,
                        color = SapphireDark,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    var qTechName by remember { mutableStateOf("") }
                    var qTechSpec by remember { mutableStateOf("") }
                    var qTechPhone by remember { mutableStateOf("") }
                    
                    OutlinedTextField(
                        value = qTechName,
                        onValueChange = { qTechName = it },
                        label = { Text("اسم الفني بالكامل", fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = qTechSpec,
                        onValueChange = { qTechSpec = it },
                        label = { Text("التخصص (مثل: خزف، زيركون)", fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = qTechPhone,
                        onValueChange = { qTechPhone = it },
                        label = { Text("رقم الهاتف", fontSize = 13.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (qTechName.isNotBlank() && qTechSpec.isNotBlank()) {
                                    val newTech = Technician(
                                        fullName = qTechName,
                                        specialty = qTechSpec,
                                        phone = qTechPhone.ifBlank { "غير مسجل" },
                                        isActive = true
                                    )
                                    recentlyAddedTechName = qTechName
                                    onAddTechnician(newTech)
                                    showQuickAddTech = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("إضافة وحفظ", color = Color.White)
                        }
                        OutlinedButton(
                            onClick = { showQuickAddTech = false },
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text("إلغاء", color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentDialog(
    dentalCase: DentalCase,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    val maxPayable = dentalCase.remainingAmount

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "تسجيل دفعة مالية جديدة",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SapphireDark),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "المريض: ${dentalCase.patientName}\n" +
                            "الحساب الكلي: ${currencyFormatter.format(dentalCase.price)}\n" +
                            "المتبقي المطلوب: ${currencyFormatter.format(maxPayable)}",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("القيمة المدفوعة (ريال)", fontSize = 13.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirm(amt)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorReady),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تأكيد العملية", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("الغاء", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusSelectionDialog(
    currentStatus: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val statuses = listOf("جديد", "قيد العمل", "جاهز للتسليم", "تم التسليم", "مرتجع")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "تحديث حالة العمل والمرحلة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SapphireDark),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                statuses.forEach { status ->
                    val isCurrent = currentStatus == status
                    val statusColor = when (status) {
                        "جديد" -> ColorNew
                        "قيد العمل" -> ColorInWork
                        "جاهز للتسليم" -> ColorReady
                        "تم التسليم" -> ColorDelivered
                        "مرتجع" -> ColorReturned
                        else -> SapphirePrimary
                    }

                    Surface(
                        color = if (isCurrent) statusColor else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(status) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isCurrent) Color.White else statusColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = status,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Color.White else Color.Black
                                )
                            }
                            if (isCurrent) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إلغاء", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

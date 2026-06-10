package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DentalCaseWithDetails
import com.example.data.model.Technician
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechniciansScreen(
    technicians: List<Technician>,
    cases: List<DentalCaseWithDetails>,
    onAddTechnician: (Technician) -> Unit,
    onUpdateTechnician: (Technician) -> Unit,
    onDeleteTechnician: (Technician) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var technicianToEdit by remember { mutableStateOf<Technician?>(null) }

    val filteredTechnicians = technicians.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.specialty.contains(searchQuery, ignoreCase = true)
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
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة فني جديد", modifier = Modifier.size(28.dp))
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
                placeholder = { Text("بحث عن فني أسنان أو تخصص...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SapphirePrimary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SapphirePrimary,
                    unfocusedBorderColor = BorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // عدد الفنيين العاملين
            Text(
                text = "كادر فنيي تقنية الأسنان بالمعمل (${technicians.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SapphireDark)
            )

            // قائمة الفنيين
            if (filteredTechnicians.isEmpty()) {
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
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = TextTertiary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "لا توجد نتائج بحث مطابقة للفنيين",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredTechnicians) { tech ->
                        // حساب إحصائيات الفني: إجمالي الحالات المكلف بها، والحالات النشطة
                        val techCases = cases.filter { it.dentalCase.technicianId == tech.id }
                        val activeCases = techCases.count { it.dentalCase.status == "جديد" || it.dentalCase.status == "قيد العمل" }
                        val finishedCases = techCases.count { it.dentalCase.status == "جاهز للتسليم" || it.dentalCase.status == "تم التسليم" }

                        TechnicianCard(
                            tech = tech,
                            activeCasesCount = activeCases,
                            finishedCasesCount = finishedCases,
                            currencyFormatter = currencyFormatter,
                            onEditClick = { technicianToEdit = tech },
                            onDeleteClick = { onDeleteTechnician(tech) }
                        )
                    }
                }
            }
        }
    }

    // مودال إضافة فني
    if (showAddDialog) {
        AddEditTechnicianDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newTech ->
                onAddTechnician(newTech)
                showAddDialog = false
            }
        )
    }

    // مودال تعديل فني
    if (technicianToEdit != null) {
        AddEditTechnicianDialog(
            techToEdit = technicianToEdit,
            onDismiss = { technicianToEdit = null },
            onSave = { updatedTech ->
                onUpdateTechnician(updatedTech)
                technicianToEdit = null
            }
        )
    }
}

@Composable
fun TechnicianCard(
    tech: Technician,
    activeCasesCount: Int,
    finishedCasesCount: Int,
    currencyFormatter: NumberFormat,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // اسم الفني وتخصصه
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tech.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SapphireDark
                        )
                    )
                    Text(
                        text = "التخصص: ${tech.specialty}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SapphirePrimary, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // الأزرار السريعة
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

            // رقم الهاتف والراتب المتفق عليه
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = tech.phone, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "الراتب/النسبة شهرياً: ", fontSize = 13.sp, color = TextSecondary)
                    Text(text = currencyFormatter.format(tech.salary), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ZirconiaWarm)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderColor.copy(alpha = 0.5f))

            // إحصائيات ضغط العمل الموزع
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(text = "حالات قيد التنفيذ", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "$activeCasesCount حالات", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 14.sp,
                            color = if (activeCasesCount > 0) ColorInWork else TextTertiary
                        )
                    }
                    Column {
                        Text(text = "حالات منجزة قريبة", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "$finishedCasesCount حالات", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 14.sp,
                            color = ColorReady
                        )
                    }
                }

                // شارة لوصف النشاط بالمعمل
                Surface(
                    color = if (tech.isActive) ColorReady.copy(alpha = 0.15f) else ElectricIce.copy(alpha = 0.5f),
                    contentColor = if (tech.isActive) ColorReady else TextSecondary,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (tech.isActive) "نشط حالياً بالمعمل" else "غير نشط مؤقتاً",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditTechnicianDialog(
    techToEdit: Technician? = null,
    onDismiss: () -> Unit,
    onSave: (Technician) -> Unit
) {
    var fullName by remember { mutableStateOf(techToEdit?.fullName ?: "") }
    var phone by remember { mutableStateOf(techToEdit?.phone ?: "") }
    var specialty by remember { mutableStateOf(techToEdit?.specialty ?: "تركيبات ثابتة - CAD/CAM") }
    var salary by remember { mutableStateOf(techToEdit?.salary?.toString() ?: "") }
    var isActive by remember { mutableStateOf(techToEdit?.isActive ?: true) }

    val specialties = listOf(
        "تركيبات ثابتة - CAD/CAM",
        "تركيبات متحركة وأكريل",
        "تلوين زيركون وخزف تجميلي",
        "تصميم رقمي تجميلي (Digital)",
        "تقويم الأسنان والأجهزة",
        "صب المعادن والجبس الهيكلي"
    )
    var testExpanded by remember { mutableStateOf(false) }

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
                    text = if (techToEdit == null) "إضافة فني تقني بالمعمل" else "تعديل بيانات الفني",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = SapphireDark),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("الاسم الكامل للفني", fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف الفني للاتصال", fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                // التخصص الفني
                Text("التخصص العلمي للفني بالمعمل", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { testExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireLight, contentColor = SapphireDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(specialty, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = testExpanded,
                        onDismissRequest = { testExpanded = false }
                    ) {
                        specialties.forEach { spec ->
                            DropdownMenuItem(
                                text = { Text(spec, fontSize = 14.sp) },
                                onClick = {
                                    specialty = spec
                                    testExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("الراتب الشهري المتفق عليه (ريال)", fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SapphirePrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                // هل النشاط ساري؟
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("موظف نشط حالياً بالمعمل؟", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ColorReady, 
                            checkedTrackColor = ColorReady.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (fullName.isBlank() || phone.isBlank()) return@Button
                            val tech = Technician(
                                id = techToEdit?.id ?: 0L,
                                fullName = fullName,
                                phone = phone,
                                specialty = specialty,
                                salary = salary.toDoubleOrNull() ?: 0.0,
                                isActive = isActive,
                                joinDate = techToEdit?.joinDate ?: System.currentTimeMillis()
                            )
                            onSave(tech)
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

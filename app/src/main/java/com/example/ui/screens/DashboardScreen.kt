package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DentalCaseWithDetails
import com.example.data.model.Doctor
import com.example.data.model.Expense
import com.example.data.model.Technician
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    cases: List<DentalCaseWithDetails>,
    doctors: List<Doctor>,
    technicians: List<Technician>,
    expenses: List<Expense>,
    onNavigateToTab: (Int) -> Unit // تنقل سريع لألسنة التبويب الأخرى
) {
    // حساب الإحصائيات المالية للـ Dashboard
    val totalRevenue = cases.sumOf { it.dentalCase.price }
    val totalPaid = cases.sumOf { it.dentalCase.paidAmount }
    val totalDebt = totalRevenue - totalPaid
    val totalExpensesSum = expenses.sumOf { it.amount }
    val netCashFlow = totalPaid - totalExpensesSum

    // حالات الطلبات
    val newCasesCount = cases.count { it.dentalCase.status == "جديد" }
    val inProgressCount = cases.count { it.dentalCase.status == "قيد العمل" }
    val readyCount = cases.count { it.dentalCase.status == "جاهز للتسليم" || it.dentalCase.status == "جاهز" }
    val deliveredCount = cases.count { it.dentalCase.status == "تم التسليم" }
    val returnedCount = cases.count { it.dentalCase.status == "مرتجع" }

    // المواد الأكثر استخداماً
    val materialCounts = cases.groupBy { it.dentalCase.material }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("ar", "YE")).apply { 
        maximumFractionDigits = 0
    } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // بطاقة الترحيب والهوية
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SapphireDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "معمل العلوي لتقنية الأسنان",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مرحباً بك في نظام الإدارة الذكي والمتابعة المالية الشاملة للمعمل",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

        // لوحة الأرقام المالية الرئيسية
        item {
            Text(
                text = "الخلاصة المالية والمستحقات",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SapphireDark
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2
            ) {
                val cardWidthModifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                
                StatCard(
                    title = "إجمالي التوريدات (المبيعات)",
                    value = formatter.format(totalRevenue),
                    icon = Icons.Default.Inventory,
                    color = SapphirePrimary,
                    modifier = cardWidthModifier
                )

                StatCard(
                    title = "إجمالي التحصيل (المقبوضات)",
                    value = formatter.format(totalPaid),
                    icon = Icons.Default.Payments,
                    color = ColorReady,
                    modifier = cardWidthModifier
                )

                StatCard(
                    title = "الديون المتبقية (عند الأطباء)",
                    value = formatter.format(totalDebt),
                    icon = Icons.Default.TrendingDown,
                    color = ColorInWork,
                    modifier = cardWidthModifier
                )

                StatCard(
                    title = "إجمالي مصروفات المعمل",
                    value = formatter.format(totalExpensesSum),
                    icon = Icons.Default.AccountBalanceWallet,
                    color = ColorReturned,
                    modifier = cardWidthModifier
                )
            }
        }

        // بطاقة صافي التدفق المالي (الربح الحقيقي)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (netCashFlow >= 0) ColorReady.copy(alpha = 0.1f) else ColorReturned.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "صافي النقد الحالي (الربح المحصل)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatter.format(netCashFlow),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netCashFlow >= 0) ColorReady else ColorReturned,
                                fontSize = 22.sp
                            )
                        )
                    }
                    Icon(
                        imageVector = if (netCashFlow >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (netCashFlow >= 0) ColorReady else ColorReturned,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // إحصائيات حالات العمل
        item {
            Text(
                text = "حالة أعمال وتركيبات عيادات الأسنان",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SapphireDark
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        item {
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
                        Text(
                            text = "توزيع الحالات حسب مرحلة الإنجاز (${cases.size} حالة)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // شريط الحالة البصري التراكمي
                    val totalCases = cases.size.coerceAtLeast(1).toFloat()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BorderColor.copy(alpha = 0.5f))
                    ) {
                        if (newCasesCount > 0) {
                            Box(modifier = Modifier.weight(newCasesCount / totalCases).fillMaxHeight().background(ColorNew))
                        }
                        if (inProgressCount > 0) {
                            Box(modifier = Modifier.weight(inProgressCount / totalCases).fillMaxHeight().background(ColorInWork))
                        }
                        if (readyCount > 0) {
                            Box(modifier = Modifier.weight(readyCount / totalCases).fillMaxHeight().background(ColorReady))
                        }
                        if (deliveredCount > 0) {
                            Box(modifier = Modifier.weight(deliveredCount / totalCases).fillMaxHeight().background(ColorDelivered))
                        }
                        if (returnedCount > 0) {
                            Box(modifier = Modifier.weight(returnedCount / totalCases).fillMaxHeight().background(ColorReturned))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // تفاصيل نسب الحالات
                    val statusList = listOf(
                        Triple("جديد", newCasesCount, ColorNew),
                        Triple("قيد العمل", inProgressCount, ColorInWork),
                        Triple("جاهز للتسليم", readyCount, ColorReady),
                        Triple("تم التسليم", deliveredCount, ColorDelivered),
                        Triple("مرتجع", returnedCount, ColorReturned)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        statusList.forEach { (statusName, count, color) ->
                            val percent = if (totalCases > 0) (count / totalCases * 100).toInt() else 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = statusName, fontSize = 14.sp)
                                }
                                Text(
                                    text = "$count حالة ($percent%)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
        }

        // إحصائيات سريعة للعيادات والمواد والفنيين
        item {
            Text(
                text = "المواد التجميلية الأكثر طلباً في المعمل",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SapphireDark
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (materialCounts.isEmpty()) {
                        Text(
                            text = "لا توجد حالات مسجلة بعد لإحصاء المواد الأكثر طلباً.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        val maxCount = materialCounts.maxOf { it.second }.coerceAtLeast(1).toFloat()
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            materialCounts.take(4).forEach { (materialName, count) ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = materialName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                        Text(text = "$count وحدة تجميلية", fontWeight = FontWeight.Bold, color = SapphirePrimary, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { count / maxCount },
                                        color = ZirconiaWarm,
                                        trackColor = SapphireLight,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // أرقام سريعة للمعمل
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickMetricBox(
                    title = "عدد الأطباء الأصدقاء",
                    count = doctors.size.toString(),
                    icon = Icons.Default.MedicalServices,
                    onClick = { onNavigateToTab(2) },
                    modifier = Modifier.weight(1f)
                )
                QuickMetricBox(
                    title = "فنيي تقنية الأسنان",
                    count = technicians.size.toString(),
                    icon = Icons.Default.People,
                    onClick = { onNavigateToTab(3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun QuickMetricBox(
    title: String,
    count: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SapphirePrimary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = count,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

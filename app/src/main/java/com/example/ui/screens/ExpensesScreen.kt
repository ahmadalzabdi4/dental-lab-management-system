package com.example.ui.screens

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Expense
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    expenses: List<Expense>,
    onAddExpense: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit
) {
    val context = LocalContext.current
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("الكل", "مواد معملية", "صيانة وإصلاح", "إيجارات ومرافق", "رواتب موظفين", "أخرى")

    // تصفية المصروفات حسب الفئة
    val filteredExpenses = expenses.filter {
        selectedCategoryFilter == "الكل" || it.category == selectedCategoryFilter
    }

    val totalAmountSum = filteredExpenses.sumOf { it.amount }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("ar", "YE")).apply { 
        maximumFractionDigits = 0
    } }
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar")) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ColorReturned, // مصروفات نربطها باللون الأحمر/النبيتي التعبيري
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "تسجيل مصروف جديد", modifier = Modifier.size(28.dp))
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
            // كرت بملخص المصروفات من الفئة المصفاة
            Card(
                colors = CardDefaults.cardColors(containerColor = ColorReturned.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ColorReturned.copy(alpha = 0.2f))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي مصروفات المعمل (${selectedCategoryFilter})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorReturned
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currencyFormatter.format(totalAmountSum),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorReturned
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = ColorReturned,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // فلاتر الفئات سريعة التمرير
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ColorReturned,
                            selectedLabelColor = Color.White,
                            containerColor = ColorReturned.copy(alpha = 0.05f),
                            labelColor = ColorReturned
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = ColorReturned,
                            selectedBorderColor = ColorReturned
                        )
                    )
                }
            }

            // جدول القيود
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجلات القيود المالية للمصروفات والبنود",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SapphireDark),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { printExpensesHtml(context, filteredExpenses, selectedCategoryFilter) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(SapphirePrimary.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "طباعة تقرير مصروفات المصادفة",
                        tint = SapphirePrimary
                    )
                }
            }

            if (filteredExpenses.isEmpty()) {
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
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = TextTertiary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "لا توجد قيود مصروفات مسجلة لهذه الفئة",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredExpenses, key = { it.id }) { exp ->
                        ExpenseItemRow(
                            expense = exp,
                            currencyFormatter = currencyFormatter,
                            dateFormatter = dateFormatter,
                            onDelete = { onDeleteExpense(exp) }
                        )
                    }
                }
            }
        }
    }

    // مودال إضافة مصروف
    if (showAddDialog) {
        AddExpenseDialog(
            categories = categories.filter { it != "الكل" },
            onDismiss = { showAddDialog = false },
            onSave = { newExp ->
                onAddExpense(newExp)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ExpenseItemRow(
    expense: Expense,
    currencyFormatter: NumberFormat,
    dateFormatter: SimpleDateFormat,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = ColorReturned.copy(alpha = 0.1f),
                        contentColor = ColorReturned,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = expense.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = dateFormatter.format(Date(expense.date)),
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                if (expense.notes.isNotEmpty()) {
                    Text(
                        text = "ملاحظة: ${expense.notes}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = currencyFormatter.format(expense.amount),
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorReturned,
                    fontSize = 15.sp
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .background(ColorReturned.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف القيد من السجل",
                        tint = ColorReturned,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "أخرى") }
    var notes by remember { mutableStateOf("") }

    var catExpanded by remember { mutableStateOf(false) }

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
                    text = "تسجيل قيد مصروف معملي جديد",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = ColorReturned),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("بيان تفاصيل المصروف", fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorReturned),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("قيمة المبلغ المصروف (ريال)", fontSize = 13.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorReturned),
                    modifier = Modifier.fillMaxWidth()
                )

                // اختيار فئة الصرف
                Text("فئة وتبويب المصروف", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { catExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorReturned.copy(alpha = 0.1f), contentColor = ColorReturned),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedCategory, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, fontSize = 14.sp) },
                                onClick = {
                                    selectedCategory = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية على قيد التبويب", fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorReturned),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (title.isBlank() || amount.isBlank()) return@Button
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt <= 0) return@Button
                            
                            val exp = Expense(
                                title = title,
                                category = selectedCategory,
                                amount = amt,
                                notes = notes,
                                date = System.currentTimeMillis()
                            )
                            onSave(exp)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorReturned),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ المصروف", color = Color.White, fontWeight = FontWeight.Bold)
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

fun printExpensesHtml(context: Context, expenses: List<Expense>, categoryFilter: String) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ar", "YE")).apply { 
        maximumFractionDigits = 0
    }
    val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar"))
    
    val listRowsHtml = java.lang.StringBuilder()
    expenses.forEach { exp ->
        val formattedDate = dateFormatter.format(Date(exp.date))
        listRowsHtml.append("""
            <tr style="border-bottom: 1px solid #cbd5e1; font-weight: 500;">
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1;">$formattedDate</td>
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1; font-weight: bold; color: #191C1E; text-align: right;">${exp.title}</td>
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1; color: #b45309; font-weight: bold;">${exp.category}</td>
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1; font-weight: bold; color: #dc2626;">${currencyFormatter.format(exp.amount)}</td>
                <td style="padding: 10px 4px; border: 1px solid #cbd5e1; color: #555; text-align: right;">${exp.notes.ifEmpty { "لا توجد ملاحظات" }}</td>
            </tr>
        """)
    }
    
    val totalSum = expenses.sumOf { it.amount }
    
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
                <div style="display: flex; align-items: center; justify-content: space-between; width: 100%; border-bottom: 3px double #0E1B2F; padding-bottom: 12px; margin-bottom: 20px;">
                    <div style="text-align: right; width: 33%;">
                        <div style="font-size: 20px; font-weight: 800; color: #0E1B2F; margin: 0; font-family: 'Segoe UI', sans-serif;">معمل العلوي</div>
                        <div style="font-size: 14px; font-weight: bold; color: #0284C7; margin-top: 2px;">لتقنية الأسنان</div>
                        <div style="font-size: 11px; color: #EA580C; font-weight: bold; margin-top: 1px;">هاتف: 776020795</div>
                    </div>
                    <div style="text-align: center; width: 34%; display: flex; flex-direction: column; align-items: center; justify-content: center;">
                        <svg viewBox="0 0 160 100" width="90" height="60" style="display: block;">
                            <defs>
                                <linearGradient id="expWaveGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                                    <stop offset="0%" stop-color="#0E1B2F" />
                                    <stop offset="50%" stop-color="#0284C7" />
                                    <stop offset="100%" stop-color="#60A5FA" />
                                </linearGradient>
                            </defs>
                            <!-- السن الأيسر (أزرق كحلي داكن) -->
                            <path d="M 45,25 C 35,15 20,25 24,55 C 26,65 35,78 40,84 C 42,87 45,87 46,81 C 48,72 48,55 48,50 C 42,42 45,35 45,25 Z" fill="none" stroke="#0E1B2F" stroke-width="4.5" stroke-linecap="round" stroke-linejoin="round" />
                            <!-- السن الأيمن العلوي (ذهبي) -->
                            <path d="M 45,25 C 55,15 70,25 66,45" fill="none" stroke="#FAC015" stroke-width="4.5" stroke-linecap="round" />
                            <!-- السن الأيمن السفلي (برتقالي) -->
                            <path d="M 66,45 C 63,55 58,70 54,81 C 53,84 50,87 48,84 C 47,82 47,75 48,65" fill="none" stroke="#EA580C" stroke-width="4.5" stroke-linecap="round" stroke-linejoin="round" />
                            <!-- الموجة الديناميكية الانسيابية ممتدة لليمين -->
                            <path d="M 18,30 C 25,65 38,72 55,50 C 70,35 110,65 145,45" fill="none" stroke="url(#expWaveGrad)" stroke-width="4" stroke-linecap="round" />
                        </svg>
                        <div style="font-size: 8px; font-weight: bold; color: #0E1B2F; letter-spacing: 0.5px; margin-top: 3px;">DENTAL LAB AL-ALAWI</div>
                    </div>
                    <div style="text-align: left; width: 33%;" dir="ltr">
                        <div style="font-size: 18px; font-weight: 800; color: #0E1B2F; margin: 0; font-family: 'Segoe UI', sans-serif;">Al-Alawi Dental</div>
                        <div style="font-size: 12px; font-weight: bold; color: #0284C7; margin-top: 2px;">Technology Lab</div>
                        <div style="font-size: 10px; color: #EA580C; font-weight: bold; margin-top: 1px;">YS-776020795</div>
                    </div>
                </div>

                <div style="text-align: center; margin: 25px 0;">
                    <span style="font-size: 20px; font-weight: 900; color: #0E1B2F; background-color: #F0F9FF; padding: 8px 30px; border-radius: 8px; border: 2px solid #0E1B2F;">تـقـريـر مـصـروفـات الـمـعـمـل الـمـالـي</span>
                </div>

                <table style="width: 100%; margin-bottom: 25px; border-collapse: collapse; font-size: 14px;">
                    <tr>
                        <td style="width: 50%; padding: 8px; border-bottom: 2px dashed #0284C7;">
                            <b style="color: #0E1B2F;">فئة المصفاة:</b> <span style="font-size: 15px; font-weight: bold; color: #000;">$categoryFilter</span>
                        </td>
                        <td style="width: 50%; padding: 8px; border-bottom: 2px dashed #0284C7;" dir="rtl">
                            <b style="color: #0E1B2F;">عدد القيود المصروفة:</b> <span style="font-size: 16px; font-weight: bold; color: #000;">${expenses.size} قيود</span>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; border-bottom: 2px dashed #0284C7;">
                            <b style="color: #0E1B2F;">إجمالي المبالغ المصروفة:</b> <span style="font-weight: bold; color: #dc2626; font-size: 15px;">${currencyFormatter.format(totalSum)}</span>
                        </td>
                        <td style="padding: 8px; border-bottom: 2px dashed #0284C7;" dir="rtl">
                            <b style="color: #0E1B2F;">تاريخ إنشاء التقرير:</b> <span style="color: #000;">${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar")).format(Date())}</span>
                        </td>
                    </tr>
                </table>

                <table style="width: 100%; border-collapse: collapse; margin-bottom: 30px; font-size: 12px; text-align: center; border-radius: 8px; overflow: hidden;">
                    <thead>
                        <tr style="background-color: #0E1B2F; color: #ffffff;">
                            <th style="padding: 12px 6px; border: 1px solid #cbd5e1; font-weight: bold; width: 20%;">تاريخ ووقت القيد</th>
                            <th style="padding: 12px 6px; border: 1px solid #cbd5e1; font-weight: bold; width: 35%;">بيان ووصف المصروف (البيان الكلي)</th>
                            <th style="padding: 12px 6px; border: 1px solid #cbd5e1; font-weight: bold; width: 15%;">فئة الصرف</th>
                            <th style="padding: 12px 6px; border: 1px solid #cbd5e1; font-weight: bold; width: 15%;">المبلغ المالي بريال</th>
                            <th style="padding: 12px 6px; border: 1px solid #cbd5e1; font-weight: bold; width: 15%;">ملاحظات توثيقية</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${if (listRowsHtml.isEmpty()) "<tr><td colspan='5' style='padding: 20px; color: gray;'>لا توجد سجلات مصروفات مقيدة لهذه الفئة</td></tr>" else listRowsHtml.toString()}
                        <tr style="background-color: #f0f9ff; font-weight: bold; border-top: 3px double #0E1B2F;">
                            <td colspan="3" style="padding: 12px; border: 1px solid #cbd5e1; font-size: 14px; color: #0E1B2F; text-align: center;">الإجمالي العام الكلي للمصروف المفلتر</td>
                            <td style="padding: 12px; border: 1px solid #cbd5e1; font-size: 15px; color: #dc2626;">${currencyFormatter.format(totalSum)}</td>
                            <td style="padding: 12px; border: 1px solid #cbd5e1; background-color: #f8fafc;">-</td>
                        </tr>
                    </tbody>
                </table>

                <table class="footer-box" style="border-collapse: collapse;">
                    <tr>
                        <td style="width: 50%; vertical-align: bottom; font-size: 13px; color: #40484C; border-top: 1px dashed #cbd5e1; padding-top: 15px;">
                            <b>معمل العلوي لتقنية الأسنان - الإدارة الحسابية والمالية</b><br>
                            تم تدقيق واعتماد مخرجات هذه الحسابات والبيانات الصادرة من النظام الحسابي للمعمل للعمل بها رسمياً.<br>
                        </td>
                        <td style="width: 50%; text-align: left; vertical-align: bottom; font-weight: bold; font-size: 14px; color: #191C1E; padding-top: 15px;">
                            <div style="margin-bottom: 50px;">التوقيع المعتمد للإدارة المالية</div>
                            <div>توقيع وختم الإدارة الحسابية: _________________</div>
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
            val jobName = "معمل_العلوي_تقرير_المصروفات_${categoryFilter}"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
}

package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DentalCaseWithDetails
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InvoiceDialog(
    caseWithDetails: DentalCaseWithDetails,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dentalCase = caseWithDetails.dentalCase
    val doctorName = caseWithDetails.doctorName ?: "غير محدد"
    val doctorClinic = caseWithDetails.doctorClinic ?: ""
    val techName = caseWithDetails.technicianName ?: "غير محدد"

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("ar", "YE")).apply { 
        maximumFractionDigits = 0
    } }
    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale("ar")) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // رأسية النافذة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فاتورة سند القبض للعمل السني",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = SapphirePrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                // كرت الفاتورة (محاكاة شكل إيصال ورقي فاخر)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SapphireLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "معمل العلوي لتقنية الأسنان",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = SapphireDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "الجيل الثالث للتصميم الرقمي والروابط الطبية المعملية",
                            fontSize = 9.sp,
                            color = SapphirePrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 12.sp
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = SopWithDottedStyle(SapphirePrimary.copy(alpha = 0.3f))
                        )

                        // بيانات الطرفين
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "اسم المريض:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = dentalCase.patientName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "الطبيب المعالج:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(text = "د. $doctorName", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                        }
                        if (doctorClinic.isNotEmpty()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "العيادة:", fontSize = 11.sp, color = TextSecondary)
                                Text(text = doctorClinic, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SapphireDark)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = SapphirePrimary.copy(alpha = 0.1f)
                        )

                        // بيانات التركيبة التقنية السنية
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "نوع العمل والمادة:", fontSize = 11.sp, color = TextSecondary)
                            Text(text = "${dentalCase.workType} (${dentalCase.material})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "اللون (Shade):", fontSize = 11.sp, color = TextSecondary)
                            Text(text = dentalCase.shade, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ZirconiaWarm)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "الأسنان المستهدفة:", fontSize = 11.sp, color = TextSecondary)
                            Text(text = "${dentalCase.teethNumbers} (${dentalCase.teethCount} وحدات)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "الفني المسؤول:", fontSize = 11.sp, color = TextSecondary)
                            Text(text = techName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SapphireDark)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = SopWithDottedStyle(SapphirePrimary.copy(alpha = 0.3f))
                        )

                        // الحسابات المالية للسند
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "إجمالي فاتورة الحساب:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = currencyFormatter.format(dentalCase.price), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "المبلغ المدفوع (سند قبض):", fontSize = 12.sp, color = ColorReady, fontWeight = FontWeight.Bold)
                            Text(text = currencyFormatter.format(dentalCase.paidAmount), fontSize = 13.sp, color = ColorReady, fontWeight = FontWeight.ExtraBold)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SapphirePrimary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "المتبقي المطلوب:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (dentalCase.remainingAmount > 0) ColorReturned else SapphirePrimary
                            )
                            Text(
                                text = currencyFormatter.format(dentalCase.remainingAmount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (dentalCase.remainingAmount > 0) ColorReturned else SapphirePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // أزرار العمليات والخيارات
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { shareInvoiceWhatsApp(context, caseWithDetails) },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorReady),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إرسال الفاتورة عبر واتساب",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { printInvoiceHtml(context, caseWithDetails) },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "طباعة الفاتورة والملخص (PDF)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// دالة لمعرفة لون الخط المقطع
@Composable
fun SopWithDottedStyle(color: Color): Color {
    return color
}

fun shareInvoiceWhatsApp(context: Context, caseWithDetails: DentalCaseWithDetails) {
    val dentalCase = caseWithDetails.dentalCase
    val doctorName = caseWithDetails.doctorName ?: "غير محدد"
    val doctorClinic = caseWithDetails.doctorClinic ?: ""
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ar", "YE")).apply { 
        maximumFractionDigits = 0
    }
    val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))

    val text = """
🦷 *معمل العلوي لتقنية الأسنان* 🦷
الجيل الثالث للتصميم الرقمي والروابط الطبية المعملية
-----------------------------------------
📋 *سند قيد وفاتورة عمل سني ومستند قبض*
-----------------------------------------
🏷️ *رقم الفاتورة:* DL-${dentalCase.id}
👤 *اسم المريض:* ${dentalCase.patientName}
👨‍⚕️ *الطبيب المعالج:* د. ${doctorName}
🏥 *العيادة:* ${doctorClinic.ifEmpty { "غير محدد" }}
-----------------------------------------
🔹 *نوع التركيبة:* ${dentalCase.workType}
🧪 *المادة التكوينية:* ${dentalCase.material}
🎨 *اللون (Shade):* ${dentalCase.shade}
🦷 *قطاعات الأسنان:* ${dentalCase.teethNumbers} (${dentalCase.teethCount} وحدات)
-----------------------------------------
📅 *تاريخ القيد:* ${dateFormatter.format(Date(dentalCase.createdAt))}
📅 *تاريخ الاستحقاق:* ${dateFormatter.format(Date(dentalCase.dueDate))}
-----------------------------------------
💰 *إجمالي حساب الفاتورة:* ${currencyFormatter.format(dentalCase.price)}
✅ *المبلغ المدفوع (سند قبض):* ${currencyFormatter.format(dentalCase.paidAmount)}
🚨 *صافي المتبقي المطلوب:* ${currencyFormatter.format(dentalCase.remainingAmount)}
-----------------------------------------
نشكركم جزيل الشكر لثقتكم الغالية وتعاملكم الراقي معنا ✨
إدارة معمل العلوي لتقنية الأسنان
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "إرسال الفاتورة عبر"))
}

fun printInvoiceHtml(context: Context, caseWithDetails: DentalCaseWithDetails) {
    val dentalCase = caseWithDetails.dentalCase
    val doctorName = caseWithDetails.doctorName ?: "غير محدد"
    val doctorClinic = caseWithDetails.doctorClinic ?: "عيادة غير محددة"
    val techName = caseWithDetails.technicianName ?: "غير محدد"
    val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ar", "YE")).apply { 
        maximumFractionDigits = 0
    }

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
                .invoice-box {
                    max-width: 800px;
                    margin: auto;
                    padding: 24px;
                    border: 1px solid #e3e8ec;
                    border-radius: 12px;
                    box-shadow: 0 4px 6px rgba(0,0,0,0.02);
                }
                .section-title {
                    font-size: 15px;
                    font-weight: bold;
                    color: #0E1B2F;
                    border-bottom: 2px dashed #0284C7;
                    padding-bottom: 6px;
                    margin-top: 25px;
                    margin-bottom: 15px;
                }
                .details-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 25px;
                    font-size: 13px;
                }
                .details-table th {
                    background-color: #F0F9FF;
                    color: #0E1B2F;
                    font-weight: bold;
                    text-align: right;
                    padding: 10px;
                    border: 1px solid #cbd5e1;
                }
                .details-table td {
                    padding: 12px 10px;
                    border: 1px solid #cbd5e1;
                    color: #191C1E;
                }
                .financial-box {
                    width: 45%;
                    float: left;
                    border-collapse: collapse;
                    margin-top: 15px;
                }
                .financial-box td {
                    padding: 8px;
                    font-size: 14px;
                }
                .financial-box .label {
                    text-align: right;
                    color: #40484C;
                }
                .financial-box .value {
                    text-align: left;
                    font-weight: bold;
                }
                .financial-box .grand-total {
                    font-size: 16px;
                    color: #0E1B2F;
                    border-top: 2px solid #0284C7;
                    padding-top: 10px;
                }
                .clear {
                    clear: both;
                }
                .footer {
                    margin-top: 40px;
                    text-align: center;
                    font-size: 12px;
                    color: #555;
                    border-top: 1px dashed #cbd5e1;
                    padding-top: 15px;
                }
                @media print {
                    body {
                        padding: 0;
                        background: none;
                    }
                    .invoice-box {
                        border: none;
                        box-shadow: none;
                        padding: 0;
                    }
                }
            </style>
        </head>
        <body>
            <div class="invoice-box">
                <!-- الذهب الدافئ والأزرق الملكي - ترويسة معمل العلوي الرسمية -->
                <div style="display: flex; align-items: center; justify-content: space-between; width: 100%; border-bottom: 3px double #0E1B2F; padding-bottom: 12px; margin-bottom: 20px;">
                    <div style="text-align: right; width: 33%;">
                        <div style="font-size: 20px; font-weight: 800; color: #0E1B2F; margin: 0; font-family: 'Segoe UI', sans-serif;">معمل العلوي</div>
                        <div style="font-size: 14px; font-weight: bold; color: #0284C7; margin-top: 2px;">لتقنية الأسنان</div>
                        <div style="font-size: 11px; color: #EA580C; font-weight: bold; margin-top: 1px;">هاتف: 776020795</div>
                    </div>
                    <div style="text-align: center; width: 34%; display: flex; flex-direction: column; align-items: center; justify-content: center;">
                        <svg viewBox="0 0 160 100" width="90" height="60" style="display: block;">
                            <defs>
                                <linearGradient id="invWaveGrad" x1="0%" y1="0%" x2="100%" y2="0%">
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
                            <path d="M 18,30 C 25,65 38,72 55,50 C 70,35 110,65 145,45" fill="none" stroke="url(#invWaveGrad)" stroke-width="4" stroke-linecap="round" />
                        </svg>
                        <div style="font-size: 8px; font-weight: bold; color: #0E1B2F; letter-spacing: 0.5px; margin-top: 3px;">DENTAL LAB AL-ALAWI</div>
                    </div>
                    <div style="text-align: left; width: 33%;" dir="ltr">
                        <div style="font-size: 18px; font-weight: 800; color: #0E1B2F; margin: 0; font-family: 'Segoe UI', sans-serif;">Al-Alawi Dental</div>
                        <div style="font-size: 12px; font-weight: bold; color: #0284C7; margin-top: 2px;">Technology Lab</div>
                        <div style="font-size: 10px; color: #EA580C; font-weight: bold; margin-top: 1px;">YS-776020795</div>
                    </div>
                </div>

                <div style="text-align: center; margin: 20px 0;">
                    <span style="font-size: 18px; font-weight: 950; color: #0E1B2F; background-color: #F0F9FF; padding: 6px 30px; border-radius: 8px; border: 2px solid #0E1B2F;">فـاتـورة عـمـل وسـنـد قـبـض</span>
                </div>

                <table style="width: 100%; margin-bottom: 20px; border-collapse: collapse; font-size: 13px;">
                    <tr>
                        <td style="width: 50%; padding: 6px 0;">
                            <b>رقم الفاتورة:</b> <span style="font-weight: bold; color: #0E1B2F;">DL-${dentalCase.id}</span>
                        </td>
                        <td style="width: 50%; padding: 6px 0; text-align: left;">
                            <b>تاريخ القيد السريري:</b> ${dateFormatter.format(Date(dentalCase.createdAt))}
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 6px 0;">
                            <b>العميل (الطبيب المعالج):</b> <span style="font-weight: bold;">د. ${doctorName}</span>
                        </td>
                        <td style="padding: 6px 0; text-align: left;">
                            <b>تاريخ الاستحقاق والتسليم:</b> ${dateFormatter.format(Date(dentalCase.dueDate))}
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 6px 0;">
                            <b>المستشفى / العيادة:</b> <span>${doctorClinic}</span>
                        </td>
                        <td style="padding: 6px 0; text-align: left;">
                            <b>الحالة التشغيلية الحالية:</b> <span style="font-weight: bold; color: #0E1B2F;">${dentalCase.status}</span>
                        </td>
                    </tr>
                </table>

                <div class="section-title">بيانات ومواصفات التعويض السني</div>
                <table class="details-table">
                    <thead>
                        <tr>
                            <th>اسم المريض الكلي</th>
                            <th>نوع التركيبة والتعويض</th>
                            <th>اللون المطلوب (Shade)</th>
                            <th>عدد القطع/الأسنان</th>
                            <th>الفني المسؤل بالمعمل</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="font-weight: bold;">${dentalCase.patientName}</td>
                            <td>${dentalCase.workType} (${dentalCase.material})</td>
                            <td style="font-weight: bold; color: #EA580C;">${dentalCase.shade}</td>
                            <td style="font-weight: bold;">${dentalCase.teethNumbers} (${dentalCase.teethCount} وحدات)</td>
                            <td>$techName</td>
                        </tr>
                    </tbody>
                </table>

                ${if (dentalCase.notes.isNotEmpty()) {
                    """<div style="margin-top: 12px; padding: 10px; background: #f8fafc; border-right: 4px solid #0E1B2F; font-size: 13px; color: #40484C;">
                        <b>ملاحظات وتوجيهات الطبيب:</b> ${dentalCase.notes}
                    </div>"""
                } else ""}

                <div class="section-title">التسوية والمطالبة المالية</div>
                <div style="width: 100%;">
                    <table class="financial-box" style="margin-right: auto; margin-left: 0;">
                        <tr>
                            <td class="label">إجمالي الحساب للوحدات:</td>
                            <td class="value">${currencyFormatter.format(dentalCase.price)}</td>
                        </tr>
                        <tr>
                            <td class="label" style="color: #16a34a;">المبلغ المستلم (المسدّد):</td>
                            <td class="value" style="color: #16a34a;">${currencyFormatter.format(dentalCase.paidAmount)}</td>
                        </tr>
                        <tr class="grand-total">
                            <td class="label" style="font-weight: bold; color: ${if (dentalCase.remainingAmount > 0) "#dc2626" else "#0E1B2F"}">صافي المتبقي المطلوب:</td>
                            <td class="value" style="font-weight: bold; color: ${if (dentalCase.remainingAmount > 0) "#dc2626" else "#0E1B2F"}">${currencyFormatter.format(dentalCase.remainingAmount)}</td>
                        </tr>
                    </table>
                </div>
                <div class="clear"></div>

                <div class="footer">
                    معمل العلوي لتقنية الأسنان - جودة، دقة، وسرعة في تلبية متطلبات التصميم السني المتقدم.<br>
                    نشكركم جزيل الشكر لثقتكم الغالية وتعاملكم المالي والمهني معنا.
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "معمل_العلوي_فاتورة_${dentalCase.patientName}"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
}

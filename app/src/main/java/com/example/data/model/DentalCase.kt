package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dental_cases")
data class DentalCase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val doctorId: Long, // معرف الطبيب
    val technicianId: Long = -1, // معرف الفني (-1 يعني غير محدد)
    val patientName: String,
    val teethNumbers: String, // أرقام الأسنان (مثل: 12, 13, 14)
    val teethCount: Int = 1, // عدد الأسنان
    val workType: String, // نوع العمل (تاج، جسر، فينير، إلخ)
    val shade: String, // اللون (A1, A2, A3)
    val material: String, // المادة (زيركون، بورسلين، إيماكس)
    val status: String = "جديد", // جديد، قيد العمل، جاهز، تم التسليم، مرتجع
    val price: Double, // سعر القطعة أو السعر الإجمالي
    val paidAmount: Double = 0.0, // المبلغ المدفوع
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long, // تاريخ التسليم
    val notes: String = ""
) {
    val remainingAmount: Double
        get() = price - paidAmount
        
    val isPaidFully: Boolean
        get() = paidAmount >= price
}

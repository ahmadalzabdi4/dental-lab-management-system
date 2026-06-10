package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // تفاصيل المصروف
    val category: String, // الفئة (مثلاً: مواد المعمل، الإيجار والكهرباء، صيانة، أخرى)
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

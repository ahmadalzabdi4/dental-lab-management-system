package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val clinicName: String,
    val phone: String,
    val address: String,
    val discountPercent: Double = 0.0, // نسبة خصم خاصة إن وجدت
    val createdAt: Long = System.currentTimeMillis()
)

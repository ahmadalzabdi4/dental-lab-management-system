package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "technicians")
data class Technician(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String,
    val specialty: String, // التخصص (تركيبات ثابتة، متحركة، زيركون، إلخ)
    val salary: Double = 0.0,
    val isActive: Boolean = true,
    val joinDate: Long = System.currentTimeMillis()
)

package com.example.data.model

import androidx.room.Embedded

data class DentalCaseWithDetails(
    @Embedded val dentalCase: DentalCase,
    val doctorName: String?,
    val doctorClinic: String?,
    val technicianName: String?
)

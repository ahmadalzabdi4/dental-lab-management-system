package com.example.data.repository

import com.example.data.dao.DentalLabDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class DentalLabRepository(private val dao: DentalLabDao) {

    // --- DOCTORS ---
    val allDoctors: Flow<List<Doctor>> = dao.getAllDoctors()

    suspend fun getDoctorById(id: Long): Doctor? = dao.getDoctorById(id)

    suspend fun insertDoctor(doctor: Doctor): Long = dao.insertDoctor(doctor)

    suspend fun deleteDoctor(doctor: Doctor) = dao.deleteDoctor(doctor)


    // --- TECHNICIANS ---
    val allTechnicians: Flow<List<Technician>> = dao.getAllTechnicians()

    suspend fun getTechnicianById(id: Long): Technician? = dao.getTechnicianById(id)

    suspend fun insertTechnician(technician: Technician): Long = dao.insertTechnician(technician)

    suspend fun deleteTechnician(technician: Technician) = dao.deleteTechnician(technician)


    // --- DENTAL CASES ---
    val allCases: Flow<List<DentalCase>> = dao.getAllCases()
    val allCasesWithDetails: Flow<List<DentalCaseWithDetails>> = dao.getAllCasesWithDetails()

    suspend fun getCaseById(id: Long): DentalCase? = dao.getCaseById(id)

    suspend fun insertCase(dentalCase: DentalCase): Long = dao.insertCase(dentalCase)

    suspend fun deleteCase(dentalCase: DentalCase) = dao.deleteCase(dentalCase)


    // --- EXPENSES ---
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()

    suspend fun insertExpense(expense: Expense): Long = dao.insertExpense(expense)

    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)

    // --- RESTORE BACKUP ---
    suspend fun restoreBackup(
        doctors: List<Doctor>,
        technicians: List<Technician>,
        cases: List<DentalCase>,
        expenses: List<Expense>
    ) = dao.restoreBackup(doctors, technicians, cases, expenses)
}

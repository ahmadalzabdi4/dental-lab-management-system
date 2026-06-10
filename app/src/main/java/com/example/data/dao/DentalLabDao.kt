package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DentalLabDao {

    // --- DOCTORS ---
    @Query("SELECT * FROM doctors ORDER BY fullName ASC")
    fun getAllDoctors(): Flow<List<Doctor>>

    @Query("SELECT * FROM doctors WHERE id = :id")
    suspend fun getDoctorById(id: Long): Doctor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: Doctor): Long

    @Delete
    suspend fun deleteDoctor(doctor: Doctor)


    // --- TECHNICIANS ---
    @Query("SELECT * FROM technicians ORDER BY fullName ASC")
    fun getAllTechnicians(): Flow<List<Technician>>

    @Query("SELECT * FROM technicians WHERE id = :id")
    suspend fun getTechnicianById(id: Long): Technician?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnician(technician: Technician): Long

    @Delete
    suspend fun deleteTechnician(technician: Technician)


    // --- DENTAL CASES ---
    @Query("SELECT * FROM dental_cases ORDER BY createdAt DESC")
    fun getAllCases(): Flow<List<DentalCase>>

    @Query("""
        SELECT 
            c.*, 
            d.fullName as doctorName, 
            d.clinicName as doctorClinic, 
            t.fullName as technicianName 
        FROM dental_cases c
        LEFT JOIN doctors d ON c.doctorId = d.id
        LEFT JOIN technicians t ON c.technicianId = t.id
        ORDER BY c.createdAt DESC
    """)
    fun getAllCasesWithDetails(): Flow<List<DentalCaseWithDetails>>

    @Query("SELECT * FROM dental_cases WHERE id = :id")
    suspend fun getCaseById(id: Long): DentalCase?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(dentalCase: DentalCase): Long

    @Delete
    suspend fun deleteCase(dentalCase: DentalCase)


    // --- EXPENSES ---
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)


    // --- BACKUP & RESTORE METHODS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<Doctor>)

    @Query("DELETE FROM doctors")
    suspend fun deleteAllDoctors()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnicians(technicians: List<Technician>)

    @Query("DELETE FROM technicians")
    suspend fun deleteAllTechnicians()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCases(cases: List<DentalCase>)

    @Query("DELETE FROM dental_cases")
    suspend fun deleteAllCases()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Transaction
    suspend fun restoreBackup(
        doctors: List<Doctor>,
        technicians: List<Technician>,
        cases: List<DentalCase>,
        expenses: List<Expense>
    ) {
        deleteAllDoctors()
        deleteAllTechnicians()
        deleteAllCases()
        deleteAllExpenses()

        insertDoctors(doctors)
        insertTechnicians(technicians)
        insertCases(cases)
        insertExpenses(expenses)
    }
}

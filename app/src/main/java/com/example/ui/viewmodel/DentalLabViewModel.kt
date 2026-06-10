package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DentalLabDatabase
import com.example.data.model.*
import com.example.data.repository.DentalLabRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DentalLabViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DentalLabRepository

    // تدفق البيانات الكلية من قاعدة البيانات
    val doctors: StateFlow<List<Doctor>>
    val technicians: StateFlow<List<Technician>>
    val cases: StateFlow<List<DentalCaseWithDetails>>
    val expenses: StateFlow<List<Expense>>

    init {
        val dao = DentalLabDatabase.getDatabase(application).dentalLabDao()
        repository = DentalLabRepository(dao)

        doctors = repository.allDoctors.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        technicians = repository.allTechnicians.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        cases = repository.allCasesWithDetails.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        expenses = repository.allExpenses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // التحقق من أن قاعدة البيانات تحتوي على بيانات أولية، وإذا كانت فارغة نقوم بتغذيتها بطريقة ذكية
        viewModelScope.launch {
            val allDocs = repository.allDoctors.first()
            if (allDocs.isEmpty()) {
                prepopulateDatabase()
            } else {
                // إزالة التكرار ودمج الأطباء ذوي الأسماء المتطابقة الذين تكرروا بسبب خلل التهيئة السابق
                val groupedByName = allDocs.groupBy { it.fullName.trim() }
                groupedByName.forEach { (_, docsWithSameName) ->
                    if (docsWithSameName.size > 1) {
                        val primaryDoc = docsWithSameName.minByOrNull { it.id } ?: return@forEach
                        val primaryId = primaryDoc.id
                        val duplicates = docsWithSameName.filter { it.id != primaryId }
                        val duplicateIds = duplicates.map { it.id }

                        // 1. إعادة ربط كافة الحالات التابعة للأطباء المكررين بالطبيب الأساسي الموحد لضمان عدم فقدان أي مريض
                        val allCasesSnapshot = repository.allCases.first()
                        allCasesSnapshot.forEach { caseObj ->
                            if (caseObj.doctorId in duplicateIds) {
                                val updatedCase = caseObj.copy(doctorId = primaryId)
                                repository.insertCase(updatedCase)
                            }
                        }

                        // 2. بعد إعادة توجيه الحالات، نقوم بحذف السجلات المكررة بشكل آمن
                        duplicates.forEach { dupDoc ->
                            repository.deleteDoctor(dupDoc)
                        }
                    }
                }

                // دمج الفنيين المكررين المتبقيين إن وجدوا
                val allTechs = repository.allTechnicians.first()
                val groupedTechs = allTechs.groupBy { it.fullName.trim() }
                groupedTechs.forEach { (_, techsWithSameName) ->
                    if (techsWithSameName.size > 1) {
                        val primaryTech = techsWithSameName.minByOrNull { it.id } ?: return@forEach
                        val primaryId = primaryTech.id
                        val duplicates = techsWithSameName.filter { it.id != primaryId }
                        val duplicateIds = duplicates.map { it.id }

                        val allCasesSnapshot = repository.allCases.first()
                        allCasesSnapshot.forEach { caseObj ->
                            if (caseObj.technicianId in duplicateIds) {
                                val updatedCase = caseObj.copy(technicianId = primaryId)
                                repository.insertCase(updatedCase)
                            }
                        }

                        duplicates.forEach { dupTech ->
                            repository.deleteTechnician(dupTech)
                        }
                    }
                }

                // دمج وإزالة الحالات المكررة بدقة متناهية لنفس الطبيب والمريض لمنع تراكم التكرار
                val finalCasesList = repository.allCases.first()
                val groupedCases = finalCasesList.groupBy {
                    "${it.doctorId}_${it.patientName.trim().lowercase()}_${it.workType.trim().lowercase()}_${it.teethNumbers.trim()}"
                }
                groupedCases.forEach { (_, casesList) ->
                    if (casesList.size > 1) {
                        val primaryCase = casesList.minByOrNull { it.id } ?: return@forEach
                        val duplicates = casesList.filter { it.id != primaryCase.id }
                        
                        // جمع وحفظ المبالغ المدفوعة السابقة لضمان عدم ضياع أي سنت حقيقي
                        val totalPaid = casesList.sumOf { it.paidAmount }
                        val mergedPaidAmount = totalPaid.coerceAtMost(primaryCase.price)
                        
                        if (primaryCase.paidAmount != mergedPaidAmount) {
                            repository.insertCase(primaryCase.copy(paidAmount = mergedPaidAmount))
                        }
                        
                        // حذف الحالات المكررة والاحتفاظ بالحالة الأساسية فقط
                        duplicates.forEach { dupCase ->
                            repository.deleteCase(dupCase)
                        }
                    }
                }
            }
        }
    }

    private suspend fun prepopulateDatabase() {
        // 1. إضافة الأطباء
        val d1 = Doctor(fullName = "د. عبد الجليل", clinicName = "عيادة الحكمة للأسنان", phone = "771234567", address = "صنعاء - شارع حده")
        val d2 = Doctor(fullName = "د. محمد عباد", clinicName = "عيادة صناع الابتسامة", phone = "773224455", address = "صنعاء - شارع الدائري")
        val d3 = Doctor(fullName = "د. سارة الصعفاني", clinicName = "عيادة ابتسامتي", phone = "775661122", address = "صنعاء - شارع الجزائر")
        val d4 = Doctor(fullName = "د. فاطمة المهدي", clinicName = "مستشفى طيبة الاستشاري", phone = "777889900", address = "صنعاء - الستين")

        val doc1Id = repository.insertDoctor(d1)
        val doc2Id = repository.insertDoctor(d2)
        val doc3Id = repository.insertDoctor(d3)
        val doc4Id = repository.insertDoctor(d4)

        // 2. إضافة الفنيين
        val t1 = Technician(fullName = "محمد الزبيدي", phone = "770112233", specialty = "تركيبات ثابتة - CAD/CAM", salary = 1200.0)
        val t2 = Technician(fullName = "عيسى الصبري", phone = "772334455", specialty = "تركيبات متحركة وأكريل", salary = 900.0)
        val t3 = Technician(fullName = "ذي يزن الأصبحي", phone = "774556677", specialty = "تقويم الأسنان والأجهزة", salary = 1000.0)
        val t4 = Technician(fullName = "علي يحيى الغرباني", phone = "776778899", specialty = "تلوين زيركون وخزف", salary = 1100.0)

        val tech1Id = repository.insertTechnician(t1)
        val tech2Id = repository.insertTechnician(t2)
        val tech3Id = repository.insertTechnician(t3)
        val tech4Id = repository.insertTechnician(t4)

        // 3. إضافة الحالات
        val oneDayMs = 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()

        repository.insertCase(
            DentalCase(
                doctorId = doc1Id,
                technicianId = tech1Id,
                patientName = "أحمد السعيدي",
                teethNumbers = "11, 21",
                teethCount = 2,
                workType = "فينير (Veneer)",
                shade = "BL2",
                material = "إيماكس (E-Max)",
                status = "تم التسليم",
                price = 1600.0,
                paidAmount = 1600.0,
                createdAt = now - (5 * oneDayMs),
                dueDate = now - (2 * oneDayMs),
                notes = "يرجى تعديل خط الابتسامة ليطابق الطبيعي"
            )
        )

        repository.insertCase(
            DentalCase(
                doctorId = doc2Id,
                technicianId = tech4Id,
                patientName = "أروى أحمد الكباسي",
                teethNumbers = "36, 37",
                teethCount = 2,
                workType = "جسور تجميلية (Crown/Bridge)",
                shade = "A2",
                material = "زيركون (Zirconia)",
                status = "جاهز للتسليم",
                price = 1200.0,
                paidAmount = 1000.0,
                createdAt = now - (3 * oneDayMs),
                dueDate = now,
                notes = "التحام عنقي ممتاز"
            )
        )

        repository.insertCase(
            DentalCase(
                doctorId = doc3Id,
                technicianId = tech2Id,
                patientName = "عمر خالد القدسي",
                teethNumbers = "46",
                teethCount = 1,
                workType = "حشوة مصبوبة (Inlay)",
                shade = "A3",
                material = "إيماكس (E-Max)",
                status = "قيد العمل",
                price = 500.0,
                paidAmount = 0.0,
                createdAt = now - (1 * oneDayMs),
                dueDate = now + (2 * oneDayMs),
                notes = "تحضير بسيط"
            )
        )

        repository.insertCase(
            DentalCase(
                doctorId = doc4Id,
                technicianId = tech2Id,
                patientName = "منى يوسف الهمداني",
                teethNumbers = "الفك العلوي بالكامل",
                teethCount = 14,
                workType = "طقم كامل (Full Denture)",
                shade = "A1",
                material = "أكريل (Acrylic)",
                status = "جديد",
                price = 2800.0,
                paidAmount = 800.0,
                createdAt = now,
                dueDate = now + (5 * oneDayMs),
                notes = "التركيز على ثبات الطقم من الخلف"
            )
        )

        // 4. إضافة المصروفات
        repository.insertExpense(Expense(title = "شراء ديسكات زيركون", category = "مواد معملية", amount = 450.0, date = now - (4 * oneDayMs), notes = "إجمالي عدد 3 ديسكات"))
        repository.insertExpense(Expense(title = "صيانة جهاز الميكرو موتور", category = "صيانة وإصلاح", amount = 120.0, date = now - (2 * oneDayMs), notes = "تغيير رصيف الدوران"))
        repository.insertExpense(Expense(title = "فاتورة كهرباء المعمل", category = "إيجارات ومرافق", amount = 180.0, date = now - oneDayMs, notes = "شهر مايو"))
        repository.insertExpense(Expense(title = "شراء جبس معملي فائق الصلابة", category = "مواد معملية", amount = 220.0, date = now, notes = "كرتونين من الصنف الألماني"))
    }

    // --- العمليات على الأطباء ---
    fun addDoctor(doctor: Doctor) {
        viewModelScope.launch { repository.insertDoctor(doctor) }
    }

    fun updateDoctor(doctor: Doctor) {
        viewModelScope.launch { repository.insertDoctor(doctor) }
    }

    fun deleteDoctor(doctor: Doctor) {
        viewModelScope.launch { repository.deleteDoctor(doctor) }
    }

    // --- العمليات على الفنيين ---
    fun addTechnician(technician: Technician) {
        viewModelScope.launch { repository.insertTechnician(technician) }
    }

    fun updateTechnician(technician: Technician) {
        viewModelScope.launch { repository.insertTechnician(technician) }
    }

    fun deleteTechnician(technician: Technician) {
        viewModelScope.launch { repository.deleteTechnician(technician) }
    }

    // --- العمليات على الحالات ---
    fun addCase(dentalCase: DentalCase) {
        viewModelScope.launch { repository.insertCase(dentalCase) }
    }

    fun updateCase(dentalCase: DentalCase) {
        viewModelScope.launch { repository.insertCase(dentalCase) }
    }

    fun updateCaseStatus(caseId: Long, newStatus: String) {
        viewModelScope.launch {
            val case = repository.getCaseById(caseId)
            if (case != null) {
                repository.insertCase(case.copy(status = newStatus))
            }
        }
    }

    fun recordCasePayment(caseId: Long, paymentAmount: Double) {
        viewModelScope.launch {
            val case = repository.getCaseById(caseId)
            if (case != null) {
                val newPaid = (case.paidAmount + paymentAmount).coerceAtMost(case.price)
                repository.insertCase(case.copy(paidAmount = newPaid))
            }
        }
    }

    fun deleteCase(dentalCase: DentalCase) {
        viewModelScope.launch { repository.deleteCase(dentalCase) }
    }

    // --- العمليات على المصروفات ---
    fun addExpense(expense: Expense) {
        viewModelScope.launch { repository.insertExpense(expense) }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    // --- العمليات على قاعدة البيانات والنسخ الاحتياطي ---
    fun restoreBackupData(
        backupData: com.example.data.BackupData,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.restoreBackup(
                    doctors = backupData.doctors,
                    technicians = backupData.technicians,
                    cases = backupData.cases,
                    expenses = backupData.expenses
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "حدث خطأ غير متوقع أثناء استعادة البيانات")
            }
        }
    }

    fun clearAllData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.restoreBackup(emptyList(), emptyList(), emptyList(), emptyList())
                onSuccess()
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    fun resetAndPrepopulate(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.restoreBackup(emptyList(), emptyList(), emptyList(), emptyList())
                prepopulateDatabase()
                onSuccess()
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }
}

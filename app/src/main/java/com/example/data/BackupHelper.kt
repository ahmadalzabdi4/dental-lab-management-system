package com.example.data

import com.example.data.model.DentalCase
import com.example.data.model.Doctor
import com.example.data.model.Expense
import com.example.data.model.Technician
import org.json.JSONArray
import org.json.JSONObject

data class BackupData(
    val doctors: List<Doctor>,
    val technicians: List<Technician>,
    val cases: List<DentalCase>,
    val expenses: List<Expense>
)

object BackupHelper {

    fun exportToJsonString(backupData: BackupData): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "DentalLabManager")
        root.put("exportedAt", System.currentTimeMillis())

        // 1. Doctors
        val doctorsArr = JSONArray()
        backupData.doctors.forEach { doc ->
            val docObj = JSONObject().apply {
                put("id", doc.id)
                put("fullName", doc.fullName)
                put("clinicName", doc.clinicName)
                put("phone", doc.phone)
                put("address", doc.address)
                put("discountPercent", doc.discountPercent)
                put("createdAt", doc.createdAt)
            }
            doctorsArr.put(docObj)
        }
        root.put("doctors", doctorsArr)

        // 2. Technicians
        val techsArr = JSONArray()
        backupData.technicians.forEach { tech ->
            val techObj = JSONObject().apply {
                put("id", tech.id)
                put("fullName", tech.fullName)
                put("phone", tech.phone)
                put("specialty", tech.specialty)
                put("salary", tech.salary)
                put("isActive", tech.isActive)
                put("joinDate", tech.joinDate)
            }
            techsArr.put(techObj)
        }
        root.put("technicians", techsArr)

        // 3. Cases
        val casesArr = JSONArray()
        backupData.cases.forEach { item ->
            val caseObj = JSONObject().apply {
                put("id", item.id)
                put("doctorId", item.doctorId)
                put("technicianId", item.technicianId)
                put("patientName", item.patientName)
                put("teethNumbers", item.teethNumbers)
                put("teethCount", item.teethCount)
                put("workType", item.workType)
                put("shade", item.shade)
                put("material", item.material)
                put("status", item.status)
                put("price", item.price)
                put("paidAmount", item.paidAmount)
                put("createdAt", item.createdAt)
                put("dueDate", item.dueDate)
                put("notes", item.notes)
            }
            casesArr.put(caseObj)
        }
        root.put("cases", casesArr)

        // 4. Expenses
        val expensesArr = JSONArray()
        backupData.expenses.forEach { exp ->
            val expObj = JSONObject().apply {
                put("id", exp.id)
                put("title", exp.title)
                put("category", exp.category)
                put("amount", exp.amount)
                put("date", exp.date)
                put("notes", exp.notes)
            }
            expensesArr.put(expObj)
        }
        root.put("expenses", expensesArr)

        return root.toString(4) // 4 spaces indentation for readability
    }

    fun importFromJsonString(jsonString: String): BackupData {
        val root = JSONObject(jsonString)

        // Read doctors
        val doctors = mutableListOf<Doctor>()
        if (root.has("doctors")) {
            val arr = root.getJSONArray("doctors")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                doctors.add(
                    Doctor(
                        id = obj.optLong("id", 0L),
                        fullName = obj.getString("fullName"),
                        clinicName = obj.optString("clinicName", ""),
                        phone = obj.optString("phone", "غير مسجل"),
                        address = obj.optString("address", ""),
                        discountPercent = obj.optDouble("discountPercent", 0.0),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        // Read technicians
        val technicians = mutableListOf<Technician>()
        if (root.has("technicians")) {
            val arr = root.getJSONArray("technicians")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                technicians.add(
                    Technician(
                        id = obj.optLong("id", 0L),
                        fullName = obj.getString("fullName"),
                        phone = obj.optString("phone", "غير مسجل"),
                        specialty = obj.optString("specialty", ""),
                        salary = obj.optDouble("salary", 0.0),
                        isActive = obj.optBoolean("isActive", true),
                        joinDate = obj.optLong("joinDate", System.currentTimeMillis())
                    )
                )
            }
        }

        // Read cases
        val cases = mutableListOf<DentalCase>()
        if (root.has("cases")) {
            val arr = root.getJSONArray("cases")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                cases.add(
                    DentalCase(
                        id = obj.optLong("id", 0L),
                        doctorId = obj.getLong("doctorId"),
                        technicianId = obj.optLong("technicianId", -1L),
                        patientName = obj.getString("patientName"),
                        teethNumbers = obj.optString("teethNumbers", "غير محدد"),
                        teethCount = obj.optInt("teethCount", 1),
                        workType = obj.getString("workType"),
                        shade = obj.optString("shade", "A2"),
                        material = obj.optString("material", ""),
                        status = obj.optString("status", "جديد"),
                        price = obj.getDouble("price"),
                        paidAmount = obj.optDouble("paidAmount", 0.0),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        dueDate = obj.getLong("dueDate"),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        }

        // Read expenses
        val expenses = mutableListOf<Expense>()
        if (root.has("expenses")) {
            val arr = root.getJSONArray("expenses")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                expenses.add(
                    Expense(
                        id = obj.optLong("id", 0L),
                        title = obj.getString("title"),
                        category = obj.optString("category", ""),
                        amount = obj.getDouble("amount"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        }

        return BackupData(doctors, technicians, cases, expenses)
    }
}

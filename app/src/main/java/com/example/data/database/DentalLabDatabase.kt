package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.DentalLabDao
import com.example.data.model.DentalCase
import com.example.data.model.Doctor
import com.example.data.model.Expense
import com.example.data.model.Technician

@Database(
    entities = [Doctor::class, Technician::class, DentalCase::class, Expense::class],
    version = 1,
    exportSchema = false
)
abstract class DentalLabDatabase : RoomDatabase() {

    abstract fun dentalLabDao(): DentalLabDao

    companion object {
        @Volatile
        private var INSTANCE: DentalLabDatabase? = null

        fun getDatabase(context: Context): DentalLabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DentalLabDatabase::class.java,
                    "al_olawi_dental_lab_db"
                )
                // في معامل الأسنان الفئات والمدخلات حساسة للانهيار، نستخدم fallbackToDestructiveMigration
                // لتبسيط التعديلات خلال مرحلة التطوير وتطوير التطبيق للهاتف
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

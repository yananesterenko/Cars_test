package com.example.cars_test.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CarEntity::class,
        RentalEntity::class,
        CustomerEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carDao(): CarDao
    abstract fun rentalDao(): RentalDao

    abstract fun customerDao(): CustomerDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cars_rental_database"
                ).fallbackToDestructiveMigration() // TODO удалить строку при продакшене
                    .build()


                INSTANCE = instance
                instance
            }
        }
    }
}
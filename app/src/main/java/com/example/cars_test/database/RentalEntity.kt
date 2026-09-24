package com.example.cars_test.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rentals")
data class RentalEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val carId: Long,

    val customerId: Long,

    val startDate: Long,

    val endDate: Long,

    val pricePerDay: Double,

    val totalPrice: Double,

    val status: String,

    val notes: String,

    val completedAt: Long? = null
)
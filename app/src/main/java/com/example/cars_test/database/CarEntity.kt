package com.example.cars_test.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class CarEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val brand: String,

    val model: String,

    val year: Int,

    val licensePlate: String,

    val vin: String,

    val mileage: Int,

    val pricePerDay: Double,

    val status: String,

    val notes: String
)
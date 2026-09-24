package com.example.cars_test.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val firstName: String,
    val lastName: String,

    val phone: String,
    val email: String,

    val driverLicense: String,

    val address: String,

    val notes: String
)
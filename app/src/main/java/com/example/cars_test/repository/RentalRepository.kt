package com.example.cars_test.repository

import com.example.cars_test.database.RentalDao
import com.example.cars_test.database.RentalEntity

class RentalRepository(
    private val rentalDao: RentalDao
) {

    suspend fun getAllRentals(): List<RentalEntity> {
        return rentalDao.getAllRentals()
    }

    suspend fun getRentalById(id: Long): RentalEntity? {
        return rentalDao.getRentalById(id)
    }

    suspend fun insertRental(rental: RentalEntity) {
        rentalDao.insertRental(rental)
    }

    suspend fun updateRental(rental: RentalEntity) {
        rentalDao.updateRental(rental)
    }

    suspend fun deleteRental(rental: RentalEntity) {
        rentalDao.deleteRental(rental)
    }
}
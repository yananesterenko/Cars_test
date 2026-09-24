package com.example.cars_test.repository

import com.example.cars_test.database.CarDao
import com.example.cars_test.database.CarEntity

class CarRepository(
    private val carDao: CarDao
) {

    suspend fun getAllCars(): List<CarEntity> {
        return carDao.getAllCars()
    }

    suspend fun getCarById(id: Long): CarEntity? {
        return carDao.getCarById(id)
    }

    suspend fun insertCar(car: CarEntity) {
        carDao.insertCar(car)
    }

    suspend fun updateCar(car: CarEntity) {
        carDao.updateCar(car)
    }

    suspend fun deleteCar(car: CarEntity) {
        carDao.deleteCar(car)
    }
}
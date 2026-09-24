package com.example.cars_test.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CarDao {

    @Query("SELECT * FROM cars ORDER BY brand, model")
    suspend fun getAllCars(): List<CarEntity>

    @Query("SELECT * FROM cars WHERE id = :id")
    suspend fun getCarById(id: Long): CarEntity?

    @Query("SELECT * FROM cars WHERE status = 'Available' ORDER BY brand, model")
    suspend fun getAvailableCars(): List<CarEntity>

    @Query("SELECT COUNT(*) FROM cars")
    suspend fun getCarsCount(): Int

    @Query("SELECT COUNT(*) FROM cars WHERE status = 'Available'")
    suspend fun getAvailableCarsCount(): Int

    @Query("SELECT COUNT(*) FROM cars WHERE status = 'Rented'")
    suspend fun getRentedCarsCount(): Int

    @Query("SELECT COUNT(*) FROM cars WHERE status = 'Maintenance'")
    suspend fun getMaintenanceCarsCount(): Int

    @Insert
    suspend fun insertCar(car: CarEntity)

    @Update
    suspend fun updateCar(car: CarEntity)

    @Delete
    suspend fun deleteCar(car: CarEntity)

}
package com.example.cars_test.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RentalDao {

    @Query("SELECT * FROM rentals ORDER BY startDate DESC")
    suspend fun getAllRentals(): List<RentalEntity>

    @Query("SELECT * FROM rentals WHERE id = :id")
    suspend fun getRentalById(id: Long): RentalEntity?

    @Query("SELECT * FROM rentals WHERE carId = :carId")
    suspend fun getRentalsForCar(carId: Long): List<RentalEntity>

    @Query("SELECT * FROM rentals WHERE customerId = :customerId ORDER BY startDate DESC")
    suspend fun getRentalsForCustomer(customerId: Long): List<RentalEntity>

    @Query("SELECT COUNT(*) FROM rentals WHERE status = 'Active'")
    suspend fun getActiveRentalsCount(): Int

    @Query("SELECT COUNT(*) FROM rentals WHERE status = 'Completed'")
    suspend fun getCompletedRentalsCount(): Int

    @Query("""
    SELECT COALESCE(SUM(totalPrice), 0)
    FROM rentals
    WHERE status = 'Completed'
    """)
    suspend fun getCompletedRevenue(): Double

    @Query("""
    SELECT COALESCE(SUM(totalPrice), 0)
    FROM rentals
    WHERE status = 'Completed'
    AND completedAt >= :startOfDay
    AND completedAt < :startOfNextDay
""")
    suspend fun getRevenueForDay(
        startOfDay: Long,
        startOfNextDay: Long
    ): Double


    @Query("""
    SELECT COALESCE(SUM(totalPrice), 0)
    FROM rentals
    WHERE status = 'Completed'
    AND completedAt >= :startOfMonth
    AND completedAt < :startOfNextMonth
""")
    suspend fun getRevenueForMonth(
        startOfMonth: Long,
        startOfNextMonth: Long
    ): Double

    @Insert
    suspend fun insertRental(rental: RentalEntity)

    @Update
    suspend fun updateRental(rental: RentalEntity)

    @Delete
    suspend fun deleteRental(rental: RentalEntity)

}
package com.example.cars_test.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cars_test.database.RentalEntity
import com.example.cars_test.repository.RentalRepository
import kotlinx.coroutines.launch

class RentalViewModel(
    private val repository: RentalRepository
) : ViewModel() {

    suspend fun getAllRentals(): List<RentalEntity> {
        return repository.getAllRentals()
    }

    suspend fun getRentalById(id: Long): RentalEntity? {
        return repository.getRentalById(id)
    }

    fun addRental(rental: RentalEntity) {
        viewModelScope.launch {
            repository.insertRental(rental)
        }
    }

    fun updateRental(rental: RentalEntity) {
        viewModelScope.launch {
            repository.updateRental(rental)
        }
    }

    fun deleteRental(rental: RentalEntity) {
        viewModelScope.launch {
            repository.deleteRental(rental)
        }
    }
}
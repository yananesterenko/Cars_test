package com.example.cars_test.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cars_test.database.CarEntity
import com.example.cars_test.repository.CarRepository
import kotlinx.coroutines.launch

class CarViewModel(
    private val repository: CarRepository
) : ViewModel() {

    fun addCar(car: CarEntity) {
        viewModelScope.launch {
            repository.insertCar(car)
        }
    }

    fun deleteCar(car: CarEntity) {
        viewModelScope.launch {
            repository.deleteCar(car)
        }
    }

    fun updateCar(car: CarEntity) {
        viewModelScope.launch {
            repository.updateCar(car)
        }
    }

    suspend fun getAllCars(): List<CarEntity> {
        return repository.getAllCars()
    }

    suspend fun getCarById(id: Long): CarEntity? {
        return repository.getCarById(id)
    }
}
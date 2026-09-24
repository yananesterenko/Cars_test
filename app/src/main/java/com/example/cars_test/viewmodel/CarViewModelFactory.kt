package com.example.cars_test.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cars_test.repository.CarRepository

class CarViewModelFactory(
    private val repository: CarRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(CarViewModel::class.java)) {
            return CarViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
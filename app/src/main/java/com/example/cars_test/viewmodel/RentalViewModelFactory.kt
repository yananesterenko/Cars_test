package com.example.cars_test.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cars_test.repository.RentalRepository

class RentalViewModelFactory(
    private val repository: RentalRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(RentalViewModel::class.java)) {
            return RentalViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
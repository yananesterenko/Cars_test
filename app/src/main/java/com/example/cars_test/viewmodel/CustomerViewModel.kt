package com.example.cars_test.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cars_test.database.CustomerEntity
import com.example.cars_test.repository.CustomerRepository
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    suspend fun getAllCustomers(): List<CustomerEntity> {
        return repository.getAllCustomers()
    }

    suspend fun getCustomerById(id: Long): CustomerEntity? {
        return repository.getCustomerById(id)
    }

    fun addCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.insertCustomer(customer)
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }
}
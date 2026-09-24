package com.example.cars_test.repository

import com.example.cars_test.database.CustomerDao
import com.example.cars_test.database.CustomerEntity

class CustomerRepository(
    private val customerDao: CustomerDao
) {

    suspend fun getAllCustomers(): List<CustomerEntity> {
        return customerDao.getAllCustomers()
    }

    suspend fun getCustomerById(id: Long): CustomerEntity? {
        return customerDao.getCustomerById(id)
    }

    suspend fun insertCustomer(customer: CustomerEntity) {
        customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.deleteCustomer(customer)
    }
}
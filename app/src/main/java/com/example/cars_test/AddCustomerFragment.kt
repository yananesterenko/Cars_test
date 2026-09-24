package com.example.cars_test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cars_test.database.AppDatabase
import com.example.cars_test.database.CustomerEntity
import com.example.cars_test.databinding.FragmentAddCustomerBinding
import com.example.cars_test.repository.CustomerRepository
import com.example.cars_test.viewmodel.CustomerViewModel
import com.example.cars_test.viewmodel.CustomerViewModelFactory
import kotlinx.coroutines.launch

class AddCustomerFragment : Fragment() {

    private var _binding: FragmentAddCustomerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CustomerViewModel

    private var editingCustomer: CustomerEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddCustomerBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val database =
            AppDatabase.getDatabase(requireContext())

        val repository =
            CustomerRepository(database.customerDao())

        val factory =
            CustomerViewModelFactory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[CustomerViewModel::class.java]

        val customerId =
            arguments?.getLong("customerId", 0L) ?: 0L

        if (customerId != 0L) {
            loadCustomer(customerId)
        }

        binding.buttonSaveCustomer.setOnClickListener {
            saveCustomer()
        }
    }

    private fun loadCustomer(customerId: Long) {

        viewLifecycleOwner.lifecycleScope.launch {

            val customer =
                viewModel.getCustomerById(customerId)

            if (customer == null) {
                Toast.makeText(
                    requireContext(),
                    "Customer not found",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigateUp()
                return@launch
            }

            editingCustomer = customer

            binding.editFirstName.setText(
                customer.firstName
            )

            binding.editLastName.setText(
                customer.lastName
            )

            binding.editPhone.setText(
                customer.phone
            )

            binding.editEmail.setText(
                customer.email
            )

            binding.editDriverLicense.setText(
                customer.driverLicense
            )

            binding.editAddress.setText(
                customer.address
            )

            binding.editNotes.setText(
                customer.notes
            )

            binding.buttonSaveCustomer.text =
                "Save Changes"
        }
    }

    private fun saveCustomer() {

        val firstName =
            binding.editFirstName.text.toString().trim()

        val lastName =
            binding.editLastName.text.toString().trim()

        if (firstName.isEmpty()) {
            binding.editFirstName.error =
                "Enter first name"
            return
        }

        if (lastName.isEmpty()) {
            binding.editLastName.error =
                "Enter last name"
            return
        }

        val oldCustomer = editingCustomer

        if (oldCustomer == null) {

            // ADD NEW CUSTOMER

            val newCustomer =
                CustomerEntity(
                    firstName = firstName,
                    lastName = lastName,
                    phone = binding.editPhone.text.toString().trim(),
                    email = binding.editEmail.text.toString().trim(),
                    driverLicense = binding.editDriverLicense.text.toString().trim(),
                    address = binding.editAddress.text.toString().trim(),
                    notes = binding.editNotes.text.toString().trim()
                )

            viewModel.addCustomer(
                newCustomer
            )

            Toast.makeText(
                requireContext(),
                "Customer saved",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            // EDIT EXISTING CUSTOMER

            val updatedCustomer =
                oldCustomer.copy(
                    firstName = firstName,
                    lastName = lastName,
                    phone = binding.editPhone.text.toString().trim(),
                    email = binding.editEmail.text.toString().trim(),
                    driverLicense = binding.editDriverLicense.text.toString().trim(),
                    address = binding.editAddress.text.toString().trim(),
                    notes = binding.editNotes.text.toString().trim()
                )

            viewModel.updateCustomer(
                updatedCustomer
            )

            Toast.makeText(
                requireContext(),
                "Customer updated",
                Toast.LENGTH_SHORT
            ).show()
        }

        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
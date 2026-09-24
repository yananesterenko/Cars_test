package com.example.cars_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cars_test.R
import com.example.cars_test.database.AppDatabase
import com.example.cars_test.database.CustomerEntity
import com.example.cars_test.databinding.FragmentCustomerDetailsBinding
import com.example.cars_test.repository.CustomerRepository
import com.example.cars_test.viewmodel.CustomerViewModel
import com.example.cars_test.viewmodel.CustomerViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomerDetailsFragment : Fragment() {

    private lateinit var database: AppDatabase

    private var _binding: FragmentCustomerDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CustomerViewModel

    private var currentCustomer: CustomerEntity? = null

    private val dateFormat =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCustomerDetailsBinding.inflate(
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

        database =
            AppDatabase.getDatabase(requireContext())

        val repository =
            CustomerRepository(database.customerDao())

        val factory =
            CustomerViewModelFactory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[CustomerViewModel::class.java]



        binding.buttonEditCustomer.setOnClickListener {
            editCustomer()
        }

        binding.buttonDeleteCustomer.setOnClickListener {
            confirmDeleteCustomer()
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

            currentCustomer = customer

            loadRentalHistory(customer.id)

            binding.textCustomerName.text =
                "${customer.firstName} ${customer.lastName}"

            binding.textPhone.text =
                "Phone: ${customer.phone}"

            binding.textEmail.text =
                "Email: ${customer.email}"

            binding.textDriverLicense.text =
                "Driver license: ${customer.driverLicense}"

            binding.textAddress.text =
                "Address: ${customer.address}"

            binding.textNotes.text =
                "Notes: ${customer.notes}"
        }
    }

    private fun editCustomer() {

        val customer =
            currentCustomer ?: return

        val bundle = Bundle().apply {
            putLong(
                "customerId",
                customer.id
            )
        }

        findNavController().navigate(
            R.id.action_CustomerDetailsFragment_to_AddCustomerFragment,
            bundle
        )
    }

    private fun confirmDeleteCustomer() {

        val customer = currentCustomer ?: return

        viewLifecycleOwner.lifecycleScope.launch {

            val rentals = database.rentalDao()
                .getRentalsForCustomer(customer.id)

            if (rentals.isNotEmpty()) {

                AlertDialog.Builder(requireContext())
                    .setTitle("Cannot delete customer")
                    .setMessage(
                        "This customer has rental history and cannot be deleted."
                    )
                    .setPositiveButton("OK", null)
                    .show()

                return@launch
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Delete customer")
                .setMessage(
                    "Delete ${customer.firstName} ${customer.lastName}?"
                )
                .setPositiveButton("Delete") { _, _ ->

                    viewModel.deleteCustomer(customer)

                    Toast.makeText(
                        requireContext(),
                        "Customer deleted",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().navigateUp()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadRentalHistory(customerId: Long) {

        viewLifecycleOwner.lifecycleScope.launch {

            val database =
                AppDatabase.getDatabase(requireContext())

            val rentals =
                database.rentalDao()
                    .getRentalsForCustomer(customerId)

            binding.rentalsContainer.removeAllViews()

            if (rentals.isEmpty()) {

                binding.textNoRentals.visibility =
                    View.VISIBLE

                return@launch
            }

            binding.textNoRentals.visibility =
                View.GONE

            for (rental in rentals) {

                val car =
                    database.carDao()
                        .getCarById(rental.carId)

                val startDate =
                    dateFormat.format(
                        Date(rental.startDate)
                    )

                val endDate =
                    dateFormat.format(
                        Date(rental.endDate)
                    )

                val carName =
                    if (car != null) {
                        "${car.brand} ${car.model} • ${car.licensePlate}"
                    } else {
                        "Unknown car"
                    }

                val textView =
                    TextView(requireContext())

                textView.text =
                    "$carName\n" +
                            "$startDate → $endDate\n" +
                            "Total: €%.2f\n".format(rental.totalPrice) +
                            "Status: ${rental.status}"

                textView.textSize = 17f

                textView.setPadding(
                    20,
                    20,
                    20,
                    30
                )

                textView.setOnClickListener {

                    val bundle =
                        Bundle().apply {
                            putLong(
                                "rentalId",
                                rental.id
                            )
                        }

                    findNavController().navigate(
                        R.id.action_CustomerDetailsFragment_to_RentalDetailsFragment,
                        bundle
                    )
                }

                binding.rentalsContainer.addView(
                    textView
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val customerId =
            arguments?.getLong("customerId") ?: 0L

        if (::viewModel.isInitialized && customerId != 0L) {
            loadCustomer(customerId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
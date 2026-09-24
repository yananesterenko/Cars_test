package com.example.cars_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cars_test.R
import com.example.cars_test.database.AppDatabase
import com.example.cars_test.databinding.FragmentCustomersBinding
import com.example.cars_test.repository.CustomerRepository
import com.example.cars_test.viewmodel.CustomerViewModel
import com.example.cars_test.viewmodel.CustomerViewModelFactory
import kotlinx.coroutines.launch

class CustomersFragment : Fragment() {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CustomerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCustomersBinding.inflate(
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

        binding.buttonAddCustomer.setOnClickListener {

            findNavController().navigate(
                R.id.action_CustomersFragment_to_AddCustomerFragment
            )
        }
    }

    private fun loadCustomers() {

        viewLifecycleOwner.lifecycleScope.launch {

            val customers =
                viewModel.getAllCustomers()

            binding.customersContainer.removeAllViews()

            if (customers.isEmpty()) {

                binding.textNoCustomers.visibility =
                    View.VISIBLE

                return@launch
            }

            binding.textNoCustomers.visibility =
                View.GONE

            for (customer in customers) {

                val textView =
                    TextView(requireContext())

                textView.text =
                    "${customer.firstName} ${customer.lastName}\n" +
                            "Phone: ${customer.phone}\n" +
                            "Email: ${customer.email}\n" +
                            "Driver license: ${customer.driverLicense}"

                textView.textSize = 18f

                textView.setPadding(
                    20,
                    20,
                    20,
                    30
                )

                textView.setOnClickListener {

                    val bundle = Bundle().apply {
                        putLong("customerId", customer.id)
                    }

                    findNavController().navigate(
                        R.id.action_CustomersFragment_to_CustomerDetailsFragment,
                        bundle
                    )
                }

                binding.customersContainer.addView(
                    textView
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (::viewModel.isInitialized) {
            loadCustomers()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
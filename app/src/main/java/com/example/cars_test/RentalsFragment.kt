package com.example.cars_test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cars_test.database.AppDatabase
import com.example.cars_test.databinding.FragmentRentalsBinding
import com.example.cars_test.repository.RentalRepository
import com.example.cars_test.viewmodel.RentalViewModel
import com.example.cars_test.viewmodel.RentalViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale




class RentalsFragment : Fragment() {

    private var _binding: FragmentRentalsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RentalViewModel

    private val dateFormat =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRentalsBinding.inflate(
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
            RentalRepository(database.rentalDao())

        val factory =
            RentalViewModelFactory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[RentalViewModel::class.java]
        binding.buttonNewRental.setOnClickListener {
            findNavController().navigate(
                R.id.action_RentalsFragment_to_NewRentalFragment
            )
        }

    }

    private fun loadRentals() {

        val database =
            AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {

            val rentals =
                viewModel.getAllRentals()

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

                val textView =
                    TextView(requireContext())

                val carName =
                    if (car != null) {
                        "${car.brand} ${car.model} • ${car.licensePlate}"
                    } else {
                        "Unknown car"
                    }

                val start =
                    dateFormat.format(
                        Date(rental.startDate)
                    )

                val end =
                    dateFormat.format(
                        Date(rental.endDate)
                    )

                textView.text =
                    "$carName\n" +
                            "$start → $end\n" +
                            "Total: €%.2f\n".format(rental.totalPrice) +
                            "Status: ${rental.status}"

                textView.textSize = 18f

                textView.setPadding(
                    20,
                    20,
                    20,
                    30
                )

                textView.setOnClickListener {

                    val bundle = Bundle().apply {
                        putLong("rentalId", rental.id)
                    }

                    findNavController().navigate(
                        R.id.action_RentalsFragment_to_RentalDetailsFragment,
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

        if (::viewModel.isInitialized) {
            loadRentals()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
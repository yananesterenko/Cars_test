package com.example.cars_test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.cars_test.database.AppDatabase
import com.example.cars_test.databinding.FragmentCarDetailsBinding
import com.example.cars_test.repository.CarRepository
import com.example.cars_test.viewmodel.CarViewModel
import com.example.cars_test.viewmodel.CarViewModelFactory
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.example.cars_test.database.CarEntity

class CarDetailsFragment : Fragment() {

    private lateinit var database: AppDatabase

    private var _binding: FragmentCarDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CarViewModel

    private var currentCar: CarEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCarDetailsBinding.inflate(
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

        database = AppDatabase.getDatabase(requireContext())
        val repository = CarRepository(database.carDao())
        val factory = CarViewModelFactory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[CarViewModel::class.java]

        val carId = arguments?.getLong("carId") ?: 0L

        loadCar(carId)
        binding.buttonDeleteCar.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.buttonEditCar.setOnClickListener {

            val car = currentCar ?: return@setOnClickListener

            val bundle = Bundle().apply {
                putLong("carId", car.id)
            }

            findNavController().navigate(
                R.id.action_CarDetailsFragment_to_CarFragment,
                bundle
            )
        }
    }

    private fun loadCar(carId: Long) {

        viewLifecycleOwner.lifecycleScope.launch {

            val car = viewModel.getCarById(carId)

            if (car != null) {
                currentCar = car
                binding.textCarName.text =
                    "${car.brand} ${car.model}"

                binding.textYear.text =
                    "Year: ${car.year}"

                binding.textLicensePlate.text =
                    "License plate: ${car.licensePlate}"

                binding.textVin.text =
                    "VIN: ${car.vin}"

                binding.textMileage.text =
                    "Mileage: ${car.mileage} km"

                binding.textPrice.text =
                    "Price per day: €${car.pricePerDay}"

                binding.textStatus.text =
                    "Status: ${car.status}"

                binding.textNotes.text =
                    "Notes: ${car.notes}"
            }
        }
    }




    private fun showDeleteConfirmation() {

        val car = currentCar ?: return

        viewLifecycleOwner.lifecycleScope.launch {

            val rentals =
                database.rentalDao()
                    .getRentalsForCar(car.id)

            if (rentals.isNotEmpty()) {

                AlertDialog.Builder(requireContext())
                    .setTitle("Cannot delete car")
                    .setMessage(
                        "This car has rental history and cannot be deleted."
                    )
                    .setPositiveButton("OK", null)
                    .show()

                return@launch
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Delete car")
                .setMessage(
                    "Delete ${car.brand} ${car.model}?"
                )
                .setPositiveButton("Delete") { _, _ ->

                    viewModel.deleteCar(car)

                    Toast.makeText(
                        requireContext(),
                        "Car deleted",
                        Toast.LENGTH_SHORT
                    ).show()

                    findNavController().navigateUp()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.cars_test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cars_test.database.AppDatabase
import com.example.cars_test.database.CarEntity
import com.example.cars_test.databinding.FragmentCarBinding
import com.example.cars_test.repository.CarRepository
import com.example.cars_test.viewmodel.CarViewModel
import com.example.cars_test.viewmodel.CarViewModelFactory
import kotlinx.coroutines.launch

class CarFragment : Fragment() {

    private var _binding: FragmentCarBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CarViewModel

    private var editingCar: CarEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCarBinding.inflate(
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

        val database = AppDatabase.getDatabase(requireContext())
        val repository = CarRepository(database.carDao())
        val factory = CarViewModelFactory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[CarViewModel::class.java]

        setupStatusSpinner()

        val carId = arguments?.getLong("carId", 0L) ?: 0L

        if (carId != 0L) {
            loadCarForEditing(carId)
        }

        binding.buttonSaveCar.setOnClickListener {
            saveCar()
        }
    }

    private fun setupStatusSpinner() {

        val statuses = listOf(
            "Available",
            "Rented",
            "Maintenance"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statuses
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spinnerStatus.adapter = adapter
    }

    private fun loadCarForEditing(carId: Long) {

        viewLifecycleOwner.lifecycleScope.launch {

            val car = viewModel.getCarById(carId)

            if (car != null) {

                editingCar = car

                binding.editBrand.setText(car.brand)
                binding.editModel.setText(car.model)
                binding.editYear.setText(car.year.toString())
                binding.editLicensePlate.setText(car.licensePlate)
                binding.editVin.setText(car.vin)
                binding.editMileage.setText(car.mileage.toString())
                binding.editPricePerDay.setText(car.pricePerDay.toString())
                binding.editNotes.setText(car.notes)

                val statuses = listOf(
                    "Available",
                    "Rented",
                    "Maintenance"
                )

                val statusPosition =
                    statuses.indexOf(car.status)

                if (statusPosition >= 0) {
                    binding.spinnerStatus.setSelection(statusPosition)
                }
            }
        }
    }

    private fun saveCar() {

        val brand =
            binding.editBrand.text.toString().trim()

        val model =
            binding.editModel.text.toString().trim()

        val yearText =
            binding.editYear.text.toString().trim()

        val licensePlate =
            binding.editLicensePlate.text.toString().trim()

        val vin =
            binding.editVin.text.toString().trim()

        val mileageText =
            binding.editMileage.text.toString().trim()

        val priceText =
            binding.editPricePerDay.text.toString().trim()

        val status =
            binding.spinnerStatus.selectedItem.toString()

        val notes =
            binding.editNotes.text.toString().trim()

        if (brand.isEmpty()) {
            binding.editBrand.error = "Enter brand"
            return
        }

        if (model.isEmpty()) {
            binding.editModel.error = "Enter model"
            return
        }

        if (yearText.isEmpty()) {
            binding.editYear.error = "Enter year"
            return
        }

        if (licensePlate.isEmpty()) {
            binding.editLicensePlate.error = "Enter license plate"
            return
        }

        val year = yearText.toIntOrNull()

        if (year == null) {
            binding.editYear.error = "Invalid year"
            return
        }

        val mileage =
            mileageText.toIntOrNull() ?: 0

        val pricePerDay =
            priceText.toDoubleOrNull() ?: 0.0

        val oldCar = editingCar

        if (oldCar == null) {

            // ADD NEW CAR

            val newCar = CarEntity(
                brand = brand,
                model = model,
                year = year,
                licensePlate = licensePlate,
                vin = vin,
                mileage = mileage,
                pricePerDay = pricePerDay,
                status = status,
                notes = notes
            )

            viewModel.addCar(newCar)

            Toast.makeText(
                requireContext(),
                "Car saved",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            // UPDATE EXISTING CAR

            val updatedCar = oldCar.copy(
                brand = brand,
                model = model,
                year = year,
                licensePlate = licensePlate,
                vin = vin,
                mileage = mileage,
                pricePerDay = pricePerDay,
                status = status,
                notes = notes
            )

            viewModel.updateCar(updatedCar)

            Toast.makeText(
                requireContext(),
                "Car updated",
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
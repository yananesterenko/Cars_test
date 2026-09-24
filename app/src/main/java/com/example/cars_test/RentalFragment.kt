package com.example.cars_test

import androidx.lifecycle.ViewModelProvider
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cars_test.database.AppDatabase
import com.example.cars_test.database.CarEntity
import com.example.cars_test.databinding.FragmentRentalBinding
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import android.widget.Toast
import com.example.cars_test.database.RentalEntity
import androidx.navigation.fragment.findNavController
import com.example.cars_test.repository.RentalRepository
import com.example.cars_test.viewmodel.RentalViewModel
import com.example.cars_test.viewmodel.RentalViewModelFactory

import com.example.cars_test.repository.CarRepository
import com.example.cars_test.viewmodel.CarViewModel
import com.example.cars_test.viewmodel.CarViewModelFactory

import com.example.cars_test.database.CustomerEntity
class RentalFragment : Fragment() {

    private var _binding: FragmentRentalBinding? = null
    private val binding get() = _binding!!

    private var cars: List<CarEntity> = emptyList()

    private var customers: List<CustomerEntity> = emptyList()

    private var startDate: Long? = null
    private var endDate: Long? = null

    private val dateFormat =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private lateinit var rentalViewModel: RentalViewModel


    private lateinit var carViewModel: CarViewModel

    private var editingRental: RentalEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRentalBinding.inflate(
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

        val carRepository =
            CarRepository(database.carDao())

        val carFactory =
            CarViewModelFactory(carRepository)

        val rentalId =
            arguments?.getLong("rentalId", 0L) ?: 0L

        carViewModel = ViewModelProvider(
            this,
            carFactory
        )[CarViewModel::class.java]

        val rentalRepository = RentalRepository(
            database.rentalDao()
        )

        val rentalFactory =
            RentalViewModelFactory(rentalRepository)

        rentalViewModel = ViewModelProvider(
            this,
            rentalFactory
        )[RentalViewModel::class.java]

        binding.buttonSaveRental.setOnClickListener {
            saveRental()
        }

        loadFormData(rentalId)


        binding.buttonStartDate.setOnClickListener {
            showDatePicker(true)
        }
        binding.buttonEndDate.setOnClickListener {
            showDatePicker(false)
        }
        binding.spinnerCar.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    updatePrice()
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }
    }

    private fun loadFormData(rentalId: Long) {

        val database =
            AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {

            customers =
                database.customerDao()
                    .getAllCustomers()

            /*
             * Для новой аренды нужны только Available машины.
             *
             * Для Edit Rental ниже отдельно добавим машину
             * текущей аренды, потому что она обычно уже Rented.
             */
            val availableCars =
                database.carDao()
                    .getAvailableCars()

            editingRental =
                if (rentalId != 0L) {
                    rentalViewModel.getRentalById(rentalId)
                } else {
                    null
                }

            val rental = editingRental

            cars =
                if (rental != null) {

                    val currentCar =
                        database.carDao()
                            .getCarById(rental.carId)

                    if (
                        currentCar != null &&
                        availableCars.none { it.id == currentCar.id }
                    ) {
                        listOf(currentCar) + availableCars
                    } else {
                        availableCars
                    }

                } else {
                    availableCars
                }

            setupCustomerSpinner()
            setupCarSpinner()

            if (rental != null) {
                fillRentalForm(rental)
            } else {
                updatePrice()
            }
        }
    }

    private fun fillRentalForm(
        rental: RentalEntity
    ) {

        val customerIndex =
            customers.indexOfFirst {
                it.id == rental.customerId
            }

        if (customerIndex >= 0) {
            binding.spinnerCustomer.setSelection(
                customerIndex
            )
        }

        val carIndex =
            cars.indexOfFirst {
                it.id == rental.carId
            }

        if (carIndex >= 0) {
            binding.spinnerCar.setSelection(
                carIndex
            )
        }

        startDate = rental.startDate
        endDate = rental.endDate

        binding.buttonStartDate.text =
            dateFormat.format(
                Date(rental.startDate)
            )

        binding.buttonEndDate.text =
            dateFormat.format(
                Date(rental.endDate)
            )

        binding.editNotes.setText(
            rental.notes
        )

        binding.buttonSaveRental.text =
            "Save Changes"

        updatePrice()
    }

    private fun setupCustomerSpinner() {

        val names =
            customers.map {
                "${it.firstName} ${it.lastName}"
            }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            names
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spinnerCustomer.adapter = adapter
    }

    private fun setupCarSpinner() {

        val names =
            cars.map {
                "${it.brand} ${it.model} — ${it.licensePlate}"
            }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            names
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spinnerCar.adapter = adapter

        binding.spinnerCar.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    updatePrice()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                }
            }
    }

    private fun loadCars() {

        val database = AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {


            cars = database.carDao().getAvailableCars()

            val carNames = cars.map { car ->
                "${car.brand} ${car.model} — ${car.licensePlate}"
            }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                carNames
            )

            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            binding.spinnerCar.adapter = adapter
        }
    }

    private fun loadCustomers() {

        val database =
            AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {

            customers =
                database.customerDao().getAllCustomers()

            val customerNames =
                customers.map { customer ->
                    "${customer.firstName} ${customer.lastName}"
                }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                customerNames
            )

            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            binding.spinnerCustomer.adapter = adapter
        }
    }
    private fun showDatePicker(isStartDate: Boolean) {

        val calendar = Calendar.getInstance()

        val currentDate =
            if (isStartDate) startDate else endDate

        if (currentDate != null) {
            calendar.timeInMillis = currentDate
        }

        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->

                val selectedCalendar =
                    Calendar.getInstance().apply {

                        set(
                            year,
                            month,
                            dayOfMonth,
                            0,
                            0,
                            0
                        )

                        set(
                            Calendar.MILLISECOND,
                            0
                        )
                    }

                val selectedDate =
                    selectedCalendar.timeInMillis

                if (isStartDate) {

                    startDate = selectedDate

                    binding.buttonStartDate.text =
                        dateFormat.format(
                            Date(selectedDate)
                        )

                } else {

                    endDate = selectedDate

                    binding.buttonEndDate.text =
                        dateFormat.format(
                            Date(selectedDate)
                        )
                }

                updatePrice()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.show()
    }

    private fun updatePrice() {

        if (cars.isEmpty()) {
            return
        }

        val position =
            binding.spinnerCar.selectedItemPosition

        if (position < 0 || position >= cars.size) {
            return
        }

        val selectedCar = cars[position]

        binding.textPricePerDay.text =
            "Price per day: €%.2f".format(
                selectedCar.pricePerDay
            )

        val start = startDate
        val end = endDate

        if (start == null || end == null) {

            binding.textRentalDays.text =
                "Rental days: 0"

            binding.textTotalPrice.text =
                "Total: €0.00"

            return
        }

        if (end < start) {

            binding.textRentalDays.text =
                "End date must be after start date"

            binding.textTotalPrice.text =
                "Total: €0.00"

            return
        }

        val difference =
            end - start

        val days =
            TimeUnit.MILLISECONDS.toDays(difference) + 1

        val total =
            days * selectedCar.pricePerDay

        binding.textRentalDays.text =
            "Rental days: $days"

        binding.textTotalPrice.text =
            "Total: €%.2f".format(total)
    }

    private fun saveRental() {

        if (cars.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No car selected",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val start = startDate
        val end = endDate

        if (start == null) {
            Toast.makeText(
                requireContext(),
                "Select start date",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (end == null) {
            Toast.makeText(
                requireContext(),
                "Select end date",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (end < start) {
            Toast.makeText(
                requireContext(),
                "End date must be after start date",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val position =
            binding.spinnerCar.selectedItemPosition

        if (position < 0 || position >= cars.size) {
            return
        }

        val selectedCar = cars[position]

        if (customers.isEmpty()) {

            Toast.makeText(
                requireContext(),
                "Add a customer first",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val customerPosition =
            binding.spinnerCustomer.selectedItemPosition

        if (
            customerPosition < 0 ||
            customerPosition >= customers.size
        ) {
            return
        }

        val selectedCustomer =
            customers[customerPosition]

        val days =
            TimeUnit.MILLISECONDS.toDays(
                end - start
            ) + 1

        val totalPrice =
            days * selectedCar.pricePerDay

        val oldRental = editingRental

        if (oldRental == null) {

            // NEW RENTAL

            val rental = RentalEntity(
                carId = selectedCar.id,
                customerId = selectedCustomer.id,
                startDate = start,
                endDate = end,
                pricePerDay = selectedCar.pricePerDay,
                totalPrice = totalPrice,
                status = "Active",
                notes = binding.editNotes.text
                    .toString()
                    .trim()
            )

            rentalViewModel.addRental(rental)

            carViewModel.updateCar(
                selectedCar.copy(
                    status = "Rented"
                )
            )

            Toast.makeText(
                requireContext(),
                "Rental saved",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            // EDIT RENTAL

            val updatedRental =
                oldRental.copy(
                    carId = selectedCar.id,
                    customerId = selectedCustomer.id,
                    startDate = start,
                    endDate = end,
                    pricePerDay = selectedCar.pricePerDay,
                    totalPrice = totalPrice,
                    notes = binding.editNotes.text
                        .toString()
                        .trim()
                )

            rentalViewModel.updateRental(
                updatedRental
            )

            /*
             * Если пользователь поменял машину,
             * старую освобождаем, новую ставим Rented.
             */
            if (oldRental.carId != selectedCar.id) {

                val database =
                    AppDatabase.getDatabase(
                        requireContext()
                    )

                viewLifecycleOwner.lifecycleScope.launch {

                    val oldCar =
                        database.carDao()
                            .getCarById(oldRental.carId)

                    if (oldCar != null) {
                        carViewModel.updateCar(
                            oldCar.copy(
                                status = "Available"
                            )
                        )
                    }

                    carViewModel.updateCar(
                        selectedCar.copy(
                            status = "Rented"
                        )
                    )
                }
            }

            Toast.makeText(
                requireContext(),
                "Rental updated",
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
package com.example.cars_test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cars_test.database.AppDatabase
import com.example.cars_test.database.CarEntity
import com.example.cars_test.database.RentalEntity
import com.example.cars_test.databinding.FragmentRentalDetailsBinding
import com.example.cars_test.repository.CarRepository
import com.example.cars_test.repository.RentalRepository
import com.example.cars_test.viewmodel.CarViewModel
import com.example.cars_test.viewmodel.CarViewModelFactory
import com.example.cars_test.viewmodel.RentalViewModel
import com.example.cars_test.viewmodel.RentalViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.cars_test.database.CustomerEntity
class RentalDetailsFragment : Fragment() {

    private lateinit var database: AppDatabase
    private var _binding: FragmentRentalDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var rentalViewModel: RentalViewModel
    private lateinit var carViewModel: CarViewModel


    private var currentRental: RentalEntity? = null
    private var currentCar: CarEntity? = null
    private var currentCustomer: CustomerEntity? = null

    private val dateFormat =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRentalDetailsBinding.inflate(
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

        val rentalRepository =
            RentalRepository(database.rentalDao())

        val rentalFactory =
            RentalViewModelFactory(rentalRepository)

        rentalViewModel = ViewModelProvider(
            this,
            rentalFactory
        )[RentalViewModel::class.java]

        val carRepository =
            CarRepository(database.carDao())

        val carFactory =
            CarViewModelFactory(carRepository)

        carViewModel = ViewModelProvider(
            this,
            carFactory
        )[CarViewModel::class.java]

        val rentalId =
            arguments?.getLong("rentalId") ?: 0L

        loadRental(rentalId)

        binding.buttonFinishRental.setOnClickListener {
            confirmFinishRental()
        }

        binding.textCustomer.setOnClickListener {

            val customer =
                currentCustomer ?: return@setOnClickListener

            val bundle = Bundle().apply {
                putLong(
                    "customerId",
                    customer.id
                )
            }

            findNavController().navigate(
                R.id.action_RentalDetailsFragment_to_CustomerDetailsFragment,
                bundle
            )
        }

        binding.buttonEditRental.setOnClickListener {

            val rental = currentRental
                ?: return@setOnClickListener

            val bundle = Bundle().apply {
                putLong("rentalId", rental.id)
            }

            findNavController().navigate(
                R.id.action_RentalDetailsFragment_to_NewRentalFragment,
                bundle
            )
        }
    }

    private fun loadRental(rentalId: Long) {

        viewLifecycleOwner.lifecycleScope.launch {

            val rental =
                rentalViewModel.getRentalById(rentalId)

            if (rental == null) {
                return@launch
            }

            val customer =
                database.customerDao()
                    .getCustomerById(rental.customerId)

            currentCustomer = customer



            currentRental = rental

            currentCustomer = customer

            if (customer != null) {

                binding.textCustomer.text =
                    "${customer.firstName} ${customer.lastName}"

                binding.textCustomerPhone.text =
                    "Phone: ${customer.phone}"

                binding.textCustomerEmail.text =
                    "Email: ${customer.email}"

            } else {

                binding.textCustomer.text =
                    "Unknown customer"

                binding.textCustomerPhone.text = ""
                binding.textCustomerEmail.text = ""
            }

            if (customer != null) {

                binding.textCustomer.text =
                    "${customer.firstName} ${customer.lastName}"

                binding.textCustomerPhone.text =
                    "Phone: ${customer.phone}"

                binding.textCustomerEmail.text =
                    "Email: ${customer.email}"

            } else {

                binding.textCustomer.text =
                    "Unknown customer"

                binding.textCustomerPhone.text = ""
                binding.textCustomerEmail.text = ""
            }

            val car =
                carViewModel.getCarById(rental.carId)

            currentCar = car

            if (car != null) {
                binding.textCar.text =
                    "${car.brand} ${car.model}\n${car.licensePlate}"
            } else {
                binding.textCar.text = "Unknown car"
            }

            val start =
                dateFormat.format(Date(rental.startDate))

            val end =
                dateFormat.format(Date(rental.endDate))

            binding.textDates.text =
                "$start → $end"

            binding.textPricePerDay.text =
                "Price per day: €%.2f".format(
                    rental.pricePerDay
                )

            binding.textTotalPrice.text =
                "Total: €%.2f".format(
                    rental.totalPrice
                )

            binding.textStatus.text =
                "Status: ${rental.status}"

            binding.textNotes.text =
                "Notes: ${rental.notes}"

            binding.buttonFinishRental.visibility =
                if (rental.status == "Active") {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            binding.buttonEditRental.visibility =
                if (rental.status == "Active") {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            binding.buttonFinishRental.visibility =
                if (rental.status == "Active") {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun confirmFinishRental() {

        val rental = currentRental ?: return

        AlertDialog.Builder(requireContext())
            .setTitle("Finish rental")
            .setMessage("Mark this rental as completed?")
            .setPositiveButton("Finish") { _, _ ->
                finishRental()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun finishRental() {

        val rental = currentRental ?: return
        val car = currentCar ?: return

        val completedRental =
            rental.copy(
                status = "Completed",
                completedAt = System.currentTimeMillis()
            )

        val availableCar =
            car.copy(
                status = "Available"
            )

        rentalViewModel.updateRental(
            completedRental
        )

        carViewModel.updateCar(
            availableCar
        )

        Toast.makeText(
            requireContext(),
            "Rental completed",
            Toast.LENGTH_SHORT
        ).show()

        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
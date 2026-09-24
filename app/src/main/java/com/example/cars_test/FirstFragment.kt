package com.example.cars_test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cars_test.database.AppDatabase
import com.example.cars_test.databinding.FragmentFirstBinding
import kotlinx.coroutines.launch
import java.util.Calendar

import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonCars = view.findViewById<View>(R.id.buttonCars)

        buttonCars.setOnClickListener {
            findNavController().navigate(
                R.id.action_FirstFragment_to_CarsFragment
            )
        }

        binding.buttonRentals.setOnClickListener {
            findNavController().navigate(
                R.id.action_FirstFragment_to_RentalsFragment
            )
        }

        binding.buttonCustomers.setOnClickListener {
            findNavController().navigate(
                R.id.action_FirstFragment_to_CustomersFragment
            )
        }

        binding.textSelectedDate.setOnClickListener {
            openDatePicker()
        }
    }

    private fun openDatePicker() {

        val picker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(selectedDate)
                .build()

        picker.addOnPositiveButtonClickListener { date ->

            selectedDate = date

            showSelectedDate()

            //loadDateInfo(selectedDate)
        }

        picker.show(
            parentFragmentManager,
            "DATE_PICKER"
        )
    }

    private fun showSelectedDate() {

        val dateFormat = SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.getDefault()
        )

        binding.textSelectedDate.text =
            dateFormat.format(Date(selectedDate))
    }




    private fun loadStatistics() {

        viewLifecycleOwner.lifecycleScope.launch {

            val database =
                AppDatabase.getDatabase(requireContext())

            val totalCars =
                database.carDao().getCarsCount()

            val availableCars =
                database.carDao().getAvailableCarsCount()

            val rentedCars =
                database.carDao().getRentedCarsCount()

            val maintenanceCars =
                database.carDao().getMaintenanceCarsCount()

            val customers =
                database.customerDao().getCustomersCount()

            val activeRentals =
                database.rentalDao().getActiveRentalsCount()

            val completedRentals =
                database.rentalDao().getCompletedRentalsCount()

            val today =
                Calendar.getInstance()

            today.set(
                Calendar.HOUR_OF_DAY,
                0
            )

            today.set(
                Calendar.MINUTE,
                0
            )

            today.set(
                Calendar.SECOND,
                0
            )

            today.set(
                Calendar.MILLISECOND,
                0
            )

            val startOfDay =
                today.timeInMillis

            val tomorrow =
                today.clone() as Calendar

            tomorrow.add(
                Calendar.DAY_OF_MONTH,
                1
            )

            val startOfNextDay =
                tomorrow.timeInMillis

            val revenueToday =
                database.rentalDao().getRevenueForDay(
                    startOfDay,
                    startOfNextDay
                )

            val month =
                Calendar.getInstance()

            month.set(
                Calendar.DAY_OF_MONTH,
                1
            )

            month.set(
                Calendar.HOUR_OF_DAY,
                0
            )

            month.set(
                Calendar.MINUTE,
                0
            )

            month.set(
                Calendar.SECOND,
                0
            )

            month.set(
                Calendar.MILLISECOND,
                0
            )

            val startOfMonth =
                month.timeInMillis

            val nextMonth =
                month.clone() as Calendar

            nextMonth.add(
                Calendar.MONTH,
                1
            )

            val startOfNextMonth =
                nextMonth.timeInMillis

            val revenueThisMonth =
                database.rentalDao().getRevenueForMonth(
                    startOfMonth,
                    startOfNextMonth
                )

            binding.textStatistics.text = """
    Cars: $totalCars
    Available: $availableCars
    Rented: $rentedCars
    Maintenance: $maintenanceCars

    Customers: $customers

    Active Rentals: $activeRentals
    Completed Rentals: $completedRentals

    Revenue Today: €%.2f
    Revenue This Month: €%.2f
""".trimIndent().format(
                revenueToday,
                revenueThisMonth
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

    override fun onResume() {
        super.onResume()

        if (_binding != null) {
            loadStatistics()
        }
    }
}
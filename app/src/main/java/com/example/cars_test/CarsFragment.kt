package com.example.cars_test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cars_test.database.AppDatabase
import com.example.cars_test.databinding.FragmentCarsBinding
import com.example.cars_test.repository.CarRepository
import com.example.cars_test.viewmodel.CarViewModel
import com.example.cars_test.viewmodel.CarViewModelFactory
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CarsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CarsFragment : Fragment() {

    private var _binding: FragmentCarsBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: CarViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCarsBinding.inflate(inflater, container, false)
        return binding.root
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_cars, container, false)
    }



    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())

        val repository = CarRepository(
            database.carDao()
        )

        val factory = CarViewModelFactory(repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[CarViewModel::class.java]

        binding.buttonAddCar.setOnClickListener {
            findNavController().navigate(
                R.id.action_CarsFragment_to_CarFragment
            )
        }

        loadCars()
    }

    private fun loadCars() {

        viewLifecycleOwner.lifecycleScope.launch {

            val cars = viewModel.getAllCars()

            binding.carsContainer.removeAllViews()

            for (car in cars) {

                val carView = layoutInflater.inflate(
                    R.layout.item_car,
                    binding.carsContainer,
                    false
                )

                val textCarName =
                    carView.findViewById<TextView>(R.id.textCarName)

                val textCarPlate =
                    carView.findViewById<TextView>(R.id.textCarPlate)

                val textCarInfo =
                    carView.findViewById<TextView>(R.id.textCarInfo)

                val textCarStatus =
                    carView.findViewById<TextView>(R.id.textCarStatus)

                textCarName.text =
                    "${car.brand} ${car.model}"

                textCarPlate.text =
                    "${car.year} • ${car.licensePlate}"

                textCarInfo.text =
                    "Mileage: ${car.mileage} km • €${car.pricePerDay} / day"

                textCarStatus.text =
                    "Status: ${car.status}"

                carView.setOnClickListener {

                    val bundle = Bundle().apply {
                        putLong("carId", car.id)
                    }

                    findNavController().navigate(
                        R.id.action_CarsFragment_to_CarDetailsFragment,
                        bundle
                    )
                }


                binding.carsContainer.addView(carView)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (::viewModel.isInitialized) {
            loadCars()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CarsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CarsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
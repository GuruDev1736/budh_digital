package com.budhdigital.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.budhdigital.app.R
import com.budhdigital.app.ServiceRequestActivity
import com.budhdigital.app.adapters.ServiceAdapter
import com.budhdigital.app.databinding.FragmentServiceBinding
import com.budhdigital.app.models.ServiceItem

class ServiceFragment : Fragment() {
    private var _binding: FragmentServiceBinding? = null
    private val binding get() = _binding!!
    private lateinit var serviceAdapter: ServiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val services = getServiceList()
        serviceAdapter = ServiceAdapter(services) { service ->
            openServiceRequest(service)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = serviceAdapter
        }
    }

    private fun openServiceRequest(service: ServiceItem) {
        val intent = Intent(requireContext(), ServiceRequestActivity::class.java)
        intent.putExtra("SERVICE_ID", service.id.toString())
        intent.putExtra("SERVICE_NAME", service.title)
        startActivity(intent)
    }

    private fun getServiceList(): List<ServiceItem> {
        return listOf(
            ServiceItem(
                id = 1,
                title = "Passport Services",
                description = "New applications, renewals, updates",
                icon = R.drawable.ic_passport,
                iconColor = R.color.service_blue
            ),
            ServiceItem(
                id = 2,
                title = "Visa Assistance",
                description = "Tourist, student, and work visas",
                icon = R.drawable.ic_visa,
                iconColor = R.color.service_purple
            ),
            ServiceItem(
                id = 3,
                title = "Rental Agreements",
                description = "Drafting, notarization & delivery",
                icon = R.drawable.ic_rental,
                iconColor = R.color.service_green
            ),
            ServiceItem(
                id = 4,
                title = "Affidavits",
                description = "Name change, gap year, income proof",
                icon = R.drawable.ic_affidavit,
                iconColor = R.color.service_orange
            ),
            ServiceItem(
                id = 5,
                title = "GST Registration",
                description = "New business filing & returns",
                icon = R.drawable.ic_gst,
                iconColor = R.color.service_purple
            ),
            ServiceItem(
                id = 6,
                title = "PAN Card",
                description = "New application and correction",
                icon = R.drawable.ic_pan,
                iconColor = R.color.service_red
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


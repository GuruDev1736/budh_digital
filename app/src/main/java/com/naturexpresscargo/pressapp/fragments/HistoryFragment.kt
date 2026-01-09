package com.naturexpresscargo.pressapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.naturexpresscargo.pressapp.R
import com.naturexpresscargo.pressapp.adapters.HistoryAdapter
import com.naturexpresscargo.pressapp.databinding.FragmentHistoryBinding
import com.naturexpresscargo.pressapp.models.HistoryItem
import com.naturexpresscargo.pressapp.models.ServiceRequest
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val historyList = mutableListOf<HistoryItem>()
    private val requestsMap = mutableMapOf<String, ServiceRequest>()
    private var requestsListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupRecyclerView()
        loadUserRequests()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(historyList) { item, action ->
            handleAction(item, action)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun loadUserRequests() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please login to view history", Toast.LENGTH_SHORT).show()
            return
        }

        _binding?.let { binding ->
            binding.progressBar.visibility = View.VISIBLE
            binding.tvNoHistory.visibility = View.GONE
        }

        val userId = currentUser.uid
        val requestsRef = database.reference.child("requests")

        requestsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _binding?.let { binding ->
                    binding.progressBar.visibility = View.GONE
                    historyList.clear()
                    requestsMap.clear()

                    if (snapshot.exists()) {
                        for (requestSnapshot in snapshot.children) {
                            val request = requestSnapshot.getValue(ServiceRequest::class.java)
                            request?.let {
                                requestsMap[it.requestId] = it
                                val historyItem = convertToHistoryItem(it)
                                historyList.add(historyItem)
                            }
                        }

                        historyList.sortByDescending { it.creationDate }
                        historyAdapter.notifyDataSetChanged()

                        binding.tvNoHistory.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        binding.tvNoHistory.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _binding?.let { binding ->
                    binding.progressBar.visibility = View.GONE
                    binding.tvNoHistory.visibility = View.VISIBLE
                }
                context?.let {
                    Toast.makeText(
                        it,
                        "Failed to load requests: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        requestsRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(requestsListener!!)
    }

    private fun convertToHistoryItem(request: ServiceRequest): HistoryItem {
        val icon = getServiceIcon(request.serviceId)
        val iconColor = getServiceIconColor(request.serviceId)
        val statusColor = getStatusColor(request.status)
        val formattedDate = formatDate(request.creationDate)

        return HistoryItem(
            id = request.requestId,
            title = request.serviceName,
            status = request.status,
            statusColor = statusColor,
            date = formattedDate,
            icon = icon,
            iconColor = iconColor,
            actionButton = getActionButton(request.status),
            creationDate = request.creationDate
        )
    }

    private fun getServiceIcon(serviceId: String): Int {
        return when (serviceId) {
            "1" -> R.drawable.ic_passport
            "2" -> R.drawable.ic_visa
            "3" -> R.drawable.ic_rental
            "4" -> R.drawable.ic_affidavit
            "5" -> R.drawable.ic_gst
            "6" -> R.drawable.ic_pan
            else -> R.drawable.ic_service
        }
    }

    private fun getServiceIconColor(serviceId: String): Int {
        return when (serviceId) {
            "1" -> R.color.service_blue
            "2" -> R.color.service_purple
            "3" -> R.color.service_green
            "4" -> R.color.service_orange
            "5" -> R.color.service_purple
            "6" -> R.color.service_red
            else -> R.color.service_blue
        }
    }

    private fun getStatusColor(status: String): Int {
        return when (status.lowercase()) {
            "pending" -> R.color.status_pending
            "in progress" -> R.color.status_in_progress
            "completed" -> R.color.status_completed
            "draft" -> R.color.status_draft
            "delivered" -> R.color.status_delivered
            else -> R.color.status_draft
        }
    }

    private fun getActionButton(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "Track"
            "in progress" -> "Track"
            "completed" -> "Download"
            "delivered" -> "View"
            else -> "View"
        }
    }

    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun handleAction(item: HistoryItem, action: String) {
        val request = requestsMap[item.id]
        if (request != null) {
            showRequestDetailsDialog(request)
        } else {
            Toast.makeText(requireContext(), "Request details not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRequestDetailsDialog(request: ServiceRequest) {
        context?.let { ctx ->
            val dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_request_details, null)

            dialogView.findViewById<TextView>(R.id.tvServiceName).text = request.serviceName
            dialogView.findViewById<TextView>(R.id.tvRequestId).text = request.requestId

            val tvStatus = dialogView.findViewById<TextView>(R.id.tvStatus)
            tvStatus.text = request.status
            tvStatus.setTextColor(ContextCompat.getColor(ctx, getStatusColor(request.status)))

            dialogView.findViewById<TextView>(R.id.tvName).text = request.name
            dialogView.findViewById<TextView>(R.id.tvMobile).text = request.mobileNo
            dialogView.findViewById<TextView>(R.id.tvEmail).text = request.email
            dialogView.findViewById<TextView>(R.id.tvAddress).text = request.address
            dialogView.findViewById<TextView>(R.id.tvRequiredDate).text = request.requiredDate
            dialogView.findViewById<TextView>(R.id.tvSubmittedDate).text = formatDate(request.creationDate)

            val llAdditionalNotes = dialogView.findViewById<View>(R.id.llAdditionalNotes)
            if (request.additionalNote.isNotEmpty()) {
                llAdditionalNotes.visibility = View.VISIBLE
                dialogView.findViewById<TextView>(R.id.tvAdditionalNotes).text = request.additionalNote
            } else {
                llAdditionalNotes.visibility = View.GONE
            }

            AlertDialog.Builder(ctx)
                .setTitle("Request Details")
                .setView(dialogView)
                .setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Remove Firebase listener to prevent memory leaks
        requestsListener?.let {
            database.reference.child("requests").removeEventListener(it)
        }
        _binding = null
    }
}


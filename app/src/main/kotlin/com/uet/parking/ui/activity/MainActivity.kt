package com.uet.parking.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.uet.parking.databinding.ActivityMainBinding
import com.uet.parking.data.model.ParkingSlot
import com.uet.parking.data.repository.ParkingRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: ParkingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRepository()
        setupUI()
        loadParkingData()
    }

    private fun setupRepository() {
        repository = ParkingRepository()
    }

    private fun setupUI() {
        binding.tvTitle.text = "Quản lý Bãi Đỗ Xe UET"
        
        binding.btnCheckAvailability.setOnClickListener {
            checkAvailableParkingSlots()
        }
    }

    private fun loadParkingData() {
        val slots = repository.getAllParkingSlots()
        updateUI(slots)
    }

    private fun checkAvailableParkingSlots() {
        val availableSlots = repository.getAvailableSlots()
        binding.tvParkingInfo.text = "Số chỗ trống: ${availableSlots.size}"
    }

    private fun updateUI(slots: List<ParkingSlot>) {
        val totalSlots = slots.size
        val availableSlots = slots.count { it.isAvailable }
        val occupiedSlots = totalSlots - availableSlots

        binding.tvTotalSlots.text = "Tổng chỗ: $totalSlots"
        binding.tvAvailableSlots.text = "Trống: $availableSlots"
        binding.tvOccupiedSlots.text = "Đã đỗ: $occupiedSlots"
    }
}

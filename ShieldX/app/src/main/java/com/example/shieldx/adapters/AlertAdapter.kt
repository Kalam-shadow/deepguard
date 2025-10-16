package com.example.shieldx.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shieldx.R
import com.example.shieldx.databinding.ItemAlertBinding
import com.example.shieldx.models.Alert
import java.text.SimpleDateFormat
import java.util.*

class AlertAdapter(
    private val alerts: List<Alert>,
    private val onItemClick: (Alert) -> Unit
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(private val binding: ItemAlertBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(alert: Alert) {
            binding.apply {
                tvAlertTitle.text = alert.title
                tvAlertMessage.text = alert.message
                tvAlertTime.text = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date(alert.timestamp))
                tvAppName.text = alert.appName
                tvThreatType.text = alert.threatType
                tvConfidence.text = "${alert.confidence}%"
                
                // Set threat level color based on confidence
                val confidenceColor = when {
                    alert.confidence >= 80 -> R.color.danger_color
                    alert.confidence >= 60 -> R.color.warning_color
                    else -> R.color.success_color
                }
                tvConfidence.setTextColor(root.context.getColor(confidenceColor))
                
                root.setOnClickListener { onItemClick(alert) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount() = alerts.size
}

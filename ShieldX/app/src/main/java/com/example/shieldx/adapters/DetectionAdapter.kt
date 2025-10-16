package com.example.shieldx.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shieldx.R
import com.example.shieldx.databinding.ItemDetectionBinding
import com.example.shieldx.models.Detection
import java.text.SimpleDateFormat
import java.util.*

class DetectionAdapter(
    private val detections: List<Detection>,
    private val onItemClick: (Detection) -> Unit
) : RecyclerView.Adapter<DetectionAdapter.DetectionViewHolder>() {

    inner class DetectionViewHolder(private val binding: ItemDetectionBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(detection: Detection) {
            binding.apply {
                tvDetectionType.text = detection.type
                tvDetectionSource.text = detection.source
                tvDetectionTime.text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    .format(Date(detection.timestamp))
                tvDetectionConfidence.text = "${detection.confidence}%"
                
                // Set confidence color
                val confidenceColor = when {
                    detection.confidence >= 80 -> R.color.danger_color
                    detection.confidence >= 60 -> R.color.warning_color
                    else -> R.color.success_color
                }
                tvDetectionConfidence.setTextColor(root.context.getColor(confidenceColor))
                
                // Set icon based on detection type
                val iconRes = when(detection.type.lowercase()) {
                    "harassment" -> R.drawable.ic_warning
                    "deepfake" -> R.drawable.ic_image
                    "spam" -> R.drawable.ic_spam
                    else -> R.drawable.ic_shield
                }
                ivDetectionIcon.setImageResource(iconRes)
                
                root.setOnClickListener { onItemClick(detection) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionViewHolder {
        val binding = ItemDetectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DetectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetectionViewHolder, position: Int) {
        holder.bind(detections[position])
    }

    override fun getItemCount() = detections.size
}

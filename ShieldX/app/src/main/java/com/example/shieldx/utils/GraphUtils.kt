package com.example.shieldx.utils

import android.graphics.Color

/**
 * DeepGuard v3.0 - Graph Utilities
 * Helper functions for charts and visual components
 */
object GraphUtils {
    
    /**
     * Get color based on safety score
     */
    fun getSafetyScoreColor(score: Double): Int {
        return when {
            score >= 80 -> Color.parseColor("#4CAF50") // Green
            score >= 60 -> Color.parseColor("#FFC107") // Yellow
            score >= 40 -> Color.parseColor("#FF9800") // Orange
            else -> Color.parseColor("#F44336") // Red
        }
    }
    
    /**
     * Get risk level text based on score
     */
    fun getRiskLevelText(score: Double): String {
        return when {
            score >= 80 -> "Low Risk"
            score >= 60 -> "Medium Risk"
            score >= 40 -> "High Risk"
            else -> "Critical Risk"
        }
    }
    
    /**
     * Format percentage with color
     */
    fun formatPercentageWithColor(value: Double): Pair<String, Int> {
        val percentage = "${value.toInt()}%"
        val color = getSafetyScoreColor(value)
        return Pair(percentage, color)
    }
    
    /**
     * Get chart colors array
     */
    fun getChartColors(): IntArray {
        return intArrayOf(
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#FFEB3B"), // Yellow
            Color.parseColor("#795548")  // Brown
        )
    }
    
    /**
     * Format number with K/M suffix
     */
    fun formatNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
            else -> number.toString()
        }
    }
    
    /**
     * Get gradient colors for safety score
     */
    fun getSafetyGradientColors(score: Double): IntArray {
        return when {
            score >= 80 -> intArrayOf(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#8BC34A")
            )
            score >= 60 -> intArrayOf(
                Color.parseColor("#FFC107"),
                Color.parseColor("#FFEB3B")
            )
            score >= 40 -> intArrayOf(
                Color.parseColor("#FF9800"),
                Color.parseColor("#FFB74D")
            )
            else -> intArrayOf(
                Color.parseColor("#F44336"),
                Color.parseColor("#EF5350")
            )
        }
    }
}

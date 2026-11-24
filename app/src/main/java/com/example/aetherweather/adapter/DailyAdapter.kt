package com.example.aetherweather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aetherweather.R
import java.text.SimpleDateFormat
import java.util.Locale

data class DailyItem(
    val date: String,
    val weatherCode: Int,
    val minTemp: Double,
    val maxTemp: Double
)

class DailyAdapter(
    private val items: List<DailyItem>,
    private val isCelsius: Boolean = true
) : RecyclerView.Adapter<DailyAdapter.DailyViewHolder>() {

    class DailyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        val tvDayIcon: TextView = itemView.findViewById(R.id.tvDayIcon)
        val tvMin: TextView = itemView.findViewById(R.id.tvMin)
        val tvMax: TextView = itemView.findViewById(R.id.tvMax)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DailyViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily, parent, false)

        return DailyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DailyViewHolder,
        position: Int
    ) {

        val item = items[position]

        holder.tvDay.text = formatDay(item.date)

        holder.tvDayIcon.text =
            getWeatherIcon(item.weatherCode)

        val minTemp = if (isCelsius) {
            item.minTemp
        } else {
            (item.minTemp * 9 / 5) + 32
        }

        val maxTemp = if (isCelsius) {
            item.maxTemp
        } else {
            (item.maxTemp * 9 / 5) + 32
        }

        val unit = if (isCelsius) "C" else "F"

        holder.tvMin.text = "${minTemp.toInt()}°$unit"
        holder.tvMax.text = "${maxTemp.toInt()}°$unit"
    }

    override fun getItemCount() = items.size

    private fun formatDay(date: String): String = try {

        val inputFormat =
            SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val outputFormat =
            SimpleDateFormat("EEE", Locale.getDefault())

        outputFormat.format(inputFormat.parse(date)!!)

    } catch (e: Exception) {
        date
    }

    private fun getWeatherIcon(code: Int): String = when (code) {
        0 -> "☀️"
        1, 2 -> "🌤️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        66, 67 -> "🌧️"
        71, 73, 75, 77 -> "❄️"
        80, 81, 82 -> "🌦️"
        85, 86 -> "🌨️"
        95, 96, 99 -> "⛈️"
        else -> "🌡️"
    }
}

package com.example.aetherweather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aetherweather.R

data class HourlyItem(
    val time: String,
    val temperature: Double,
    val weatherCode: Int
)

class HourlyAdapter(
    private val items: List<HourlyItem>,
    private val isCelsius: Boolean = true
) : RecyclerView.Adapter<HourlyAdapter.HourlyViewHolder>() {

    class HourlyViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvHourIcon: TextView = itemView.findViewById(R.id.tvHourIcon)
        val tvHourTemp: TextView = itemView.findViewById(R.id.tvHourTemp)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HourlyViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly, parent, false)

        return HourlyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: HourlyViewHolder,
        position: Int
    ) {

        val item = items[position]

        holder.tvTime.text =
            if (position == 0) "Now"
            else item.time.substringAfter("T").take(5)

        val temperature = if (isCelsius) {
            item.temperature
        } else {
            (item.temperature * 9 / 5) + 32
        }

        val unit = if (isCelsius) "C" else "F"

        holder.tvHourTemp.text =
            "${temperature.toInt()}°$unit"

        holder.tvHourIcon.text =
            getWeatherIcon(item.weatherCode)
    }

    override fun getItemCount() = items.size

    private fun getWeatherIcon(code: Int): String = when (code) {
        0 -> "☀️"
        1, 2 -> "🌤️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75, 77 -> "❄️"
        80, 81, 82 -> "🌦️"
        95, 96, 99 -> "⛈️"
        else -> "🌡️"
    }
}

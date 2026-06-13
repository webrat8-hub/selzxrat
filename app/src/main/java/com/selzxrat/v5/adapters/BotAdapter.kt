package com.selzxrat.v5.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.selzxrat.v5.BotInfo
import com.selzxrat.v5.R
import java.text.SimpleDateFormat
import java.util.*

class BotAdapter(
    private val onBotClick: (String) -> Unit
) : RecyclerView.Adapter<BotAdapter.BotViewHolder>() {

    private val bots = mutableMapOf<String, BotInfo>()
    private val botList = mutableListOf<String>()

    fun setBots(map: Map<String, BotInfo>) {
        bots.clear()
        bots.putAll(map)
        botList.clear()
        botList.addAll(map.keys)
        notifyDataSetChanged()
    }

    fun updateBot(id: String, info: BotInfo) {
        if (!bots.containsKey(id)) {
            botList.add(id)
        }
        bots[id] = info
        notifyDataSetChanged()
    }

    fun removeBot(id: String) {
        bots.remove(id)
        botList.remove(id)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bot, parent, false)
        return BotViewHolder(view)
    }

    override fun onBindViewHolder(holder: BotViewHolder, position: Int) {
        val id = botList[position]
        val bot = bots[id] ?: return
        holder.bind(id, bot)
    }

    override fun getItemCount() = botList.size

    inner class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cvBot: CardView = itemView.findViewById(R.id.cvBot)
        private val ivStatus: ImageView = itemView.findViewById(R.id.ivBotStatus)
        private val tvName: TextView = itemView.findViewById(R.id.tvBotName)
        private val tvModel: TextView = itemView.findViewById(R.id.tvBotModel)
        private val tvBattery: TextView = itemView.findViewById(R.id.tvBotBattery)
        private val tvLastSeen: TextView = itemView.findViewById(R.id.tvBotLastSeen)
        private val tvIP: TextView = itemView.findViewById(R.id.tvBotIP)
        private val tvCountry: TextView = itemView.findViewById(R.id.tvBotCountry)

        fun bind(id: String, bot: BotInfo) {
            tvName.text = bot.deviceName.ifEmpty { id.take(8) }
            tvModel.text = bot.deviceModel
            tvBattery.text = "${bot.batteryLevel}%"
            tvIP.text = bot.ipAddress.ifEmpty { "N/A" }
            tvCountry.text = bot.country.ifEmpty { "Unknown" }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            tvLastSeen.text = if (bot.lastSeen > 0) sdf.format(Date(bot.lastSeen)) else "Never"

            ivStatus.setImageDrawable(
                ContextCompat.getDrawable(itemView.context,
                    if (bot.isOnline) R.drawable.ic_online else R.drawable.ic_offline)
            )

            cvBot.setOnClickListener { onBotClick(id) }
        }
    }
}

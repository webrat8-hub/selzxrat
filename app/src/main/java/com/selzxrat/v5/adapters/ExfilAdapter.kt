package com.selzxrat.v5.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.selzxrat.v5.ExfiltratedData
import com.selzxrat.v5.R
import java.text.SimpleDateFormat
import java.util.*

class ExfilAdapter : RecyclerView.Adapter<ExfilAdapter.ExfilViewHolder>() {
    private val dataList = mutableListOf<ExfiltratedData>()

    fun setData(list: List<ExfiltratedData>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    fun addData(data: ExfiltratedData) {
        dataList.add(0, data)
        notifyItemInserted(0)
    }

    fun clear() {
        dataList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExfilViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exfil, parent, false)
        return ExfilViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExfilViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount() = dataList.size

    inner class ExfilViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cvExfil: CardView = itemView.findViewById(R.id.cvExfil)
        private val tvType: TextView = itemView.findViewById(R.id.tvExfilType)
        private val tvContent: TextView = itemView.findViewById(R.id.tvExfilContent)
        private val tvDevice: TextView = itemView.findViewById(R.id.tvExfilDevice)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvExfilTimestamp)

        fun bind(data: ExfiltratedData) {
            tvType.text = data.type.uppercase()
            tvContent.text = data.content.take(200) + if (data.content.length > 200) "..." else ""
            tvDevice.text = "Device: ${data.deviceId.take(8)}..."

            // 🔥 FIX: pake getTimestampAsLong() biar aman dari String/Long
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            tvTimestamp.text = sdf.format(Date(data.getTimestampAsLong()))
        }
    }
}

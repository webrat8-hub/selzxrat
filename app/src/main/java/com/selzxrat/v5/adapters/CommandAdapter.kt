package com.selzxrat.v5.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.selzxrat.v5.C2Command
import com.selzxrat.v5.R
import java.text.SimpleDateFormat
import java.util.*

class CommandAdapter : RecyclerView.Adapter<CommandAdapter.CommandViewHolder>() {

    private val commands = mutableListOf<C2Command>()

    fun addCommand(cmd: C2Command) {
        commands.add(0, cmd)
        notifyItemInserted(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_command, parent, false)
        return CommandViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommandViewHolder, position: Int) {
        holder.bind(commands[position])
    }

    override fun getItemCount() = commands.size

    inner class CommandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cvCmd: CardView = itemView.findViewById(R.id.cvCommand)
        private val tvType: TextView = itemView.findViewById(R.id.tvCmdType)
        private val tvPayload: TextView = itemView.findViewById(R.id.tvCmdPayload)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvCmdStatus)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvCmdTimestamp)

        fun bind(cmd: C2Command) {
            tvType.text = cmd.type.uppercase()
            tvPayload.text = cmd.payload.ifEmpty { "(no payload)" }
            tvStatus.text = cmd.status
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            tvTimestamp.text = sdf.format(Date(cmd.timestamp))
        }
    }
}
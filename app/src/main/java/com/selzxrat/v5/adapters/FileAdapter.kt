package com.selzxrat.v5.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.selzxrat.v5.R
import java.io.File

class FileAdapter(
    private val onFileClick: (File) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private var files = listOf<File>()

    fun submitList(list: List<File>) {
        files = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount() = files.size

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivFileIcon)
        private val tvName: TextView = itemView.findViewById(R.id.tvFileName)
        private val tvSize: TextView = itemView.findViewById(R.id.tvFileSize)

        fun bind(file: File) {
            tvName.text = file.name
            tvSize.text = if (file.isDirectory) "Folder" else formatFileSize(file.length())
            ivIcon.setImageResource(if (file.isDirectory) R.drawable.ic_folder else R.drawable.ic_file)
            itemView.setOnClickListener { onFileClick(file) }
        }

        private fun formatFileSize(bytes: Long): String {
            val units = arrayOf("B", "KB", "MB", "GB")
            var size = bytes.toDouble()
            var idx = 0
            while (size >= 1024 && idx < units.size - 1) { size /= 1024; idx++ }
            return "%.1f %s".format(size, units[idx])
        }
    }
}
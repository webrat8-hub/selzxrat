package com.selzxrat.v5.ui

import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.selzxrat.v5.R
import com.selzxrat.v5.adapters.FileAdapter
import java.io.File

class FileBrowserActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvPath: TextView
    private lateinit var btnBack: Button
    private var currentDir = Environment.getExternalStorageDirectory()
    private lateinit var adapter: FileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_browser)

        supportActionBar?.title = "File Browser"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recyclerFiles)
        tvPath = findViewById(R.id.tvCurrentPath)
        btnBack = findViewById(R.id.btnGoBack)

        adapter = FileAdapter { file ->
            if (file.isDirectory) {
                currentDir = file
                loadFiles()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener {
            val parent = currentDir.parentFile
            if (parent != null) {
                currentDir = parent
                loadFiles()
            }
        }

        loadFiles()
    }

    private fun loadFiles() {
        tvPath.text = currentDir.absolutePath
        
        // Mengambil daftar file dan mengurutkannya
        val files = currentDir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            ?: emptyArray()
        
        // Memperbaiki error dengan memberi tahu compiler tipe data secara eksplisit
        adapter.submitList(files.toList() as List<File>)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

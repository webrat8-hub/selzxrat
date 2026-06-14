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
        
        // Mengambil daftar file dari folder
        val filesArray = currentDir.listFiles()
        
        // Menyiapkan daftar yang pasti bertipe List<File>
        val fileList: MutableList<File> = mutableListOf()
        
        if (filesArray != null) {
            // Mengurutkan folder di atas dan file sesuai abjad
            val sortedList = filesArray.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            fileList.addAll(sortedList)
        }
        
        // Memberikan data ke adapter
        adapter.submitList(fileList.toList())
    }

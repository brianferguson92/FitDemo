package com.example.fitdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitdemo.model.DataTypeModel

class GoogleFitActivity : AppCompatActivity() {
    private lateinit var  recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_fit)

        recyclerView = findViewById(R.id.rvFit)

        val dataTypeModels = mutableListOf<DataTypeModel>()

        dataTypeModels.add(DataTypeModel("STEPS:", "ADD STEPS"))

        val googleFitDisplayAdapter = GoogleFitDisplayAdapter(dataTypeModels)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = googleFitDisplayAdapter
    }
}
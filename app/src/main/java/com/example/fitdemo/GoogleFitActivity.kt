package com.example.fitdemo

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitdemo.model.DataTypeModel
import com.example.fitdemo.util.GoogleFitUtil
import com.example.fitdemo.util.StepsUtil
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

class GoogleFitActivity : AppCompatActivity(), GoogleFitDisplayAdapter.GoogleFitDisplayListener {
    private lateinit var  recyclerView: RecyclerView

    private var stepsUtil = StepsUtil()
    private var fitnessUtil = GoogleFitUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_fit)

        recyclerView = findViewById(R.id.rvFit)

        val dataTypeModels = mutableListOf<DataTypeModel>()

        dataTypeModels.add(DataTypeModel("STEPS:", "ADD STEPS", DataType.TYPE_STEP_COUNT_DELTA))

        val googleFitDisplayAdapter = GoogleFitDisplayAdapter(dataTypeModels, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = googleFitDisplayAdapter
    }

    override fun onButtonClicked(dataType: DataType, value: EditText) {
    }

    override fun readData(dataType: DataType): String {
        val fitnessOptions = FitnessOptions.builder()
                .addDataType(dataType)
                .build()

        fitnessUtil.readData(this, fitnessOptions, dataType)
        return "COUNT"
    }

}
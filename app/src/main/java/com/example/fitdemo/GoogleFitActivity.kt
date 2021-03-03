package com.example.fitdemo

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitdemo.model.DataTypeModel
import com.example.fitdemo.util.GoogleFitUtil
import com.example.fitdemo.util.StepsUtil
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field

class GoogleFitActivity : AppCompatActivity(), GoogleFitDisplayAdapter.GoogleFitDisplayListener {
    private lateinit var  recyclerView: RecyclerView

    private var stepsUtil = StepsUtil()
    private var fitnessUtil = GoogleFitUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_fit)

        recyclerView = findViewById(R.id.rvFit)

        val dataTypeModels = mutableListOf<DataTypeModel>()

        dataTypeModels.add(DataTypeModel("STEPS:", "ADD STEPS", DataType.TYPE_STEP_COUNT_DELTA, Field.FIELD_STEPS))
        dataTypeModels.add(DataTypeModel("WEIGHT:", "ADD", DataType.TYPE_WEIGHT, Field.FIELD_WEIGHT))

        val googleFitDisplayAdapter = GoogleFitDisplayAdapter(dataTypeModels, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = googleFitDisplayAdapter
    }

    override fun onButtonClicked(dataType: DataType, value: EditText) {
    }

    override fun readData(dataType: DataType, field: Field, textView: TextView) {
        val fitnessOptions = FitnessOptions.builder()
                .addDataType(dataType)
                .build()

        fitnessUtil.readData(this, fitnessOptions, dataType, field, textView)
    }

}
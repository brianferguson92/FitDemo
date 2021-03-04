package com.example.fitdemo

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.icu.util.Calendar.HOUR_OF_DAY
import android.icu.util.Calendar.MINUTE
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitdemo.model.GoogleFitModel
import com.example.fitdemo.util.GoogleFitUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class GoogleFitActivity : AppCompatActivity(), GoogleFitDisplayAdapter.GoogleFitDisplayListener {
    private lateinit var  recyclerView: RecyclerView

    private var fitnessUtil = GoogleFitUtil()
    private lateinit var fitnessOptions: FitnessOptions

    var endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
    var startTime = endTime.minusHours(1)

    val ANDROID_PERMISSIONS_REQUEST_CODE = 1
    val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_fit)

        recyclerView = findViewById(R.id.rvFit)

        checkPermissions()
        authorizeAccess()

        val googleFitModels = mutableListOf<GoogleFitModel>()

        googleFitModels.add(GoogleFitModel("STEPS:",  DataType.TYPE_STEP_COUNT_DELTA, Field.FIELD_STEPS))
        googleFitModels.add(GoogleFitModel("WEIGHT:",  DataType.TYPE_WEIGHT, Field.FIELD_WEIGHT))
        googleFitModels.add(GoogleFitModel("WATER:",  DataType.TYPE_HYDRATION, Field.FIELD_VOLUME))

        val googleFitDisplayAdapter = GoogleFitDisplayAdapter(googleFitModels, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = googleFitDisplayAdapter
    }

    override fun onButtonClicked(dataType: DataType, field: Field, value: EditText, startTime: ZonedDateTime, endTime: ZonedDateTime, textView: TextView) {
        val fitnessOptions = FitnessOptions.builder()
                .addDataType(dataType)
                .build()

        fitnessUtil.insertData(this, fitnessOptions, dataType, field, value.text.toString().toInt(), startTime, endTime)
        fitnessUtil.readData(this, fitnessOptions, dataType, field, textView)
    }

    override fun readData(dataType: DataType, field: Field, textView: TextView) {
        val fitnessOptions = FitnessOptions.builder()
                .addDataType(dataType)
                .build()

        fitnessUtil.readData(this, fitnessOptions, dataType, field, textView)
    }

    override fun timePicker(textView: TextView) {
        val cldr = Calendar.getInstance()
        TimePickerDialog(this, timeListener(textView), cldr.get(HOUR_OF_DAY), cldr.get(MINUTE), true).show()
    }

    private fun timeListener(textView: TextView): TimePickerDialog.OnTimeSetListener {
        return TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, hour: Int, min: Int ->
            val pattern = "hh:mm:ss a"
            val localTime = LocalTime.of(hour, min)

            textView.text = LocalDateTime.of(LocalDate.now(), localTime).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern))
        }
    }

    private fun authorizeAccess() {
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
                .addDataType(DataType.TYPE_HYDRATION, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HYDRATION, FitnessOptions.ACCESS_WRITE)
                .build()
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    account,
                    fitnessOptions)
        }
    }

    private fun checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
                val permissions = arrayOf(
                        Manifest.permission.ACTIVITY_RECOGNITION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BODY_SENSORS
                )
                ActivityCompat.requestPermissions(this, permissions, ANDROID_PERMISSIONS_REQUEST_CODE)
            }
        }
    }
}
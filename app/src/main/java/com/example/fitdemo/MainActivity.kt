package com.example.fitdemo

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.fitdemo.util.StepsUtil
import com.example.fitdemo.util.WeightUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), StepsUtil.GoogleFitStepsListener, WeightUtil.GoogleFitWeightListener {
    private lateinit var tvTotalSteps: TextView
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var tvWeightStartTime: TextView
    private lateinit var tvWeightEndTime: TextView
    private lateinit var tvWeight: TextView

    private var eTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
    private var sTime = eTime.minusHours(1)


    private lateinit var etSteps: EditText
    private lateinit var etWeight: EditText

    private lateinit var btnSteps: Button
    private lateinit var btnRefreshData: Button
    private lateinit var btnData: Button

    private lateinit var fitnessOptions: FitnessOptions

    var numSteps = 0;

    val ANDROID_PERMISSIONS_REQUEST_CODE = 1
    val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 2

    val googleFitStepsUtil = StepsUtil()
    val googleFitWeightUtil = WeightUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTotalSteps = findViewById(R.id.total_steps)
        tvStartTime = findViewById(R.id.tvStepsStartTime)
        tvEndTime = findViewById(R.id.tvStepsEndTime)
        tvWeightStartTime = findViewById(R.id.tvWeightStartTime)
        tvWeightEndTime = findViewById(R.id.tvWeightEndTime)
        tvWeight = findViewById(R.id.tvWeight)

        etSteps = findViewById(R.id.etSteps)
        etWeight = findViewById(R.id.etWeight)

        btnSteps = findViewById(R.id.btnSteps)
        btnRefreshData = findViewById(R.id.btnRefresh_data)
        btnData = findViewById(R.id.btnData)

        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusHours(1)

        val pattern = "hh:mm:ss a"
        val cldr = Calendar.getInstance()


        tvStartTime.text = "START TIME: ${startTime.format(DateTimeFormatter.ofPattern(pattern))}"
        tvEndTime.text = "END TIME: ${endTime.format(DateTimeFormatter.ofPattern(pattern))}"

        checkPermissions()
        authorizeAccess()
        getData()

        btnSteps.setOnClickListener {
            googleFitStepsUtil.insertSteps(this, fitnessOptions, etSteps.text.toString().toInt(), sTime, eTime)
            googleFitStepsUtil.readCurrentDailySteps(this, fitnessOptions)
            Toast.makeText(this, "Steps Added", Toast.LENGTH_LONG).show()
        }

        tvStartTime.setOnClickListener {
            TimePickerDialog(this, timeListener(tvStartTime, "START TIME:"), cldr.get(Calendar.HOUR_OF_DAY), cldr.get(Calendar.MINUTE), true).show()
        }

        tvEndTime.setOnClickListener {
            val time = timeListener(tvEndTime, "END TIME:")
            TimePickerDialog(this, time, cldr.get(Calendar.HOUR_OF_DAY), cldr.get(Calendar.MINUTE), true).show()
        }

        btnRefreshData.setOnClickListener {
            googleFitStepsUtil.readCurrentDailySteps(this, fitnessOptions)
            Toast.makeText(this, "Date Refreshed", Toast.LENGTH_LONG).show()
        }

        btnData.setOnClickListener {
            //val intent = Intent(this, GoogleFitAcitivty::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Check for auth flow
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> {
                    googleFitStepsUtil.createSensorsClientForSteps(this, fitnessOptions)
                    googleFitStepsUtil.createRecordingClientForSteps(this, fitnessOptions)
                    googleFitStepsUtil.readCurrentDailySteps(this, fitnessOptions)

                    googleFitWeightUtil.readWeight(this, fitnessOptions)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        googleFitStepsUtil.unregisterListeners(this, fitnessOptions)
    }


    //Function to check permissions for Android Q
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

    private fun authorizeAccess() {
        fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(this, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, account, fitnessOptions)
        } else {
            googleFitStepsUtil.createSensorsClientForSteps(this, fitnessOptions)
            googleFitStepsUtil.createRecordingClientForSteps(this, fitnessOptions)
            googleFitStepsUtil.readCurrentDailySteps(this, fitnessOptions)

            googleFitWeightUtil.readWeight(this, fitnessOptions)
        }
    }

    override fun getSensorData(steps: Int) {
        numSteps += steps
    }

    override fun getDailyTotal(totalSteps: Int) {
        tvTotalSteps.text = "STEPS: ${totalSteps}"
    }

    override fun getWeight(weight: Float) {
        val pounds = weight * 2.2046
        tvWeight.text = "WEIGHT: ${pounds.roundToInt()}lbs"
    }

    private fun getData() {

        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusDays(1)

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .read(DataType.TYPE_WEIGHT)
            .read(DataType.TYPE_HEIGHT)
            .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
            .build()

        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readData(readRequest)
            .addOnSuccessListener { response ->
                logData(response.dataSets)
            }
            .addOnFailureListener {
                Log.e("GOOGLE_FIT", it.localizedMessage ?: "")
            }
    }

    private fun logData(dataSets: List<DataSet>) {
        for (data in dataSets) {
            for (dp in data.dataPoints) {
                Log.i("GOOGLE_FIT", "Data point:")
                Log.i("GOOGLE_FIT", "\tType: ${dp.dataType.name}")
                Log.i("GOOGLE_FIT", "\tStart: ${dp.getStartTime(TimeUnit.SECONDS)}")
                Log.i("GOOGLE_FIT", "\tEnd: ${dp.getEndTime(TimeUnit.SECONDS)}")
                for (field in dp.dataType.fields) {
                    Log.i("GOOGLE_FIT", "\tField: ${field.name.toString()} Value: ${dp.getValue(field)}")
                }
            }
        }
    }

    private fun timeListener(view: TextView, time: String): TimePickerDialog.OnTimeSetListener {
        return TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, hour: Int, min: Int ->
            val pattern = "hh:mm:ss a"
            val localTime = LocalTime.of(hour, min)
            view.text = "${time} ${localTime.format(DateTimeFormatter.ofPattern(pattern))}"
            if(time == "START TIME:") {
                sTime = LocalDateTime.of(LocalDate.now(), localTime).atZone(ZoneId.systemDefault())
            } else {
                eTime = LocalDateTime.of(LocalDate.now(), localTime).atZone(ZoneId.systemDefault())
            }
        }
    }
}
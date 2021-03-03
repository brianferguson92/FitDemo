package com.example.fitdemo.util

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataUpdateRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class StepsUtil {
    private lateinit var googleFitStepsListener: GoogleFitStepsListener
    private lateinit var listener: OnDataPointListener

    interface GoogleFitStepsListener {
        fun getSensorData(steps: Int)
        fun getDailyTotal(totalSteps: Int)
    }

    fun createSensorsClientForSteps(context: Context, fitnessOptions: FitnessOptions) {
        googleFitStepsListener = context as GoogleFitStepsListener

        listener = OnDataPointListener {
            for (field in it.dataType.fields) {
                if (field == Field.FIELD_STEPS) {
                    googleFitStepsListener.getSensorData(it.getValue(Field.FIELD_STEPS).asInt())
                }

                Log.i("STEPS", "Detected DataPoint field: ${field.name}")
                Log.i("STEPS", "Detected DataPoint value: ${it.getValue(field)}")
            }
        }

        Fitness.getSensorsClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .add(SensorRequest.Builder()
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setSamplingRate(1, TimeUnit.SECONDS)
                        .build(),
                        listener
                )
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i("STEPS", "Listener registered")
                    }
                }
    }

    fun createRecordingClientForSteps(context: Context, fitnessOptions: FitnessOptions) {
        Fitness.getRecordingClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i("STEPS", "Recording Client created!")
                    }
                }
    }

    fun insertSteps(context: Context, fitnessOptions: FitnessOptions, steps: Int, startTime: ZonedDateTime, endTime: ZonedDateTime ) {

        val dataSource = DataSource.Builder()
                .setAppPackageName(context)
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_RAW)
                .build()

        val dataPoint = DataPoint.builder(dataSource)
                .setField(Field.FIELD_STEPS, steps)
                .setTimeInterval(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build()
        val dataSet = DataSet.builder(dataSource)
                .add(dataPoint)
                .build()

        val request = DataUpdateRequest.Builder()
                .setDataSet(dataSet)
                .setTimeInterval(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .updateData(request)
                .addOnSuccessListener {
                    Log.d("GOOGLE_FIT", "STEPS INSERTED")
                }
                .addOnFailureListener {
                    Log.d("GOOGLE_FIT", it.localizedMessage ?: "")
                }
    }

    fun readCurrentDailySteps(context: Context, fitnessOptions: FitnessOptions) {
        googleFitStepsListener = context as GoogleFitStepsListener

        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener {
                    for (dataPoint in it.dataPoints) {
                        for (field in dataPoint.dataType.fields) {
                            if (field == Field.FIELD_STEPS) {
                                googleFitStepsListener.getDailyTotal(dataPoint.getValue(Field.FIELD_STEPS).asInt())
                            }
                        }
                    }
                }.addOnFailureListener {
                    Log.e("GOOGLE_FIT", it.localizedMessage ?: "")
                }
    }

    fun unregisterListeners(context: Context, fitnessOptions: FitnessOptions) {
        Fitness.getSensorsClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .remove(listener)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i("STEPS", "Listener Removed!")
                    }
                }

        Fitness.getRecordingClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .unsubscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i("STEPS", "Recording Unsubed")
                    }
                }
    }
}
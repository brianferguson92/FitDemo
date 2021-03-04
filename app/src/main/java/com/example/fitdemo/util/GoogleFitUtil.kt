package com.example.fitdemo.util

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataUpdateRequest
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class GoogleFitUtil {
    private lateinit var dataPoint: DataPoint
    fun readData(context: Context, fitnessOptions: FitnessOptions, dataType: DataType, field: Field, textView: TextView) {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusDays(1)

        val readRequest = DataReadRequest.Builder().read(dataType).setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS).build()

        if(dataType == DataType.TYPE_STEP_COUNT_DELTA) {
            Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                    .readDailyTotal(dataType)
                    .addOnSuccessListener {
                            for(dataPoint in it.dataPoints) {
                                if(dataPoint.dataType == dataType) {
                                    setText(dataType, textView, dataPoint.getValue(field))
                                }
                            }
                    }
                    .addOnFailureListener {
                        Log.e("GOOGLE_FIT", it.localizedMessage ?: "")
                    }
        } else {
            Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                    .readData(readRequest)
                    .addOnSuccessListener {
                        for (data in it.dataSets)
                            for (dataPoint in data.dataPoints) {
                                if (dataPoint.dataType == dataType) {
                                    setText(dataType, textView, dataPoint.getValue(field))
                                }
                            }
                    }
                    .addOnFailureListener {
                        Log.e("GOOGLE_FIT", it.localizedMessage ?: "")
                    }
        }
    }

    fun insertData(context: Context,
                   fitnessOptions: FitnessOptions,
                   dataType: DataType,
                   field: Field,
                   value: Int,
                   startTime: ZonedDateTime,
                   endTime: ZonedDateTime) {

        val dataSource = DataSource.Builder()
                .setAppPackageName(context)
                .setDataType(dataType)
                .setType(DataSource.TYPE_RAW)
                .build()

        setDataPoint(dataType, dataSource, field, value, startTime, endTime)

        val dataSet = DataSet.builder(dataSource).add(dataPoint).build()
        val request = DataUpdateRequest.Builder()
                .setDataSet(dataSet)
                .setTimeInterval(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .updateData(request)
                .addOnSuccessListener {
                    Log.i("GOOGLE_FIT", "DataSet updated")
                }.addOnFailureListener {
                    Log.e("GOOGLE_FIT", "ERROR!")
                }
    }

    private fun setDataPoint(dataType: DataType, dataSource:DataSource, field: Field, value: Int, startTime: ZonedDateTime, endTime: ZonedDateTime) {
        when(dataType){
            DataType.TYPE_HYDRATION -> {
                  dataPoint = DataPoint.builder(dataSource)
                        .setTimeInterval(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                        .setField(field, value.toFloat())
                         .build()
            }
            DataType.TYPE_WEIGHT -> {
                val lbsToKilo = value.toFloat() * 0.45359237
                dataPoint = DataPoint.builder(dataSource)
                        .setTimeInterval(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                        .setField(field, lbsToKilo.toFloat())
                        .build()
            }
            else -> {
                dataPoint = DataPoint.builder(dataSource)
                        .setTimeInterval(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                        .setField(field, value)
                        .build()
            }
        }
    }

    private fun setText(dataType: DataType, textView: TextView, value: Value) {
        when(dataType) {
            DataType.TYPE_HYDRATION -> {
                textView.text = "${value}L"
            }
            DataType.TYPE_WEIGHT -> {
                val kiloToPounds = value.asFloat() * 2.20462
                textView.text = "${ceil(kiloToPounds)}lb"
            }
            DataType.TYPE_STEP_COUNT_DELTA -> {
                textView.text = "$value"
            }
        }
    }
}
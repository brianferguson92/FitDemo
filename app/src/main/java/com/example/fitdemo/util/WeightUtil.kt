package com.example.fitdemo.util

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.DataUpdateRequest
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class WeightUtil {
    private lateinit var googleWeightListener: GoogleFitWeightListener

    interface GoogleFitWeightListener {
        fun getWeight(weight: Float)
    }
    fun readWeight(context: Context, fitnessOptions: FitnessOptions) {
        googleWeightListener = context as GoogleFitWeightListener
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusDays(1)

        val readRequest = DataReadRequest.Builder().read(DataType.TYPE_WEIGHT).setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS).build()
        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .readData(readRequest)
                .addOnSuccessListener { response ->
                    for(dateSet in response.dataSets) {
                        for(dataPoint in dateSet.dataPoints) {
                            if(dataPoint.dataType == DataType.TYPE_WEIGHT) {
                                googleWeightListener.getWeight(dataPoint.getValue(Field.FIELD_WEIGHT).asFloat())
                            }
                        }
                    }
                }.addOnFailureListener {
                    Log.e("GOOGLE_FIT", it.localizedMessage ?: "")
                }
    }

    fun insertWeight(context: Context, fitnessOptions: FitnessOptions, weight: Float) {
        val killogramToPounds = (weight * 2.2046).toFloat()
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())

        val dataSource = DataSource.Builder()
                .setAppPackageName(context)
                .setDataType(DataType.TYPE_WEIGHT)
                .setType(DataSource.TYPE_RAW)
                .build()
        val dataPoint = DataPoint.builder(dataSource)
                .setField(Field.FIELD_WEIGHT, killogramToPounds)
                .setTimestamp(endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build()

        val dataSet = DataSet.builder(dataSource)
                .add(dataPoint)
                .build()
        val request = DataUpdateRequest.Builder()
                .setDataSet(dataSet)
                .setTimeInterval(endTime.minusHours(1).toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build()

        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .updateData(request)
                .addOnSuccessListener {
                    Log.i("GOOGLE_FIT", "WEIGHT INSERTED: ${weight}")
                }
                .addOnFailureListener {
                    Log.e("GOOGLE_FIT", it.localizedMessage ?: "")
                }
    }
}
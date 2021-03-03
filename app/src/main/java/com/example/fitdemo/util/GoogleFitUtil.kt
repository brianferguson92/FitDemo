package com.example.fitdemo.util

import android.content.Context
import android.util.Log
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class GoogleFitUtil {
    fun readData(context: Context, fitnessOptions: FitnessOptions, dataType: DataType, field: Field, textView: TextView) {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusDays(1)

        val readRequest = DataReadRequest.Builder().read(dataType).setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS).build()


        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .readData(readRequest)
                .addOnSuccessListener {
                    for(data in it.dataSets)
                        for(dataPoint in data.dataPoints) {
                            if(dataPoint.dataType == dataType) {
                                textView.text = dataPoint.getValue(field).toString()
                            }
                        }
                }
                .addOnFailureListener {
                    Log.e("GOOGLE_FIT", it.localizedMessage ?: "")
                }
    }
}
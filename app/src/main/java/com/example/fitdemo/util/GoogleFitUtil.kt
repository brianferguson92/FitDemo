package com.example.fitdemo.util

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

class GoogleFitUtil {
    fun readData(context: Context, fitnessOptions: FitnessOptions, dataType: DataType) {
        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .readDailyTotal(dataType)
                .addOnSuccessListener {

                }
    }
}
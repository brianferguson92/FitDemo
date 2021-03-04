package com.example.fitdemo.model

import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field

data class GoogleFitModel(val title: String, val dataType: DataType, val field: Field)
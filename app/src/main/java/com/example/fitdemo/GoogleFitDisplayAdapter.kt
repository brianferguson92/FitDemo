package com.example.fitdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitdemo.model.DataTypeModel
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field

class GoogleFitDisplayAdapter(private val dataTypes: List<DataTypeModel>, private val listener: GoogleFitDisplayListener): RecyclerView.Adapter<GoogleFitDisplayAdapter.ViewHolder>() {
    interface GoogleFitDisplayListener {
        fun onButtonClicked(dataType: DataType, value: EditText)
        fun readData(dataType: DataType, field: Field, textView: TextView)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val button: Button = view.findViewById(R.id.btnInput)
        val count: TextView = view.findViewById(R.id.tvCount)
        val startTime: TextView = view.findViewById(R.id.tvStartTime)
        val endTime: TextView = view.findViewById(R.id.tvEndTime)
        val editText: EditText = view.findViewById(R.id.etInput)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.google_fit_display, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = dataTypes[position].title
        holder.button.text = dataTypes[position].btnText
        listener.readData(dataTypes[position].dataType, dataTypes[position].field, holder.count)
        holder.button.setOnClickListener {
           listener.onButtonClicked(dataTypes[position].dataType, holder.editText)
        }
    }

    override fun getItemCount(): Int {
        return dataTypes.size
    }
}
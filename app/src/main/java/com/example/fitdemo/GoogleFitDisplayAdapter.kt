package com.example.fitdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitdemo.model.GoogleFitModel
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class GoogleFitDisplayAdapter(private val googleFits: List<GoogleFitModel>, private val listener: GoogleFitDisplayListener): RecyclerView.Adapter<GoogleFitDisplayAdapter.ViewHolder>() {
    interface GoogleFitDisplayListener {
        fun onButtonClicked(dataType: DataType, field: Field, value: EditText, startTime: ZonedDateTime, endTime: ZonedDateTime, textView: TextView)
        fun readData(dataType: DataType, field: Field, textView: TextView)
        fun timePicker(textView: TextView)
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
        var eTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        var sTime = eTime.minusHours(1)
        val pattern = "hh:mm:ss a"

        holder.title.text = googleFits[position].title
        holder.button.text = "ADD"
        holder.startTime.text = sTime.format(DateTimeFormatter.ofPattern(pattern))
        holder.endTime.text = eTime.format(DateTimeFormatter.ofPattern(pattern))
        listener.readData(googleFits[position].dataType, googleFits[position].field, holder.count)

        holder.button.setOnClickListener {
           listener.onButtonClicked(googleFits[position].dataType, googleFits[position].field, holder.editText, sTime, eTime, holder.count)
        }

        holder.startTime.setOnClickListener {
            listener.timePicker(holder.startTime)
        }

        holder.endTime.setOnClickListener {
            listener.timePicker(holder.endTime)
        }
    }

    override fun getItemCount(): Int {
        return googleFits.size
    }
}
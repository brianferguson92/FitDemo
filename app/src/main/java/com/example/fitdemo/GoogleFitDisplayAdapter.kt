package com.example.fitdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitdemo.model.DataTypeModel

class GoogleFitDisplayAdapter(private val dataTypes: List<DataTypeModel>): RecyclerView.Adapter<GoogleFitDisplayAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val button: Button = view.findViewById(R.id.btnInput)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.google_fit_display, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = dataTypes[position].title
        holder.button.text = dataTypes[position].btnText
        holder.button.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return dataTypes.size
    }
}
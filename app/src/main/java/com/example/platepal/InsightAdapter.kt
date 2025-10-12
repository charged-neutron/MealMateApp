package com.example.platepal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InsightAdapter(private val insights: List<Insight>) : RecyclerView.Adapter<InsightAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
    ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_insight, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val insight = insights[position]
        holder.nameTextView.text = insight.name
        holder.quantityTextView.text = "Meals: ${insight.quantity}"
    }

    override fun getItemCount() = insights.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.name_textview)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantity_textview)
    }
}

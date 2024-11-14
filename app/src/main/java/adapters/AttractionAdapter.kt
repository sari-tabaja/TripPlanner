package adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import models.AttractionModel
import com.example.hw1.R

class AttractionAdapter(
    private val attractionList: List<AttractionModel>,
    private val onItemClick: (AttractionModel) -> Unit
) : RecyclerView.Adapter<AttractionAdapter.AttractionViewHolder>() {

    inner class AttractionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val attractionName: TextView = view.findViewById(R.id.attraction_name)
        val attractionDate: TextView = view.findViewById(R.id.attraction_date)
        val attractionTime: TextView = view.findViewById(R.id.attraction_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttractionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attraction, parent, false)
        return AttractionViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttractionViewHolder, position: Int) {
        val attraction = attractionList[position]
        holder.attractionName.text = attraction.eventName ?: "No Name"
        holder.attractionDate.text = attraction.startDate ?: "No Date"
        holder.attractionTime.text = attraction.startTime ?: "No Time"

        // Log each binding for debugging
        Log.d("AttractionAdapter", "Binding attraction: ${attraction.eventName}, Date: ${attraction.startDate}, Time: ${attraction.startTime}")

        holder.itemView.setOnClickListener {
            onItemClick(attraction)
        }
    }

    override fun getItemCount(): Int = attractionList.size
}

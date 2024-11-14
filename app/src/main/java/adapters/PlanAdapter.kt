package adapters

import activities.FlightDetailsActivity
import activities.LodgingDetailsActivity
import activities.RestaurantDetailsActivity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import activities.AttractionDetailsActivity
import models.PlanItem
import com.example.hw1.R

class PlanItemAdapter(
    private val planItems: List<PlanItem>
) : RecyclerView.Adapter<PlanItemAdapter.PlanItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plan, parent, false)
        return PlanItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanItemViewHolder, position: Int) {
        val planItem = planItems[position]
        holder.bind(planItem)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            when (planItem.type) {
                "flight" -> context.startActivity(Intent(context, FlightDetailsActivity::class.java).apply {
                    putExtra("flightId", planItem.id)
                })
                "lodging" -> context.startActivity(Intent(context, LodgingDetailsActivity::class.java).apply {
                    putExtra("lodgingId", planItem.id)
                })
                "restaurant" -> context.startActivity(Intent(context, RestaurantDetailsActivity::class.java).apply {
                    putExtra("restaurantId", planItem.id)
                })
                "attraction" -> context.startActivity(Intent(context, AttractionDetailsActivity::class.java).apply {
                    putExtra("attractionId", planItem.id)
                })
            }
        }
    }

    override fun getItemCount() = planItems.size

    class PlanItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.plan_item_name)
        private val dateTextView: TextView = view.findViewById(R.id.plan_item_date)
        private val timeTextView: TextView = view.findViewById(R.id.plan_item_time)
        private val iconImageView: ImageView = view.findViewById(R.id.plan_item_icon)

        fun bind(planItem: PlanItem) {
            nameTextView.text = planItem.name
            dateTextView.text = planItem.date
            timeTextView.text = planItem.time

            // Set the icon based on the type
            val iconResource = when (planItem.type) {
                "flight" -> R.drawable.flight
                "lodging" -> R.drawable.lodging
                "restaurant" -> R.drawable.restaurant
                "attraction" -> R.drawable.activity
                else -> R.drawable.ic_account // Default icon if type is unknown
            }
            iconImageView.setImageResource(iconResource)
        }
    }
}
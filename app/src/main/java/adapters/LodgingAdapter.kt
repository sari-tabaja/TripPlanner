package adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import models.LodgingModel
import com.example.hw1.R

class LodgingAdapter(
    private val lodgingList: List<LodgingModel>,
    private val onItemClick: (LodgingModel) -> Unit
) : RecyclerView.Adapter<LodgingAdapter.LodgingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LodgingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lodging, parent, false)
        return LodgingViewHolder(view)
    }

    override fun onBindViewHolder(holder: LodgingViewHolder, position: Int) {
        val lodging = lodgingList[position]
        holder.bind(lodging)
        holder.itemView.setOnClickListener { onItemClick(lodging) }
    }

    override fun getItemCount(): Int = lodgingList.size

    // ViewHolder for displaying a single lodging card with check-in and check-out details
    inner class LodgingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lodgingName: TextView = itemView.findViewById(R.id.lodging_name)
        private val checkInDate: TextView = itemView.findViewById(R.id.check_in_date)
        private val checkOutDate: TextView = itemView.findViewById(R.id.check_out_date)
        private val address: TextView = itemView.findViewById(R.id.address)

        fun bind(lodging: LodgingModel) {
            lodgingName.text = lodging.lodgingName
            checkInDate.text = "Check-in: ${lodging.checkInDate} ${lodging.checkInTime}"
            checkOutDate.text = "Check-out: ${lodging.checkOutDate} ${lodging.checkOutTime}"
            address.text = lodging.address
        }
    }
}
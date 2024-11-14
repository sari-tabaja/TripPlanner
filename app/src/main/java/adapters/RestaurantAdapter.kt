package adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hw1.R
import models.RestaurantModel

class RestaurantAdapter(
    private val restaurantList: List<RestaurantModel>,
    private val onItemClick: (RestaurantModel) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val restaurantName: TextView = view.findViewById(R.id.restaurant_name)
        val restaurantDate: TextView = view.findViewById(R.id.restaurant_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurantList[position]
        holder.restaurantName.text = restaurant.restaurantName
        holder.restaurantDate.text = restaurant.date
        holder.itemView.setOnClickListener { onItemClick(restaurant) }
    }

    override fun getItemCount(): Int = restaurantList.size
}
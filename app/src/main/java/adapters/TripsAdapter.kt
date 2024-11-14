package adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hw1.R
import activities.TripDetailsActivity
import models.TripModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TripsAdapter(private val tripsList: List<TripModel>) : RecyclerView.Adapter<TripsAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trip_item, parent, false)
        return TripViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val currentTrip = tripsList[position]

        holder.tripTitle.text = currentTrip.tripName
        holder.tripDate.text = "${currentTrip.startDate} - ${currentTrip.endDate}"
        holder.tripDetails.text = currentTrip.details

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Check if the trip is a favorite and set the icon accordingly
        val db = FirebaseFirestore.getInstance()
        val favoriteRef = db.collection("users").document(userId).collection("favorites").document(currentTrip.tripId)

        favoriteRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                holder.favoriteIcon.setImageResource(R.drawable.ic_favorite) // Filled icon for favorite
            } else {
                holder.favoriteIcon.setImageResource(R.drawable.ic_favorite_border) // Outline icon for not favorite
            }
        }

        // Toggle favorite status on icon click
        holder.favoriteIcon.setOnClickListener {
            favoriteRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Remove from favorites
                    favoriteRef.delete().addOnSuccessListener {
                        holder.favoriteIcon.setImageResource(R.drawable.ic_favorite_border)
                        Toast.makeText(holder.itemView.context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Add to favorites
                    favoriteRef.set(currentTrip).addOnSuccessListener {
                        holder.favoriteIcon.setImageResource(R.drawable.ic_favorite)
                        Toast.makeText(holder.itemView.context, "Added to favorites", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, TripDetailsActivity::class.java)
            intent.putExtra("tripId", currentTrip.tripId)
            intent.putExtra("tripName", currentTrip.tripName)  // Ensure this is set
            intent.putExtra("tripDate", "${currentTrip.startDate} - ${currentTrip.endDate}") // Ensure this is set
            holder.itemView.context.startActivity(intent)
        }
    }



    override fun getItemCount(): Int {
        return tripsList.size
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tripImage: ImageView = itemView.findViewById(R.id.trip_image)
        val tripTitle: TextView = itemView.findViewById(R.id.trip_title)
        val tripDate: TextView = itemView.findViewById(R.id.trip_date)
        val tripDetails: TextView = itemView.findViewById(R.id.trip_details)
        val favoriteIcon: ImageButton = itemView.findViewById(R.id.favoriteIcon)  // New favorite icon
    }
}
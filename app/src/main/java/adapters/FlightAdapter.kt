package adapters

import activities.FlightDetailsActivity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import models.FlightModel
import com.example.hw1.R

class FlightAdapter(private val flightList: List<FlightModel>) : RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    class FlightViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val flightDate: TextView = view.findViewById(R.id.flight_date)
        val departureTime: TextView = view.findViewById(R.id.departure_time)
        val route: TextView = view.findViewById(R.id.departure_to_arrival)
        val flightNumber: TextView = view.findViewById(R.id.flight_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flight, parent, false)
        return FlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val flight = flightList[position]

        // Set the data for the flight card
        holder.flightDate.text = flight.flightDate
        holder.departureTime.text = flight.departureTime
        holder.route.text = "${flight.departureAirport} – ${flight.arrivalAirport}"
        holder.flightNumber.text = "${flight.flightNumber} (${flight.airline})"

        // Add click listener to open FlightDetailsActivity
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, FlightDetailsActivity::class.java)
            intent.putExtra("flightId", flight.flightId) // Ensure this is the correct flightId
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = flightList.size
}

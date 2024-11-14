package activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hw1.R
import com.google.firebase.firestore.FirebaseFirestore

class FlightDetailsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var documentUrl: String? = null  // To store the document URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_details)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Reference to essential UI elements
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val flightRoute = findViewById<TextView>(R.id.flight_route)
        val flightInfo = findViewById<TextView>(R.id.flight_info)
        val departureTime = findViewById<TextView>(R.id.departure_time)
        val arrivalTime = findViewById<TextView>(R.id.arrival_time)
        val airline = findViewById<TextView>(R.id.airline)
        val flightNumber = findViewById<TextView>(R.id.flight_number)
        val departureAirport = findViewById<TextView>(R.id.departure_airport)
        val arrivalAirport = findViewById<TextView>(R.id.arrival_airport)
        val documentIcon = findViewById<ImageView>(R.id.document_icon)  // Assuming `document_icon` is your ImageView for the document

        // Retrieve flightId from intent
        val flightId = intent.getStringExtra("flightId")

        if (flightId != null) {
            loadFlightDetails(
                flightId, flightRoute, flightInfo,
                departureTime, arrivalTime, airline,
                flightNumber, departureAirport, arrivalAirport
            )
        } else {
            Toast.makeText(this, "No flight ID provided", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set click listener for document icon
        documentIcon.setOnClickListener {
            documentUrl?.let { url ->
                openDocument(url)
            } ?: Toast.makeText(this, "No document available", Toast.LENGTH_SHORT).show()
        }

        // Back button action
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadFlightDetails(
        flightId: String,
        flightRoute: TextView,
        flightInfo: TextView,
        departureTime: TextView,
        arrivalTime: TextView,
        airline: TextView,
        flightNumber: TextView,
        departureAirport: TextView,
        arrivalAirport: TextView
    ) {
        db.collection("flights").document(flightId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    flightRoute.text = "${document.getString("departureAirport") ?: "Unknown"} ➔ ${document.getString("arrivalAirport") ?: "Unknown"}"
                    flightInfo.text = "${document.getString("flightNumber") ?: "N/A"} (${document.getString("airline") ?: "Unknown Airline"})"
                    departureTime.text = document.getString("departureTime") ?: "N/A"
                    arrivalTime.text = document.getString("arrivalTime") ?: "N/A"
                    airline.text = document.getString("airline") ?: "Unknown Airline"
                    flightNumber.text = document.getString("flightNumber") ?: "N/A"
                    departureAirport.text = document.getString("departureAirport") ?: "Unknown"
                    arrivalAirport.text = document.getString("arrivalAirport") ?: "Unknown"

                    // Get document URL and store it
                    documentUrl = document.getString("imageUrl")
                } else {
                    Toast.makeText(this, "Flight details not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load flight details: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun openDocument(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}

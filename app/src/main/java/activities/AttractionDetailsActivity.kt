package activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hw1.R
import com.google.firebase.firestore.FirebaseFirestore

class AttractionDetailsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var attractionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attraction_details)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Get attractionId from intent
        attractionId = intent.getStringExtra("attractionId")

        if (attractionId != null) {
            loadAttractionDetails(attractionId!!)
        } else {
            Toast.makeText(this, "No attraction ID provided", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set up back button
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun loadAttractionDetails(attractionId: String) {
        db.collection("attractions").document(attractionId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    findViewById<TextView>(R.id.attraction_name).text = document.getString("eventName") ?: "N/A"
                    findViewById<TextView>(R.id.start_date).text = document.getString("startDate") ?: "N/A"
                    findViewById<TextView>(R.id.start_time).text = document.getString("startTime") ?: "N/A"
                    findViewById<TextView>(R.id.end_date).text = document.getString("endDate") ?: "N/A"
                    findViewById<TextView>(R.id.end_time).text = document.getString("endTime") ?: "N/A"
                    findViewById<TextView>(R.id.venue).text = document.getString("venue") ?: "N/A"
                    findViewById<TextView>(R.id.address).text = document.getString("address") ?: "N/A"
                    findViewById<TextView>(R.id.phone).text = document.getString("phone") ?: "N/A"
                    findViewById<TextView>(R.id.website).text = document.getString("website") ?: "N/A"
                    findViewById<TextView>(R.id.email).text = document.getString("email") ?: "N/A"
                    findViewById<TextView>(R.id.confirmation).text = document.getString("confirmation") ?: "N/A"
                } else {
                    Toast.makeText(this, "Attraction details not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load attraction details: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

}


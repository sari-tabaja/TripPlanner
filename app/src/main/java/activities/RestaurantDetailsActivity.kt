package activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hw1.R
import com.google.firebase.firestore.FirebaseFirestore

class RestaurantDetailsActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var websiteTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var confirmationTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_details)

        // Initialize and set click listener for the back button
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Close this activity and go back to the previous screen
        }

        // Initialize views by finding them in the layout
        nameTextView = findViewById(R.id.restaurant_name)
        dateTextView = findViewById(R.id.restaurant_date)
        timeTextView = findViewById(R.id.restaurant_time)
        addressTextView = findViewById(R.id.restaurant_address)
        phoneTextView = findViewById(R.id.restaurant_phone)
        websiteTextView = findViewById(R.id.restaurant_website)
        emailTextView = findViewById(R.id.restaurant_email)
        confirmationTextView = findViewById(R.id.restaurant_confirmation)

        loadRestaurantDetails()
    }

    private fun loadRestaurantDetails() {
        val restaurantId = intent.getStringExtra("restaurantId") ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("restaurants").document(restaurantId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    nameTextView.text = document.getString("restaurantName") ?: "Unknown Restaurant"
                    dateTextView.text = document.getString("date")
                    timeTextView.text = document.getString("time")
                    addressTextView.text = document.getString("address")
                    phoneTextView.text = document.getString("phone")
                    websiteTextView.text = document.getString("website")
                    emailTextView.text = document.getString("email")
                    confirmationTextView.text = document.getString("confirmation")
                } else {
                    Toast.makeText(this, "Restaurant details not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load restaurant details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

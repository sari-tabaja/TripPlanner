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

    class LodgingDetailsActivity : AppCompatActivity() {

        private lateinit var db: FirebaseFirestore
        private var documentUrl: String? = null  // To store the document URL

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_lodging_details)

            // Initialize Firestore
            db = FirebaseFirestore.getInstance()

            // Reference to essential UI elements
            val backButton = findViewById<ImageButton>(R.id.backButton)
            val lodgingAbbreviation = findViewById<TextView>(R.id.lodging_abbreviation)
            val lodgingName = findViewById<TextView>(R.id.lodging_name)
            val checkInDate = findViewById<TextView>(R.id.check_in_date)
            val checkOutDate = findViewById<TextView>(R.id.check_out_date)
            val address = findViewById<TextView>(R.id.address)
            val documentIcon = findViewById<ImageView>(R.id.document_icon)

            // Retrieve lodgingId from intent
            val lodgingId = intent.getStringExtra("lodgingId")

            if (lodgingId != null) {
                loadLodgingDetails(
                    lodgingId, lodgingAbbreviation, lodgingName, checkInDate, checkOutDate, address
                )
            } else {
                Toast.makeText(this, "No lodging ID provided", Toast.LENGTH_SHORT).show()
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

        private fun loadLodgingDetails(
            lodgingId: String,
            lodgingAbbreviation: TextView,
            lodgingName: TextView,
            checkInDate: TextView,
            checkOutDate: TextView,
            address: TextView
        ) {
            db.collection("lodgings").document(lodgingId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullLodgingName = document.getString("lodgingName") ?: "Unknown Lodging"
                        val abbreviation = createAbbreviation(fullLodgingName)

                        // Set the abbreviation and full lodging name
                        lodgingAbbreviation.text = abbreviation
                        lodgingName.text = fullLodgingName

                        checkInDate.text = document.getString("checkInDate") ?: "N/A"
                        checkOutDate.text = document.getString("checkOutDate") ?: "N/A"
                        address.text = document.getString("address") ?: "Unknown Address"

                        // Retrieve document URL
                        documentUrl = document.getString("documentUrl")

                        if (documentUrl == null) {
                            Toast.makeText(this, "No document available", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Lodging details not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load lodging details: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }

        // Function to create an abbreviation from the full lodging name
        private fun createAbbreviation(fullName: String): String {
            return fullName.split(" ").take(3).joinToString(" ")
        }

        private fun openDocument(url: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
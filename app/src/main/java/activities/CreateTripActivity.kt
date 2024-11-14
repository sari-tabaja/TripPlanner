package activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hw1.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateTripActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var placesClient: PlacesClient
    private lateinit var countryAutoCompleteTextView: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_trip)

        // Initialize Firebase Auth and Places API
        auth = FirebaseAuth.getInstance()
        Places.initialize(applicationContext, "AIzaSyC2Z7zOR5C51gcchPTkZRtuPoirkh3_MYo")
        placesClient = Places.createClient(this)

        // Back button functionality
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()  // Finish activity to go back to previous screen
        }

        val tripNameEditText = findViewById<EditText>(R.id.trip_name)
        countryAutoCompleteTextView = findViewById(R.id.destination_city) // Using AutoCompleteTextView
        val startDateEditText = findViewById<EditText>(R.id.start_date)
        val endDateEditText = findViewById<EditText>(R.id.end_date)
        val descriptionEditText = findViewById<EditText>(R.id.description)
        val saveButton = findViewById<Button>(R.id.buttonSaveTrip)

        // Setup Google Places AutoComplete
        setupAutoComplete()

        // Set date pickers for start and end dates
        startDateEditText.setOnClickListener {
            showDatePickerDialog(startDateEditText)
        }

        endDateEditText.setOnClickListener {
            showDatePickerDialog(endDateEditText)
        }

        // Set click listener for save button
        saveButton.setOnClickListener {
            val tripName = tripNameEditText.text.toString()
            val destinationCity = countryAutoCompleteTextView.text.toString()
            val startDate = startDateEditText.text.toString()
            val endDate = endDateEditText.text.toString()
            val description = descriptionEditText.text.toString()

            if (tripName.isEmpty() || destinationCity.isEmpty()) {
                Toast.makeText(this, "Trip name and destination city are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get the current user
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val tripRef = db.collection("trips").document()
                val trip = hashMapOf(
                    "tripId" to tripRef.id,
                    "tripName" to tripName,
                    "destinationCity" to destinationCity,
                    "startDate" to startDate,
                    "endDate" to endDate,
                    "description" to description,
                    "uid" to currentUser.uid,
                    "sharedWith" to arrayListOf<String>() // Initialize with an empty list
                )


                tripRef.set(trip)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Trip saved successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to save trip: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User is not signed in!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Set up the AutoCompleteTextView for country suggestions
    private fun setupAutoComplete() {
        countryAutoCompleteTextView.threshold = 1 // Start suggesting after 1 character
        countryAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val suggestion = parent.getItemAtPosition(position).toString()
            countryAutoCompleteTextView.setText(suggestion)
        }

        countryAutoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    fetchPredictions(s.toString())
                }
            }
        })
    }

    // Fetch autocomplete predictions for the entered text
    private fun fetchPredictions(query: String) {
        val token = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val suggestions = response.autocompletePredictions.map { prediction ->
                    prediction.getFullText(null).toString()
                }
                val adapter = android.widget.ArrayAdapter(
                    this, android.R.layout.simple_dropdown_item_1line, suggestions
                )
                countryAutoCompleteTextView.setAdapter(adapter)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching predictions: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Show Date Picker for the given EditText
    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                editText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}

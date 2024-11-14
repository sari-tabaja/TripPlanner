package activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.hw1.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddRestaurantActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var placesClient: PlacesClient

    private lateinit var restaurantNameEditText: AutoCompleteTextView
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var websiteEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var confirmationEditText: EditText
    private lateinit var saveRestaurantButton: Button
    private var tripId: String? = null  // Add variable to hold the tripId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_restaurant)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize Google Places API
        Places.initialize(applicationContext, "AIzaSyC2Z7zOR5C51gcchPTkZRtuPoirkh3_MYo")
        placesClient = Places.createClient(this)

        // Get the tripId from the Intent
        tripId = intent.getStringExtra("tripId")

        // Reference UI elements
        restaurantNameEditText = findViewById(R.id.restaurantNameEditText)
        dateEditText = findViewById(R.id.dateEditText)
        timeEditText = findViewById(R.id.timeEditText)
        addressEditText = findViewById(R.id.addressEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        websiteEditText = findViewById(R.id.websiteEditText)
        emailEditText = findViewById(R.id.emailEditText)
        confirmationEditText = findViewById(R.id.confirmationEditText)
        saveRestaurantButton = findViewById(R.id.saveRestaurantButton)

        // Setup autocomplete for restaurant name
        setupRestaurantNameAutocomplete()

        // Setup date and time pickers
        setupDatePicker(dateEditText)
        setupTimePicker(timeEditText)

        // Back button
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Save restaurant data
        saveRestaurantButton.setOnClickListener {
            saveRestaurantToFirebase()
        }
    }

    private fun setupRestaurantNameAutocomplete() {
        restaurantNameEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    val token = AutocompleteSessionToken.newInstance()
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build()

                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            val suggestions = response.autocompletePredictions.map { it.getFullText(null).toString() }
                            val adapter = ArrayAdapter(this@AddRestaurantActivity, android.R.layout.simple_dropdown_item_1line, suggestions)
                            restaurantNameEditText.setAdapter(adapter)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@AddRestaurantActivity, "Error fetching restaurant suggestions: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        })
    }

    private fun setupDatePicker(editText: EditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                editText.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay))
            }, year, month, day)
            datePickerDialog.show()
        }
    }

    private fun setupTimePicker(editText: EditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                editText.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            }, hour, minute, true)
            timePickerDialog.show()
        }
    }

    private fun saveRestaurantToFirebase() {
        val userId = auth.currentUser?.uid ?: return
        if (tripId == null) {
            Toast.makeText(this, "Error: tripId is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val restaurant = hashMapOf(
            "restaurantName" to restaurantNameEditText.text.toString(),
            "date" to dateEditText.text.toString(),
            "time" to timeEditText.text.toString(),
            "address" to addressEditText.text.toString(),
            "phone" to phoneEditText.text.toString(),
            "website" to websiteEditText.text.toString(),
            "email" to emailEditText.text.toString(),
            "confirmation" to confirmationEditText.text.toString(),
            "userId" to userId,
            "tripId" to tripId  // Add tripId to the restaurant data
        )

        db.collection("restaurants")
            .add(restaurant)
            .addOnSuccessListener {
                Toast.makeText(this, "Restaurant saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save restaurant: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

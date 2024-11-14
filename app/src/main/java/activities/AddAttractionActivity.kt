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

class AddAttractionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var placesClient: PlacesClient

    private lateinit var eventNameEditText: EditText
    private lateinit var startDateEditText: EditText
    private lateinit var startTimeEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var endTimeEditText: EditText
    private lateinit var venueEditText: AutoCompleteTextView
    private lateinit var addressEditText: AutoCompleteTextView
    private lateinit var phoneEditText: EditText
    private lateinit var websiteEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var confirmationEditText: EditText
    private lateinit var saveAttractionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_attraction)

        // Initialize Firebase Auth, Firestore, and Places API
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize Google Places with your API key
        Places.initialize(applicationContext, "AIzaSyC2Z7zOR5C51gcchPTkZRtuPoirkh3_MYo")
        placesClient = Places.createClient(this)

        // Reference UI elements
        eventNameEditText = findViewById(R.id.eventNameEditText)
        startDateEditText = findViewById(R.id.startDateEditText)
        startTimeEditText = findViewById(R.id.startTimeEditText)
        endDateEditText = findViewById(R.id.endDateEditText)
        endTimeEditText = findViewById(R.id.endTimeEditText)
        venueEditText = findViewById(R.id.venueEditText)
        addressEditText = findViewById(R.id.addressEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        websiteEditText = findViewById(R.id.websiteEditText)
        emailEditText = findViewById(R.id.emailEditText)
        confirmationEditText = findViewById(R.id.confirmationEditText)
        saveAttractionButton = findViewById(R.id.saveAttractionButton)

        // Setup date and time pickers
        setupDatePicker(startDateEditText)
        setupDatePicker(endDateEditText)
        setupTimePicker(startTimeEditText)
        setupTimePicker(endTimeEditText)

        // Set up autocomplete for venue and address
        setupAutocomplete(venueEditText)
        setupAutocomplete(addressEditText)

        // Back button
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Save attraction data
        saveAttractionButton.setOnClickListener {
            saveAttractionToFirebase()
        }
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

    private fun setupAutocomplete(autoCompleteTextView: AutoCompleteTextView) {
        autoCompleteTextView.threshold = 1
        autoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    fetchPredictions(s.toString(), autoCompleteTextView)
                }
            }
        })
    }

    private fun fetchPredictions(query: String, autoCompleteTextView: AutoCompleteTextView) {
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val suggestions = response.autocompletePredictions.map { it.getFullText(null).toString() }
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
                autoCompleteTextView.setAdapter(adapter)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching predictions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveAttractionToFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val tripId = intent.getStringExtra("tripId") ?: return

        val attraction = hashMapOf(
            "eventName" to eventNameEditText.text.toString(),
            "startDate" to startDateEditText.text.toString(),
            "startTime" to startTimeEditText.text.toString(),
            "endDate" to endDateEditText.text.toString(),
            "endTime" to endTimeEditText.text.toString(),
            "venue" to venueEditText.text.toString(),
            "address" to addressEditText.text.toString(),
            "phone" to phoneEditText.text.toString(),
            "website" to websiteEditText.text.toString(),
            "email" to emailEditText.text.toString(),
            "confirmation" to confirmationEditText.text.toString(),
            "userId" to userId,
            "tripId" to tripId
        )

        db.collection("attractions")
            .add(attraction)
            .addOnSuccessListener {
                Toast.makeText(this, "Attraction saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save attraction: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

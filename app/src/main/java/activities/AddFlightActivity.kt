package activities

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hw1.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class AddFlightActivity : AppCompatActivity() {

    private lateinit var flightDateEditText: EditText
    private lateinit var departureTimeEditText: EditText
    private lateinit var departureAirportEditText: AutoCompleteTextView
    private lateinit var arrivalTimeEditText: EditText
    private lateinit var arrivalAirportEditText: AutoCompleteTextView
    private lateinit var airlineEditText: EditText
    private lateinit var flightNumberEditText: EditText
    private lateinit var saveFlightButton: Button
    private lateinit var flightImageView: ImageView
    private lateinit var uploadImageButton: Button

    private var imageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var placesClient: PlacesClient

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_flight)

        // Initialize Firebase Auth, Firestore, and Storage
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference.child("flight_images")
        Places.initialize(applicationContext, "AIzaSyC2Z7zOR5C51gcchPTkZRtuPoirkh3_MYo")
        placesClient = Places.createClient(this)

        // Retrieve tripId from intent
        val tripId = intent.getStringExtra("tripId")

        // Link UI elements
        flightDateEditText = findViewById(R.id.flightDateEditText)
        departureTimeEditText = findViewById(R.id.departureTimeEditText)
        departureAirportEditText = findViewById(R.id.departureAirportEditText)
        arrivalTimeEditText = findViewById(R.id.arrivalTimeEditText)
        arrivalAirportEditText = findViewById(R.id.arrivalAirportEditText)
        airlineEditText = findViewById(R.id.airlineEditText)
        flightNumberEditText = findViewById(R.id.flightNumberEditText)
        saveFlightButton = findViewById(R.id.saveFlightButton)
        flightImageView = findViewById(R.id.flightImageView)
        uploadImageButton = findViewById(R.id.uploadImageButton)

        // Set up listeners
        setupDatePicker(flightDateEditText)
        setupTimePicker(departureTimeEditText)
        setupTimePicker(arrivalTimeEditText)
        setupAutoComplete(departureAirportEditText)
        setupAutoComplete(arrivalAirportEditText)

        uploadImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        saveFlightButton.setOnClickListener {
            saveFlightToFirebase(tripId)
        }
    }

    private fun setupDatePicker(editText: EditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                editText.setText(selectedDate)
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

    private fun setupAutoComplete(autoCompleteTextView: AutoCompleteTextView) {
        autoCompleteTextView.threshold = 1
        autoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
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
                val adapter = android.widget.ArrayAdapter(
                    this, android.R.layout.simple_dropdown_item_1line, suggestions
                )
                autoCompleteTextView.setAdapter(adapter)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching predictions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            Glide.with(this).load(imageUri).into(flightImageView)
        }
    }

    private fun saveFlightToFirebase(tripId: String?) {
        val userId = auth.currentUser?.uid ?: return
        if (imageUri != null) {
            val imageRef = storageReference.child("flights/${UUID.randomUUID()}")
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveFlightData(tripId, userId, uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveFlightData(tripId, userId, null)
        }
    }

    private fun saveFlightData(tripId: String?, userId: String, imageUrl: String?) {
        val flight = hashMapOf(
            "flightDate" to flightDateEditText.text.toString(),
            "departureTime" to departureTimeEditText.text.toString(),
            "departureAirport" to departureAirportEditText.text.toString(),
            "arrivalTime" to arrivalTimeEditText.text.toString(),
            "arrivalAirport" to arrivalAirportEditText.text.toString(),
            "airline" to airlineEditText.text.toString(),
            "flightNumber" to flightNumberEditText.text.toString(),
            "userId" to userId,
            "tripId" to (tripId ?: ""),
            "imageUrl" to imageUrl
        )

        db.collection("flights")
            .add(flight)
            .addOnSuccessListener { documentReference ->
                Log.d("AddFlightActivity", "Flight saved with ID: ${documentReference.id}")
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("AddFlightActivity", "Failed to save flight", e)
            }
    }
}

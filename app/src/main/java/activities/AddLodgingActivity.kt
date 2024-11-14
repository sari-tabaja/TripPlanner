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

class AddLodgingActivity : AppCompatActivity() {

    private lateinit var checkInDateEditText: EditText
    private lateinit var checkOutDateEditText: EditText
    private lateinit var checkInTimeEditText: EditText
    private lateinit var checkOutTimeEditText: EditText
    private lateinit var lodgingNameEditText: AutoCompleteTextView
    private lateinit var addressEditText: AutoCompleteTextView
    private lateinit var saveLodgingButton: Button
    private lateinit var lodgingImageView: ImageView
    private lateinit var uploadImageButton: Button

    private var imageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var placesClient: PlacesClient

    companion object {
        private const val REQUEST_IMAGE_PICK = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_lodging)

        // Initialize Firebase Auth, Firestore, and Storage
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference.child("lodging_images")
        Places.initialize(applicationContext, "AIzaSyC2Z7zOR5C51gcchPTkZRtuPoirkh3_MYo")
        placesClient = Places.createClient(this)

        // Retrieve tripId from intent
        val tripId = intent.getStringExtra("tripId")

        // Link UI elements
        checkInDateEditText = findViewById(R.id.checkInDate)
        checkOutDateEditText = findViewById(R.id.checkOutDate)
        checkInTimeEditText = findViewById(R.id.checkInTime)
        checkOutTimeEditText = findViewById(R.id.checkOutTime)
        lodgingNameEditText = findViewById(R.id.lodgingName)
        addressEditText = findViewById(R.id.address)
        saveLodgingButton = findViewById(R.id.saveLodgingButton)
        lodgingImageView = findViewById(R.id.lodgingImageView)
        uploadImageButton = findViewById(R.id.uploadImageButton)

        // Set up listeners
        setupDatePicker(checkInDateEditText)
        setupDatePicker(checkOutDateEditText)
        setupTimePicker(checkInTimeEditText)
        setupTimePicker(checkOutTimeEditText)
        setupAutoComplete(lodgingNameEditText)
        setupAutoComplete(addressEditText)

        uploadImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        saveLodgingButton.setOnClickListener {
            saveLodgingToFirebase(tripId)
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
            Glide.with(this).load(imageUri).into(lodgingImageView)
        }
    }

    private fun saveLodgingToFirebase(tripId: String?) {
        val userId = auth.currentUser?.uid ?: return
        if (imageUri != null) {
            val imageRef = storageReference.child("lodgings/${UUID.randomUUID()}")
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveLodgingData(tripId, userId, uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveLodgingData(tripId, userId, null)
        }
    }

    private fun saveLodgingData(tripId: String?, userId: String, imageUrl: String?) {
        val lodging = hashMapOf(
            "checkInDate" to checkInDateEditText.text.toString(),
            "checkOutDate" to checkOutDateEditText.text.toString(),
            "checkInTime" to checkInTimeEditText.text.toString(),
            "checkOutTime" to checkOutTimeEditText.text.toString(),
            "lodgingName" to lodgingNameEditText.text.toString(),
            "address" to addressEditText.text.toString(),
            "userId" to userId,
            "tripId" to (tripId ?: ""),
            "documentUrl" to imageUrl  // Make sure this field name matches in both AddLodgingActivity and LodgingDetailsActivity
        )

        db.collection("lodgings")
            .add(lodging)
            .addOnSuccessListener { documentReference ->
                Log.d("AddLodgingActivity", "Lodging saved with ID: ${documentReference.id}")
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("AddLodgingActivity", "Failed to save lodging", e)
            }
    }
}


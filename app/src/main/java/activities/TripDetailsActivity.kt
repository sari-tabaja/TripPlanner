package activities

import adapters.PlanItemAdapter
import adapters.SharedPeopleAdapter
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hw1.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import models.AttractionModel
import models.FlightModel
import models.LodgingModel
import models.PlanItem
import models.RestaurantModel
import java.text.SimpleDateFormat
import java.util.Locale

class TripDetailsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var planItemAdapter: PlanItemAdapter
    private val planItems = mutableListOf<PlanItem>()

    private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Assume this is the Firestore date format
    private val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val inputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val outputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details)

        // Get trip information from intent
        val tripId = intent.getStringExtra("tripId")
        val tripName = intent.getStringExtra("tripName")
        val tripDate = intent.getStringExtra("tripDate")

        // Reference views
        val tripTitleTextView = findViewById<TextView>(R.id.trip_title)
        val tripDateTextView = findViewById<TextView>(R.id.trip_date)
        val addPlanButton = findViewById<Button>(R.id.addPlanButton)
        val fabAddPlan = findViewById<ImageButton>(R.id.fab_add_plan)
        val noPlansMessage = findViewById<TextView>(R.id.no_plans_message)
        val planItemsRecyclerView = findViewById<RecyclerView>(R.id.recycler_view_plan_items)
        val shareButton = findViewById<Button>(R.id.buttonShareTrip)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Set up the back button
        backButton.setOnClickListener { finish() }

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set up RecyclerView and Adapter for combined plans
        planItemAdapter = PlanItemAdapter(planItems)
        planItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        planItemsRecyclerView.adapter = planItemAdapter

        // Set trip details to the UI elements
        tripTitleTextView.text = tripName ?: "No Title"
        tripDateTextView.text = tripDate ?: "No Date"

        // Fetch and display all plans
        fetchPlans(tripId, noPlansMessage, addPlanButton, fabAddPlan)

        // Set up add plan button and FAB to show the dialog for choosing plan type
        addPlanButton.setOnClickListener { showAddPlanDialog() }
        fabAddPlan.setOnClickListener { showAddPlanDialog() }

        // Set up share button
        shareButton.setOnClickListener { tripId?.let { showShareTripDialog(it) } }
    }

    private fun formatDate(date: String): String {
        return try {
            val parsedDate = inputDateFormat.parse(date)
            outputDateFormat.format(parsedDate)
        } catch (e: Exception) {
            date
        }
    }

    private fun formatTime(time: String): String {
        return try {
            val parsedTime = inputTimeFormat.parse(time)
            outputTimeFormat.format(parsedTime)
        } catch (e: Exception) {
            time
        }
    }

    private fun showShareTripDialog(tripId: String) {
        val dialogView = layoutInflater.inflate(R.layout.share_trip_dialog, null)
        val emailInput = dialogView.findViewById<EditText>(R.id.email_input)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recycler_view_shared_people)

        val sharedPeopleEmails = mutableListOf<String>()
        val adapter = SharedPeopleAdapter(sharedPeopleEmails)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        db.collection("trips").document(tripId).get()
            .addOnSuccessListener { document ->
                val sharedWith = document.get("sharedWith") as? List<String> ?: return@addOnSuccessListener
                sharedPeopleEmails.clear()
                sharedWith.forEach { userId ->
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            userDoc.getString("email")?.let { email ->
                                sharedPeopleEmails.add(email)
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }

        AlertDialog.Builder(this)
            .setTitle("Share Trip")
            .setView(dialogView)
            .setPositiveButton("Share") { _, _ ->
                val email = emailInput.text.toString().trim()
                if (email.isNotEmpty()) {
                    shareTripWithUser(tripId, email) { success ->
                        if (success) {
                            sharedPeopleEmails.add(email)
                            adapter.notifyDataSetChanged()
                            Toast.makeText(this, "Trip shared with $email", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to share trip with $email", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    private fun shareTripWithUser(tripId: String, email: String, callback: (Boolean) -> Unit) {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(false)
                    return@addOnSuccessListener
                }
                val userId = documents.first().id
                db.collection("trips").document(tripId)
                    .update("sharedWith", FieldValue.arrayUnion(userId))
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
            }
            .addOnFailureListener { callback(false) }
    }

    private fun showAddPlanDialog() {
        val options = arrayOf("Add Flight", "Add Lodging", "Add Restaurant", "Add Attraction")
        val icons = arrayOf(
            R.drawable.flight, R.drawable.lodging, R.drawable.restaurant, R.drawable.activity
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Plan Type")
        val adapter = object : ArrayAdapter<String>(this, R.layout.dialog_item_with_icon, options) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.dialog_item_with_icon, parent, false)
                view.findViewById<ImageView>(R.id.icon).setImageResource(icons[position])
                view.findViewById<TextView>(R.id.title).text = options[position]
                return view
            }
        }
        builder.setAdapter(adapter) { _, which ->
            when (which) {
                0 -> openAddFlightActivity()
                1 -> openAddLodgingActivity()
                2 -> openAddRestaurantActivity()
                3 -> openAddAttractionActivity()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun fetchPlans(tripId: String?, noPlansMessage: TextView, addPlanButton: Button, fabAddPlan: ImageButton) {
        planItems.clear()

        if (tripId != null) {
            val fetchTasks = listOf(
                db.collection("flights").whereEqualTo("tripId", tripId).get().addOnSuccessListener { result ->
                    for (document in result) {
                        val flight = document.toObject(FlightModel::class.java)
                        planItems.add(
                            PlanItem(
                                id = document.id,
                                name = flight.flightNumber,
                                date = formatDate(flight.flightDate),
                                time = formatTime(flight.departureTime),
                                type = "flight"
                            )
                        )
                    }
                },
                db.collection("lodgings").whereEqualTo("tripId", tripId).get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val lodging = document.toObject(LodgingModel::class.java)
                            planItems.add(
                                PlanItem(
                                    id = document.id,
                                    type = "lodging",
                                    name = "${lodging.lodgingName} (Check-in)",
                                    date = lodging.checkInDate,  // Use check-in date for sorting
                                    time = lodging.checkInTime
                                )
                            )

                        // Add check-out plan item
                        planItems.add(
                            PlanItem(
                                id = document.id,
                                name = "${lodging.lodgingName} (Check-out)",
                                date = formatDate(lodging.checkOutDate),
                                time = formatTime(lodging.checkOutTime),
                                type = "lodging",
                                isCheckIn = false
                            )
                        )
                    }
                },
                db.collection("restaurants").whereEqualTo("tripId", tripId).get().addOnSuccessListener { result ->
                    for (document in result) {
                        val restaurant = document.toObject(RestaurantModel::class.java)
                        planItems.add(
                            PlanItem(
                                id = document.id,
                                name = restaurant.restaurantName,
                                date = formatDate(restaurant.date),
                                time = formatTime(restaurant.time),
                                type = "restaurant"
                            )
                        )
                    }
                },
                db.collection("attractions").whereEqualTo("tripId", tripId).get().addOnSuccessListener { result ->
                    for (document in result) {
                        val attraction = document.toObject(AttractionModel::class.java)
                        planItems.add(
                            PlanItem(
                                id = document.id,
                                name = attraction.eventName,
                                date = formatDate(attraction.startDate),
                                time = formatTime(attraction.startTime),
                                type = "attraction"
                            )
                        )
                    }
                }
            )

            fetchTasks.forEach { task ->
                task.addOnCompleteListener { updateAndSortPlanItems(noPlansMessage, addPlanButton, fabAddPlan) }
            }
        }
    }

    private fun updateAndSortPlanItems(noPlansMessage: TextView, addPlanButton: Button, fabAddPlan: ImageButton) {
        planItems.sortWith(compareBy({ it.date }, { it.time }))
        planItemAdapter.notifyDataSetChanged()

        val plansExist = planItems.isNotEmpty()
        noPlansMessage.visibility = if (plansExist) View.GONE else View.VISIBLE
        addPlanButton.visibility = if (plansExist) View.GONE else View.VISIBLE
        fabAddPlan.visibility = if (plansExist) View.VISIBLE else View.GONE
    }

    private fun openAddAttractionActivity() {
        val addAttractionIntent = Intent(this, AddAttractionActivity::class.java)
        addAttractionIntent.putExtra("tripId", intent.getStringExtra("tripId"))
        startActivity(addAttractionIntent)
    }

    private fun openAddRestaurantActivity() {
        val addRestaurantIntent = Intent(this, AddRestaurantActivity::class.java)
        addRestaurantIntent.putExtra("tripId", intent.getStringExtra("tripId"))
        startActivity(addRestaurantIntent)
    }

    private fun openAddFlightActivity() {
        val addFlightIntent = Intent(this, AddFlightActivity::class.java)
        addFlightIntent.putExtra("tripId", intent.getStringExtra("tripId"))
        startActivity(addFlightIntent)
    }

    private fun openAddLodgingActivity() {
        val addLodgingIntent = Intent(this, AddLodgingActivity::class.java)
        addLodgingIntent.putExtra("tripId", intent.getStringExtra("tripId"))
        startActivity(addLodgingIntent)
    }
}
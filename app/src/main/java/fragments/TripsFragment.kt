package fragments

import activities.CreateTripActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hw1.R
import models.TripModel
import adapters.TripsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class TripsFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var tripsAdapter: TripsAdapter
    private val tripsList = mutableListOf<TripModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trips, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_trips)
        val addTripButton = view.findViewById<ImageButton>(R.id.button_add_trip)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Setup RecyclerView with adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        tripsAdapter = TripsAdapter(tripsList)
        recyclerView.adapter = tripsAdapter

        // Set click listener for the ImageButton to start CreateTripActivity
        addTripButton.setOnClickListener {
            val intent = Intent(activity, CreateTripActivity::class.java)
            startActivity(intent)
        }

        // Get the current user and fetch both owned and shared trips
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserTrips(currentUser.uid)
        } else {
            Toast.makeText(context, "No user is signed in!", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun fetchUserTrips(userId: String) {
        // Fetch trips where the user is the owner or the trip is shared with the user
        db.collection("trips")
            .whereEqualTo("uid", userId)
            .get()
            .addOnSuccessListener { ownedTrips ->
                tripsList.clear()
                addTripsFromSnapshot(ownedTrips)

                // Fetch shared trips
                db.collection("trips")
                    .whereArrayContains("sharedWith", userId)
                    .get()
                    .addOnSuccessListener { sharedTrips ->
                        addTripsFromSnapshot(sharedTrips)
                        // Sort tripsList in ascending order by startDate, and if equal, by startTime
                        tripsList.sortWith(compareBy<TripModel> { it.startDate }.thenBy { it.startTime })
                        tripsAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Failed to fetch shared trips: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to fetch owned trips: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Helper function to add trips from Firestore query snapshot to the list
    private fun addTripsFromSnapshot(snapshot: QuerySnapshot) {
        for (document in snapshot) {
            val trip = document.toObject(TripModel::class.java)
            trip.tripId = document.id
            tripsList.add(trip)
        }
    }
}

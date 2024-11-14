package fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hw1.R
import models.TripModel
import adapters.TripsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tripAdapter: TripsAdapter
    private val favoriteTrips = mutableListOf<TripModel>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(context)
        tripAdapter = TripsAdapter(favoriteTrips)
        recyclerView.adapter = tripAdapter

        loadFavoriteTrips()

        return view
    }

    private fun loadFavoriteTrips() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                favoriteTrips.clear()
                for (document in documents) {
                    val trip = document.toObject(TripModel::class.java)
                    favoriteTrips.add(trip)
                }
                tripAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load favorites: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

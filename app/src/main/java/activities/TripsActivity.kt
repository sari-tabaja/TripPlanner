package activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.hw1.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import fragments.AccountFragment
import fragments.FavoritesFragment
import fragments.TripsFragment

class TripsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips)

        auth = FirebaseAuth.getInstance()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val logoutButton = findViewById<ImageButton>(R.id.buttonLogout)  // Changed to ImageButton
        val helloTextView = findViewById<TextView>(R.id.textHelloName)

        val user = auth.currentUser
        user?.let {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(it.uid)

            userRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "User"
                    helloTextView.text = "Hello, $name"
                } else {
                    helloTextView.text = "Hello, User"
                }
            }.addOnFailureListener {
                helloTextView.text = "Hello, User" // Fallback in case of error
            }
        }

        // Set initial fragment (e.g., TripsFragment)
        loadFragment(TripsFragment())

        // Handle navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_trips -> TripsFragment()
                R.id.nav_account -> AccountFragment()
                R.id.nav_favorites -> FavoritesFragment() // New Favorites fragment
                else -> TripsFragment()
            }
            loadFragment(selectedFragment)
            true
        }


        // Handle logout
        logoutButton.setOnClickListener {
            auth.signOut()  // Sign out from Firebase

            // Redirect to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // Clear back stack
            startActivity(intent)
            finish()  // Close TripsActivity
        }
    }



    private fun loadFragment(fragment: Fragment) {
        // Replace the existing fragment with a new one
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}

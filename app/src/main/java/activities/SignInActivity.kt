package activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hw1.R
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToTripsActivity()
        }

        val emailField = findViewById<EditText>(R.id.emailSignIn)
        val passwordField = findViewById<EditText>(R.id.passwordSignIn)
        val signInButton = findViewById<Button>(R.id.buttonSignIn)
        val signUpRedirect = findViewById<TextView>(R.id.signUpRedirect)

        signInButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                // Ensure user data exists in Firestore
                                db.collection("users").document(user.uid).get()
                                    .addOnSuccessListener { document ->
                                        if (!document.exists()) {
                                            // Save user data to Firestore if it doesn't exist
                                            val userData = hashMapOf(
                                                "email" to user.email,
                                                "uid" to user.uid
                                            )
                                            db.collection("users").document(user.uid).set(userData)
                                                .addOnSuccessListener {
                                                    navigateToTripsActivity()
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            navigateToTripsActivity()
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Failed to check user data", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Sign-in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }

        signUpRedirect.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun navigateToTripsActivity() {
        startActivity(Intent(this, TripsActivity::class.java))
        finish()
    }
}

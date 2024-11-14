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

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        val emailField = findViewById<EditText>(R.id.emailSignUp)
        val passwordField = findViewById<EditText>(R.id.passwordSignUp)
        val signUpButton = findViewById<Button>(R.id.buttonSignUp)
        val signInRedirect = findViewById<TextView>(R.id.signInRedirect)
        val nameField = findViewById<EditText>(R.id.nameSignUp)


        signUpButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val name = nameField.text.toString().trim() // Capture the name

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                // Create a user data map including the name
                                val userData = hashMapOf(
                                    "email" to user.email,
                                    "uid" to user.uid,
                                    "name" to name
                                )
                                db.collection("users").document(user.uid).set(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Sign-up successful!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            "Failed to save user data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            Toast.makeText(
                                this,
                                "Sign-up failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        signInRedirect.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}

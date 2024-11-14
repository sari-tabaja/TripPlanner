package fragments

import activities.SignInActivity
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.hw1.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AccountFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var emailTextView: TextView
    private lateinit var changePasswordButton: Button
    private lateinit var deleteAccountButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")

        // Link UI elements
        profileImageView = view.findViewById(R.id.profileImageView)
        uploadButton = view.findViewById(R.id.uploadButton)
        emailTextView = view.findViewById(R.id.emailTextView)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton)

        // Load existing profile image and email
        loadProfileImage()
        displayUserEmail()

        // Set up button listeners
        uploadButton.setOnClickListener { selectImageFromGallery() }
        changePasswordButton.setOnClickListener { promptChangePassword() }
        deleteAccountButton.setOnClickListener { confirmDeleteAccount() }

        return view
    }

    private fun loadProfileImage() {
        val userId = auth.currentUser?.uid ?: return
        val profileImageRef = storageReference.child("$userId/profile.jpg")

        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
            if (isAdded) {
                Glide.with(this)
                    .load(uri)
                    .into(profileImageView)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "No profile picture uploaded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayUserEmail() {
        val user = auth.currentUser
        emailTextView.text = "Email: ${user?.email}"
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
            if (imageUri != null) {
                Glide.with(this).load(imageUri).into(profileImageView)
                uploadProfileImage() // Call upload function after selecting image
            } else {
                Toast.makeText(context, "Failed to select image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadProfileImage() {
        val userId = auth.currentUser?.uid ?: return
        val profileImageRef = storageReference.child("$userId/profile.jpg")

        imageUri?.let {
            profileImageRef.putFile(it)
                .addOnSuccessListener {
                    profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveProfileImageUrl(uri.toString())
                        Toast.makeText(context, "Profile picture uploaded successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileImageUrl(imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile URL saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to save profile URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun reauthenticateUser(onSuccess: () -> Unit) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            // Create a dialog to prompt for the current password
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Re-authenticate")
            builder.setMessage("Please enter your password to proceed")

            // Set up the input field for the password
            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            builder.setView(input)

            builder.setPositiveButton("Confirm") { dialog, _ ->
                val password = input.text.toString().trim()
                if (password.isNotEmpty()) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, password)

                    // Re-authenticate with the credential
                    user.reauthenticate(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onSuccess()
                            } else {
                                Toast.makeText(context, "Re-authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        } else {
            Toast.makeText(context, "User email not found", Toast.LENGTH_SHORT).show()
        }
    }


    private fun promptChangePassword() {
        reauthenticateUser {
            // Create an alert dialog to ask for the new password
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Change Password")

            // Set up the input field for the new password
            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            builder.setView(input)

            builder.setMessage("Enter your new password:")
            builder.setPositiveButton("Change") { dialog, _ ->
                val newPassword = input.text.toString().trim()
                if (newPassword.isNotEmpty()) {
                    auth.currentUser?.updatePassword(newPassword)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Password change failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            builder.show()
        }
    }


    private fun confirmDeleteAccount() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Account")
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")
        builder.setPositiveButton("Delete") { _, _ ->
            reauthenticateUser {
                deleteAccount()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun deleteAccount() {
        auth.currentUser?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, SignInActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                } else {
                    Toast.makeText(context, "Account deletion failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

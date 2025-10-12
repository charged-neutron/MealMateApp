package com.example.platepal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.platepal.databinding.ActivityRegisterNgoBinding // Import the binding class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// Data class to structure the NGO user's data
data class NgoUser(
    val ngoName: String? = null,
    val ngoDirector: String? = null,
    val email: String? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val role: String? = null
)

class RegisterNgo : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivityRegisterNgoBinding // Declare the binding variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using view binding
        binding = ActivityRegisterNgoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Set click listener on the register button using the binding object
        binding.btnRegister.setOnClickListener {
            // Get text from all fields
            val ngoName = binding.etNgoName.text.toString().trim()
            val ngoDirector = binding.etNgoDirector.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()

            // --- Data Validation ---
            if (ngoName.isEmpty() || ngoDirector.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Firebase Registration ---
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        val userId = firebaseUser!!.uid

                        // Create user object with all the NGO data
                        val ngoUser = NgoUser(
                            ngoName = ngoName,
                            ngoDirector = ngoDirector,
                            email = email,
                            address = address,
                            phoneNumber = phoneNumber,
                            role = "NGO"
                        )

                        // Save the complete NGO user object to the database
                        database.getReference("users").child(userId).setValue(ngoUser)
                            .addOnSuccessListener {
                                Toast.makeText(this, "NGO registration successful!", Toast.LENGTH_SHORT).show()
                                // Redirect to the NGO login screen
                                startActivity(Intent(this, LoginNgo::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                            }

                    } else {
                        // Handle authentication failure
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
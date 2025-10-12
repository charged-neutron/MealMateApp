package com.example.platepal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.platepal.databinding.ActivityRegisterMessBinding // Import the binding class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

// Data class to structure user data for Firebase
data class User(
    val messName: String? = null,
    val messAdminName: String? = null,
    val email: String? = null,
    val address: String? = null,
    val phoneNumber: String? = null,
    val role: String? = null
)

class RegisterMess : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivityRegisterMessBinding // Declare the binding variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using view binding
        binding = ActivityRegisterMessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Access views through the binding object, no more findViewById!
        binding.btnRegister.setOnClickListener {
            // Get text from all fields via the binding object
            val messName = binding.etMessName.text.toString().trim()
            val messAdminName = binding.etMessAdminName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()

            // --- Data Validation ---
            if (messName.isEmpty() || messAdminName.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty() || phoneNumber.isEmpty()) {
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

                        // Create user object with all the data
                        val user = User(
                            messName = messName,
                            messAdminName = messAdminName,
                            email = email,
                            address = address,
                            phoneNumber = phoneNumber,
                            role = "Mess Admin"
                        )

                        // Save the entire user object to the database
                        database.getReference("users").child(userId).setValue(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                                // Redirect to login screen
                                startActivity(Intent(this, LoginMess::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                            }

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
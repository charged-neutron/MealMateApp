package com.example.platepal

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.platepal.models.FoodItem // Import the consolidated FoodItem model
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

// The old, duplicate FoodItem data class has been removed from this file.

class AddFoodActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var currentMessName: String? = null
    private var currentMessAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        fetchCurrentMessDetails()

        val etFoodName = findViewById<TextInputEditText>(R.id.etFoodName)
        val etQuantity = findViewById<TextInputEditText>(R.id.etQuantity)
        val etPickupTime = findViewById<TextInputEditText>(R.id.etPickupTime)
        val etDay = findViewById<TextInputEditText>(R.id.etDay)
        val etBestBefore = findViewById<TextInputEditText>(R.id.etBestBefore)
        val etFoodDescription = findViewById<TextInputEditText>(R.id.etFoodDescription)
        val btnPostFood = findViewById<Button>(R.id.btnPostFood)

        btnPostFood.setOnClickListener {
            val name = etFoodName.text.toString().trim()
            val quantity = etQuantity.text.toString().trim()
            val pickupTime = etPickupTime.text.toString().trim()
            val day = etDay.text.toString().trim()
            val bestBefore = etBestBefore.text.toString().trim()
            val description = etFoodDescription.text.toString().trim()
            val messAdminId = auth.currentUser?.uid

            if (name.isEmpty() || quantity.isEmpty() || pickupTime.isEmpty() || day.isEmpty() || bestBefore.isEmpty() || messAdminId == null) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (currentMessName == null || currentMessAddress == null) {
                Toast.makeText(this, "Could not verify mess details. Please try again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val foodRef = database.getReference("food_listings").push()
            val foodId = foodRef.key

            val foodItemMap = mapOf(
                "id" to foodId,
                "name" to name,
                "quantity" to quantity,
                "pickupTime" to pickupTime,
                "day" to day,
                "bestBefore" to bestBefore,
                "description" to description,
                "postedBy" to messAdminId,
                "messName" to currentMessName,
                "location" to currentMessAddress,
                "timestamp" to ServerValue.TIMESTAMP,
                "claimed" to false,
                "bookedBy" to null,
                "ngoName" to null
            )

            foodRef.setValue(foodItemMap)
                .addOnSuccessListener {
                    database.getReference("users").child(messAdminId).child("postings").child(foodId!!).setValue(true)
                    Toast.makeText(this, "Food posted successfully", Toast.LENGTH_SHORT).show()
                    finish() 
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to post food: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun fetchCurrentMessDetails() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentMessName = snapshot.child("messName").getValue(String::class.java)
                currentMessAddress = snapshot.child("address").getValue(String::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddFoodActivity, "Failed to fetch user details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
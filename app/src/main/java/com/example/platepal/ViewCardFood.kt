package com.example.platepal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.platepal.models.FoodItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewCardFood : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private var currentUserRole: String? = null
    private var currentNgoName: String? = null
    private lateinit var foodId: String

    private lateinit var btnBookFood: Button
    private lateinit var btnMarkAsClaimed: Button
    private lateinit var tvBookingStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_card_food)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        val receivedFoodId = intent.getStringExtra("foodId")
        if (receivedFoodId == null) {
            Toast.makeText(this, "Error: Food item not found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        foodId = receivedFoodId

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        btnBookFood = findViewById(R.id.btnBookFood)
        btnMarkAsClaimed = findViewById(R.id.btnMarkAsClaimed)
        tvBookingStatus = findViewById(R.id.tvMessNameDetail) // Re-using this view for booking status

        fetchCurrentUserRoleAndDetails()
    }

    private fun fetchFoodDetails() {
        val foodRef = database.getReference("food_listings").child(foodId)
        foodRef.addValueEventListener(object : ValueEventListener { // Use real-time listener
            override fun onDataChange(snapshot: DataSnapshot) {
                val foodItem = snapshot.getValue(FoodItem::class.java)
                if (foodItem == null || foodItem.claimed) { // If claimed, it's gone
                    Toast.makeText(this@ViewCardFood, "This food item is no longer available.", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }
                populateUi(foodItem)
                updateButtonAndStatusVisibility(foodItem)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewCardFood, "Failed to load food details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateUi(foodItem: FoodItem) {
        findViewById<TextView>(R.id.tvFoodName).text = foodItem.name
        findViewById<TextView>(R.id.tvMessNameDetail).text = "Posted by: ${foodItem.messName}"
        findViewById<TextView>(R.id.tvLocationDetail).text = foodItem.location
        findViewById<TextView>(R.id.tvFoodDescription).text = foodItem.description
        findViewById<TextView>(R.id.tvQuantity).text = "Serves ${foodItem.quantity} people"
        findViewById<TextView>(R.id.tvPickupTime).text = "Pickup by ${foodItem.pickupTime}"
        findViewById<TextView>(R.id.tvBestBefore).text = "Best before ${foodItem.bestBefore}"
        findViewById<TextView>(R.id.tvDay).text = foodItem.day
    }

    private fun updateButtonAndStatusVisibility(foodItem: FoodItem) {
        val currentUserId = auth.currentUser?.uid
        
        // Handle the booking status text view
        if (foodItem.booked) {
            tvBookingStatus.visibility = View.VISIBLE
            tvBookingStatus.text = "Booked"
        } else {
            tvBookingStatus.visibility = View.GONE
        }

        // Handle button visibility based on role
        when (currentUserRole) {
            "Mess Admin" -> {
                btnBookFood.visibility = View.GONE // Mess admin never books
                // Mess can only claim if they are the poster AND the item is booked but not claimed
                if (foodItem.postedBy == currentUserId && foodItem.booked && !foodItem.claimed) {
                    btnMarkAsClaimed.visibility = View.VISIBLE
                    btnMarkAsClaimed.setOnClickListener { markFoodAsClaimed() }
                } else {
                    btnMarkAsClaimed.visibility = View.GONE
                }
            }
            "NGO" -> {
                btnMarkAsClaimed.visibility = View.GONE // NGO never claims
                if (foodItem.booked) {
                    btnBookFood.visibility = View.GONE // Hide if already booked by anyone
                } else {
                    btnBookFood.visibility = View.VISIBLE
                    btnBookFood.setOnClickListener { bookFood() }
                }
            }
            else -> {
                btnBookFood.visibility = View.GONE
                btnMarkAsClaimed.visibility = View.GONE
            }
        }
    }

    private fun bookFood() {
        val userId = auth.currentUser?.uid
        if (currentNgoName == null || userId == null) {
            Toast.makeText(this, "Could not verify NGO details. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }
        val updates = mapOf(
            "booked" to true,
            "bookedBy" to userId,
            "ngoName" to currentNgoName
        )
        database.getReference("food_listings").child(foodId).updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Food booked successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { Toast.makeText(this, "Failed to book food: ${it.message}", Toast.LENGTH_SHORT).show() }
    }

    private fun markFoodAsClaimed() {
        val foodRef = database.getReference("food_listings").child(foodId)
        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val foodItem = snapshot.getValue(FoodItem::class.java)
                if (foodItem != null && foodItem.booked && !foodItem.claimed) {
                    // Item is in a valid state to be claimed.
                    // The ngoName is already stored from the booking, so we just set claimed to true.
                    foodRef.child("claimed").setValue(true).addOnSuccessListener {
                        Toast.makeText(this@ViewCardFood, "Food marked as claimed!", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this@ViewCardFood, "Failed to update status: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ViewCardFood, "This item cannot be claimed at this moment.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewCardFood, "Failed to verify food status: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun fetchCurrentUserRoleAndDetails() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUserRole = snapshot.child("role").getValue(String::class.java)
                if (currentUserRole == "NGO") {
                    currentNgoName = snapshot.child("ngoName").getValue(String::class.java)
                }
                fetchFoodDetails()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewCardFood, "Failed to verify user role: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
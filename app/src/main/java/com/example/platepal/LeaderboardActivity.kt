package com.example.platepal

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.*

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var messLeaderboardContainer: LinearLayout
    private lateinit var ngoLeaderboardContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        database = FirebaseDatabase.getInstance().reference
        messLeaderboardContainer = findViewById(R.id.mess_leaderboard_container)
        ngoLeaderboardContainer = findViewById(R.id.ngo_leaderboard_container)

        fetchLeaderboardData()
    }

    private fun fetchLeaderboardData() {
        database.child("food_listings").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@LeaderboardActivity, "No data available to generate leaderboard.", Toast.LENGTH_SHORT).show()
                    return
                }

                val messMealMap = mutableMapOf<String, Int>()
                val ngoMealMap = mutableMapOf<String, Int>()

                for (foodSnapshot in snapshot.children) {
                    val foodItem = foodSnapshot.getValue(FoodItem::class.java)
                    if (foodItem != null && foodItem.claimed) {
                        val quantity = foodItem.quantity?.toIntOrNull() ?: 0
                        val messName = foodItem.messName ?: "Unknown Mess"
                        val ngoName = foodItem.ngoName ?: "Unknown NGO"

                        messMealMap[messName] = (messMealMap[messName] ?: 0) + quantity
                        ngoMealMap[ngoName] = (ngoMealMap[ngoName] ?: 0) + quantity
                    }
                }

                val sortedMesses = messMealMap.entries.sortedByDescending { it.value }.take(3)
                val sortedNgos = ngoMealMap.entries.sortedByDescending { it.value }.take(3)

                updateLeaderboardUI(messLeaderboardContainer, sortedMesses, "donated")
                updateLeaderboardUI(ngoLeaderboardContainer, sortedNgos, "distributed")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LeaderboardActivity, "Failed to load leaderboard: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateLeaderboardUI(container: LinearLayout, data: List<Map.Entry<String, Int>>, action: String) {
        container.removeAllViews()
        if (data.isEmpty()) {
            val noDataTv = TextView(this).apply {
                text = "No contributions yet."
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@LeaderboardActivity, R.color.eco_text_secondary))
            }
            container.addView(noDataTv)
            return
        }

        data.forEachIndexed { index, entry ->
            val rankTv = TextView(this).apply {
                text = "${index + 1}. ${entry.key}"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@LeaderboardActivity, R.color.eco_text_primary))
            }

            val mealsTv = TextView(this).apply {
                text = "${entry.value} meals $action"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@LeaderboardActivity, R.color.eco_text_primary))
                gravity = Gravity.END
            }

            val entryLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                addView(rankTv)
                val spacer = android.view.View(this@LeaderboardActivity)
                val params = LinearLayout.LayoutParams(0, 0, 1.0f)
                spacer.layoutParams = params
                addView(spacer)
                addView(mealsTv)
                setPadding(0, 8, 0, 8)
            }
            container.addView(entryLayout)
        }
    }
}
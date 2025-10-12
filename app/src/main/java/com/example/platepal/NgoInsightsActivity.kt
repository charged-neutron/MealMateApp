package com.example.platepal

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.DecimalFormat

class NgoInsightsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InsightAdapter
    private lateinit var database: DatabaseReference
    private val insights = mutableListOf<Insight>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ngo_insights)

        recyclerView = findViewById(R.id.insights_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = InsightAdapter(insights)
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance().reference

        fetchInsights()
    }

    private fun fetchInsights() {
        val ngoId = FirebaseAuth.getInstance().currentUser?.uid
        if (ngoId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("food_listings").orderByChild("bookedBy").equalTo(ngoId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(this@NgoInsightsActivity, "No meals received yet.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    var totalMealsReceived = 0
                    val messMealMap = mutableMapOf<String, Int>()

                    for (foodSnapshot in snapshot.children) {
                        val foodItem = foodSnapshot.getValue(FoodItem::class.java)
                        if (foodItem != null) {
                            val quantity = foodItem.quantity?.toIntOrNull() ?: 0
                            totalMealsReceived += quantity

                            val messName = foodItem.messName ?: "Unknown Mess"
                            messMealMap[messName] = (messMealMap[messName] ?: 0) + quantity
                        }
                    }

                    // --- Impact Calculation ---
                    val wasteSavedKg = totalMealsReceived * 0.3
                    val co2SavingsKg = wasteSavedKg * 4.3
                    val df = DecimalFormat("#,##0.0")
                    val mealsDf = DecimalFormat("#,###")

                    findViewById<TextView>(R.id.total_meals_textview).text = mealsDf.format(totalMealsReceived)
                    findViewById<TextView>(R.id.waste_saved_textview).text = "${df.format(wasteSavedKg)} kg"
                    findViewById<TextView>(R.id.co2_savings_textview).text = "${df.format(co2SavingsKg)} kg"

                    if (messMealMap.isEmpty()) {
                        Toast.makeText(this@NgoInsightsActivity, "No supplying messes to display.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    insights.clear()
                    messMealMap.forEach { (name, quantity) ->
                        insights.add(Insight(name, quantity))
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@NgoInsightsActivity, "Firebase query cancelled: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
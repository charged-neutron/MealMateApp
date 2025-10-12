package com.example.platepal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.platepal.models.FoodItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardNgo : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodList: ArrayList<FoodItem>
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_ngo)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerAvailableFood)
        recyclerView.layoutManager = LinearLayoutManager(this)
        foodList = ArrayList()
        
        foodAdapter = FoodAdapter(foodList, { foodItem ->
            val intent = Intent(this, ViewCardFood::class.java)
            intent.putExtra("foodId", foodItem.id)
            startActivity(intent)
        }, "NGO")
        recyclerView.adapter = foodAdapter

        fetchAvailableFood()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginNgo::class.java))
                finish()
                true
            }
            R.id.action_insights -> {
                startActivity(Intent(this, NgoInsightsActivity::class.java))
                true
            }
            R.id.action_leaderboard -> {
                startActivity(Intent(this, LeaderboardActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun fetchAvailableFood() {
        val foodRef = database.getReference("food_listings")
        val query = foodRef.orderByChild("timestamp")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                foodList.clear()
                val tempList = ArrayList<FoodItem>()
                for (postSnapshot in snapshot.children) {
                    val foodItem = postSnapshot.getValue(FoodItem::class.java)
                    if (foodItem != null && !foodItem.claimed) {
                        tempList.add(foodItem)
                    }
                }
                tempList.reverse()
                foodList.addAll(tempList)
                foodAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardNgo, "Failed to load food items: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
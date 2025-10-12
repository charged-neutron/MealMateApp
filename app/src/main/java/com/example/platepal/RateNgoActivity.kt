package com.example.platepal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RateNgoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate_ngo)

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val btnSubmitRating = findViewById<Button>(R.id.btnSubmitRating)
        val btnSkipRating = findViewById<Button>(R.id.btnSkipRating)

        btnSubmitRating.setOnClickListener {
            val rating = ratingBar.rating
            // For now, just show a toast message. No data is stored.
            Toast.makeText(this, "Rated: $rating stars", Toast.LENGTH_SHORT).show()
            finish() // Go back to the previous activity
        }

        btnSkipRating.setOnClickListener {
            val intent= Intent(this, DashboardMess::class.java)// Go back to the previous activity
            startActivity(intent)
        }
    }
}

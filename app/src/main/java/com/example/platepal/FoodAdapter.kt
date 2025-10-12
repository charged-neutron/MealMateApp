package com.example.platepal

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.platepal.models.FoodItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FoodAdapter(
    private val foodList: List<FoodItem>,
    private val onItemClick: (FoodItem) -> Unit,
    private val userType: String // "NGO" or "Mess"
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food_card, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]
        holder.bind(foodItem, onItemClick, userType)
    }

    override fun getItemCount() = foodList.size

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFoodName: TextView = itemView.findViewById(R.id.tvFoodName)
        private val tvMessName: TextView = itemView.findViewById(R.id.tvMessName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvBookedBy: TextView = itemView.findViewById(R.id.tvBookedBy)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val btnBook: Button = itemView.findViewById(R.id.btnBook)
        private val btnClaim: Button = itemView.findViewById(R.id.btnClaim)

        fun bind(foodItem: FoodItem, onItemClick: (FoodItem) -> Unit, userType: String) {
            tvFoodName.text = foodItem.name
            tvMessName.text = "Posted by: ${foodItem.messName}"
            tvLocation.text = "Location: ${foodItem.location}"
            tvQuantity.text = "Quantity: ${foodItem.quantity}"

            // Handle UI based on user type and food item status
            when (userType) {
                "NGO" -> {
                    btnClaim.visibility = View.GONE // NGOs can't claim
                    if (foodItem.booked) {
                        btnBook.visibility = View.GONE
                        tvBookedBy.visibility = View.VISIBLE
                        tvBookedBy.text = "Booked"
                    } else {
                        btnBook.visibility = View.VISIBLE
                        tvBookedBy.visibility = View.GONE
                    }
                }
                "Mess" -> {
                    btnBook.visibility = View.GONE // Mess can't book
                    if (foodItem.booked) {
                        btnClaim.visibility = View.VISIBLE
                        tvBookedBy.visibility = View.VISIBLE
                        tvBookedBy.text = "Booked"
                    } else {
                        btnClaim.visibility = View.GONE
                        tvBookedBy.visibility = View.GONE
                    }
                    if(foodItem.claimed) {
                        btnClaim.visibility = View.GONE
                        tvBookedBy.visibility = View.VISIBLE
                        tvBookedBy.text = "Claimed"
                    }
                }
            }

            btnBook.setOnClickListener {
                val user = FirebaseAuth.getInstance().currentUser ?: return@setOnClickListener
                val database = FirebaseDatabase.getInstance().reference
                val foodRef = database.child("food_listings").child(foodItem.id!!)
                val userRef = database.child("users").child(user.uid)

                userRef.child("name").get().addOnSuccessListener { dataSnapshot ->
                    val ngoName = dataSnapshot.value as? String
                    if (ngoName != null) {
                        foodRef.child("booked").setValue(true)
                        foodRef.child("bookedBy").setValue(user.uid)
                        foodRef.child("ngoName").setValue(ngoName)
                    }
                }
            }

            btnClaim.setOnClickListener {
                val database = FirebaseDatabase.getInstance().reference
                val foodRef = database.child("food_listings").child(foodItem.id!!)

                foodRef.child("claimed").setValue(true).addOnSuccessListener {
                    val intent = Intent(itemView.context, RateNgoActivity::class.java)
                    itemView.context.startActivity(intent)
                }
            }


            itemView.setOnClickListener { onItemClick(foodItem) }
        }
    }
}
package com.example.platepal.models

data class FoodItem(
    val id: String? = null,
    val name: String? = null,
    val quantity: String? = null,
    val pickupTime: String? = null,
    val day: String? = null,
    val bestBefore: String? = null,
    val description: String? = null,
    val postedBy: String? = null,      // Mess Admin's UID
    val messName: String? = null,      // Mess Admin's Display Name
    val location: String? = null,      // Mess address
    val timestamp: Long? = null,       // Timestamp for sorting
    var booked: Boolean = false,
    var claimed: Boolean = false,
    var bookedBy: String? = null,      // NGO's UID
    var ngoName: String? = null        // NGO's Display Name
)

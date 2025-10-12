package com.example.platepal

data class FoodItem(
    val id: String? = null,
    val name: String? = null,
    val quantity: String? = null,
    val pickupTime: String? = null,
    val day: String? = null,
    val bestBefore: String? = null,
    val description: String? = null,
    val postedBy: String? = null,
    val messName: String? = null,
    val location: String? = null,
    val timestamp: Long? = null, // Timestamp for sorting
    var claimed: Boolean = false,
    var bookedBy: String? = null,
    var ngoName: String? = null
)
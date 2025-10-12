package com.example.platepal

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Donation(
    val quantity: Int = 0,
    val ngoId: String = "",
    val messId: String = ""
)

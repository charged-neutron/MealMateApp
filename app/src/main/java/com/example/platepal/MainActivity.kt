package com.example.platepal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    // Declare the launcher at the top of the activity for handling the permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show()
        } else {
            // Inform the user that they will not receive notifications.
            Toast.makeText(this, "Notifications permission denied. You will not receive updates.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.splash_screen)

        // Request notification permission right away on app start
        askNotificationPermission()

        //Firebase Authentication
        auth = Firebase.auth
     }

    private fun askNotificationPermission() {
        // This is only required for API level 33 and above (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkUserRoleAndRedirect(currentUser.uid)
        }else{
            val loginIntent = Intent(this, AskLogin::class.java)
            startActivity(loginIntent)
            finish()
        }
    }

    private fun checkUserRoleAndRedirect(userId: String) {
        val database = Firebase.database.reference
        database.child("users").child(userId).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val role = dataSnapshot.child("role").value as? String
                when (role) {
                    "Mess Admin" -> {
                        startActivity(Intent(this, DashboardMess::class.java))
                        finish()
                    }
                    "NGO" -> {
                        startActivity(Intent(this, DashboardNgo::class.java))
                        finish()
                    }
                    else -> {
                        // Role is null or unexpected. Logout and go to login.
                        Toast.makeText(this, "User role not found. Please log in again.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                        startActivity(Intent(this, AskLogin::class.java))
                        finish()
                    }
                }
            } else {
                // User exists in Auth but not in the database. Logout and go to login.
                Toast.makeText(this, "User data not found. Please register again.", Toast.LENGTH_LONG).show()
                auth.signOut()
                startActivity(Intent(this, AskLogin::class.java))
                finish()
            }
        }.addOnFailureListener { exception ->
            // Database read failed. Logout and go to login.
            Toast.makeText(this, "Failed to fetch user data: ${exception.message}", Toast.LENGTH_LONG).show()
            auth.signOut()
            startActivity(Intent(this, AskLogin::class.java))
            finish()
        }
    }
}

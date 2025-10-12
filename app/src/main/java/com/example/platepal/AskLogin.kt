package com.example.platepal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AskLogin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnMessAdminLogin = findViewById<Button>(R.id.btnMessAdminLogin)
        val btnNgoLogin = findViewById<Button>(R.id.btnNgoLogin)

        btnMessAdminLogin.setOnClickListener {
            val goLoginMess = Intent(this, LoginMess::class.java)
            startActivity(goLoginMess)
        }

        btnNgoLogin.setOnClickListener {
            val goLoginNgo = Intent(this, LoginNgo::class.java)
            startActivity(goLoginNgo)
        }
    }
}
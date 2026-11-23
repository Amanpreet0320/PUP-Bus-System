package com.example.pup_bus_system

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class PassDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pass_details)

        val backButton = findViewById<ImageButton>(R.id.btnBack)

        backButton.setOnClickListener {
            finish()
        }
    }
}
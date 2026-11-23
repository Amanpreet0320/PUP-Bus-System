package com.example.pup_bus_system

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        val studentName = findViewById<TextView>(R.id.tvStudentName)
        val rollNumber = findViewById<TextView>(R.id.tvRollNumber)
        val department = findViewById<TextView>(R.id.tvDepartment)
        val course = findViewById<TextView>(R.id.tvCourse)
        val logoutButton = findViewById<Button>(R.id.btnLogout)

        val currentUser = auth.currentUser

        if (currentUser == null) {
            // No user logged in → go to Login
            goToLogin()
            return
        }

        // Load real data from Firestore
        firestore.collection("students")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    studentName.text = document.getString("name") ?: "Student Name"
                    rollNumber.text = document.getString("rollNumber") ?: "Not available"
                    department.text = document.getString("department") ?: "Not available"
                    course.text = document.getString("course") ?: "Not available"
                } else {
                    Toast.makeText(this, "Student data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }

        backButton.setOnClickListener {
            finish()
        }

        logoutButton.setOnClickListener {
            // Real Firebase logout
            auth.signOut()

            // Clear the entire back stack and go to Login
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
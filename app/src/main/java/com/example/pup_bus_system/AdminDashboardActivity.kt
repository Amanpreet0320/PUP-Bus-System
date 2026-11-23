package com.example.pup_bus_system

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnManageQueries = findViewById<LinearLayout>(R.id.btnManageQueries)
        val btnPostNotice = findViewById<LinearLayout>(R.id.btnPostNotice)
        val btnViewStudents = findViewById<LinearLayout>(R.id.btnViewStudents)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Show welcome message
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("students")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (!isFinishing && !isDestroyed) {
                        val name = document.getString("name") ?: "Admin"
                        tvWelcome.text = "Welcome, $name"
                    }
                }
                .addOnFailureListener {
                    if (!isFinishing && !isDestroyed) {
                        tvWelcome.text = "Welcome, Admin"
                    }
                }
        } else {
            tvWelcome.text = "Welcome, Admin"
        }

        // 1. Manage Student Queries
        btnManageQueries.setOnClickListener {
            startActivity(Intent(this, AdminQueryActivity::class.java))
        }

        // 2. Post Emergency Notice
        btnPostNotice.setOnClickListener {
            startActivity(Intent(this, PostNoticeActivity::class.java))
        }

        // 3. View Registered Students
        btnViewStudents.setOnClickListener {
            startActivity(Intent(this, ViewStudentActivity::class.java))
        }

        // 4. Logout
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
package com.example.pup_bus_system

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var latestNoticeTitle = ""
    private var latestNoticeMessage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            goToLogin()
            return
        }

        val welcomeText = findViewById<TextView>(R.id.tvWelcome)
        val layoutNotice = findViewById<LinearLayout>(R.id.layoutNotice)

        // Load student name
        firestore.collection("students")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Student"
                    welcomeText.text = "Welcome, $name!"
                } else {
                    welcomeText.text = "Welcome, Student!"
                }
            }
            .addOnFailureListener {
                welcomeText.text = "Welcome, Student!"
            }

        // Load latest notice
        loadLatestNotice(layoutNotice)

        // When student taps the notice banner
        layoutNotice.setOnClickListener {
            if (latestNoticeTitle.isNotEmpty() || latestNoticeMessage.isNotEmpty()) {
                showFullNoticeDialog()
            }
        }

        // Cards click listeners
        findViewById<LinearLayout>(R.id.cardBusStops).setOnClickListener {
            startActivity(Intent(this, BusStopsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardBusTiming).setOnClickListener {
            startActivity(Intent(this, BusTimingActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardDriverDetails).setOnClickListener {
            startActivity(Intent(this, DriverDetailsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardBusDetails).setOnClickListener {
            startActivity(Intent(this, BusDetailsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardPassDetails).setOnClickListener {
            startActivity(Intent(this, PassDetailsActivity::class.java))
        }
        findViewById<TextView>(R.id.tvHelpCenter).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadLatestNotice(layoutNotice: LinearLayout) {
        firestore.collection("notices")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val notice = documents.documents[0]
                    latestNoticeTitle = notice.getString("title") ?: ""
                    latestNoticeMessage = notice.getString("message") ?: ""

                    if (latestNoticeTitle.isNotEmpty() || latestNoticeMessage.isNotEmpty()) {
                        layoutNotice.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun showFullNoticeDialog() {
        AlertDialog.Builder(this)
            .setTitle("🚨 $latestNoticeTitle")
            .setMessage(latestNoticeMessage)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun goToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
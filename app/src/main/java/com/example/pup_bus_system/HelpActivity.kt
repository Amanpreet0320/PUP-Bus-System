package com.example.pup_bus_system

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HelpActivity : AppCompatActivity() {

    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val backButton = findViewById<ImageButton>(R.id.btnBack)
        val query = findViewById<EditText>(R.id.etQuery)
        val submitButton = findViewById<Button>(R.id.btnSubmit)
        val submittedQuery = findViewById<TextView>(R.id.tvSubmittedQuery)
        val adminReply = findViewById<TextView>(R.id.tvAdminReply)

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            return
        }

        backButton.setOnClickListener { finish() }

        // Live updates: shows the student's latest query and the admin's reply when it arrives
        listener = db.collection("queries")
            .whereEqualTo("studentId", user.uid)
            .addSnapshotListener { snap, _ ->
                if (snap == null || snap.isEmpty) return@addSnapshotListener
                val latest = snap.documents.maxByOrNull {
                    it.getTimestamp("createdAt")?.seconds ?: Long.MAX_VALUE
                } ?: return@addSnapshotListener

                submittedQuery.text = latest.getString("query") ?: ""
                val reply = latest.getString("reply") ?: ""
                adminReply.text = if (reply.isEmpty()) "No reply yet" else reply
            }

        submitButton.setOnClickListener {
            val text = query.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Please write your query", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitButton.isEnabled = false

            fun saveQuery(name: String) {
                val data = hashMapOf(
                    "studentId" to user.uid,
                    "studentName" to name,
                    "query" to text,
                    "reply" to "",
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("queries").add(data)
                    .addOnSuccessListener {
                        query.text.clear()
                        submitButton.isEnabled = true
                        Toast.makeText(this, "Query submitted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        submitButton.isEnabled = true
                        Toast.makeText(this, "Failed to submit. Check internet.", Toast.LENGTH_SHORT).show()
                    }
            }

            db.collection("students").document(user.uid).get()
                .addOnSuccessListener { doc -> saveQuery(doc.getString("name") ?: "Student") }
                .addOnFailureListener { saveQuery("Student") }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}
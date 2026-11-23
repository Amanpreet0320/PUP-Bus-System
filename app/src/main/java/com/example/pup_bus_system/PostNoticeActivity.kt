package com.example.pup_bus_system

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PostNoticeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_notice)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnPost = findViewById<Button>(R.id.btnPost)

        btnBack.setOnClickListener {
            finish()
        }

        btnPost.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val message = etMessage.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter notice title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter notice message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnPost.isEnabled = false

            val noticeData = hashMapOf(
                "title" to title,
                "message" to message,
                "postedBy" to (auth.currentUser?.uid ?: "admin"),
                "postedAt" to FieldValue.serverTimestamp(),
                "isActive" to true
            )

            firestore.collection("notices")
                .add(noticeData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Emergency Notice posted successfully", Toast.LENGTH_LONG).show()
                    etTitle.text.clear()
                    etMessage.text.clear()
                    btnPost.isEnabled = true
                    finish()
                }
                .addOnFailureListener { e ->
                    btnPost.isEnabled = true
                    Toast.makeText(this, "Failed to post notice: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
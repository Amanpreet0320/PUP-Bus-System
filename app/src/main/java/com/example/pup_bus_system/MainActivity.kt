package com.example.pup_bus_system

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val etRollNumber = findViewById<EditText>(R.id.etRollNumber)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<TextView?>(R.id.tvForgotPassword)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // 1. LOGIN BUTTON
        btnLogin.setOnClickListener {
            var loginInput = etRollNumber.text.toString().trim()
            val pass = etPassword.text.toString()

            if (loginInput.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter Login ID and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            // Admin shortcut: typing "admin" maps to admin email
            if (loginInput.equals("admin", ignoreCase = true)) {
                loginInput = "adminforpupbus@gmail.com"
            }

            // Case A: Direct Email login (Admin or Student with email)
            if (loginInput.contains("@")) {
                auth.signInWithEmailAndPassword(loginInput, pass)
                    .addOnCompleteListener { task ->
                        btnLogin.isEnabled = true
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                checkUserRoleAndRedirect(user.uid)
                            }
                        } else {
                            val msg = task.exception?.localizedMessage ?: "Invalid Email or Password"
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                return@setOnClickListener
            }

            // Case B: Student Roll Number login
            firestore.collection("students")
                .whereEqualTo("rollNumber", loginInput)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        btnLogin.isEnabled = true
                        Toast.makeText(this, "No student found with Roll Number: $loginInput", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val email = documents.documents[0].getString("email") ?: ""
                    if (email.isEmpty()) {
                        btnLogin.isEnabled = true
                        Toast.makeText(this, "Account error: No email linked to this Roll Number", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            btnLogin.isEnabled = true
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null) {
                                    checkUserRoleAndRedirect(user.uid)
                                }
                            } else {
                                val msg = task.exception?.localizedMessage ?: "Invalid Password"
                                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                }
                .addOnFailureListener { e ->
                    btnLogin.isEnabled = true
                    Toast.makeText(this, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // 2. FORGOT PASSWORD (Cleaned without syntax errors)
        tvForgotPassword?.setOnClickListener {
            showForgotPasswordDialog()
        }

        // 3. REGISTER LINK
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showForgotPasswordDialog() {
        val dp16 = (16 * resources.displayMetrics.density).toInt()
        val dp20 = (20 * resources.displayMetrics.density).toInt()

        val input = EditText(this).apply {
            hint = "Enter Roll Number or Registered Email"
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(dp20, dp16, dp20, dp16)
        }

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your Roll Number or Email to receive a password reset link:")
            .setView(input)
            .setPositiveButton("Send Reset Link") { _, _ ->
                val userIdentifier = input.text.toString().trim()
                if (userIdentifier.isEmpty()) {
                    Toast.makeText(this, "Please enter your Roll Number or Email", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (userIdentifier.contains("@")) {
                    sendPasswordReset(userIdentifier)
                } else {
                    firestore.collection("students")
                        .whereEqualTo("rollNumber", userIdentifier)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { docs ->
                            if (!docs.isEmpty) {
                                val email = docs.documents[0].getString("email") ?: ""
                                if (email.isNotEmpty()) {
                                    sendPasswordReset(email)
                                } else {
                                    Toast.makeText(this, "No email linked to this Roll Number", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Roll Number not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, "Reset link sent to $email! Check your inbox.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send reset link: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun checkUserRoleAndRedirect(uid: String) {
        val userEmail = auth.currentUser?.email?.lowercase() ?: ""

        if (userEmail.contains("admin")) {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()
            return
        }

        firestore.collection("students")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role") ?: "student"
                    if (role.equals("admin", ignoreCase = true)) {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
    }
}
package com.example.pup_bus_system

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etRollNumber = findViewById<EditText>(R.id.etRollNumber)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etDepartment = findViewById<EditText>(R.id.etDepartment)
        val etCourse = findViewById<EditText>(R.id.etCourse)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {

            val name = etName.text.toString().trim()
            val roll = etRollNumber.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val department = etDepartment.text.toString().trim()
            val course = etCourse.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            // Validation
            if (name.isEmpty() || roll.isEmpty() || email.isEmpty() ||
                department.isEmpty() || course.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false

            // Create user with real email
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val user = auth.currentUser

                        if (user == null) {
                            btnRegister.isEnabled = true
                            Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_LONG).show()
                            return@addOnCompleteListener
                        }

                        val studentData = hashMapOf<String, Any>(
                            "name" to name,
                            "rollNumber" to roll,
                            "email" to email,
                            "department" to department,
                            "course" to course,
                            "role" to "student",
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        firestore.collection("students")
                            .document(user.uid)
                            .set(studentData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                auth.signOut()

                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { error ->
                                user.delete()
                                btnRegister.isEnabled = true
                                Toast.makeText(
                                    this,
                                    "Could not save details: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                    } else {
                        btnRegister.isEnabled = true
                        val errorMsg = task.exception?.message ?: "Registration failed"
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }
}
package com.example.pup_bus_system

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ViewStudentActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var studentList: LinearLayout
    private lateinit var countView: TextView
    private lateinit var emptyView: TextView

    private var students = listOf<DocumentSnapshot>()
    private var loadFailed = false

    private val pastelColors = listOf(
        0xFFE3EFFF.toInt(),
        0xFFFFE8DE.toInt(),
        0xFFDDF5E8.toInt(),
        0xFFF0E5FF.toInt(),
        0xFFFFF0C9.toInt()
    )

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_student)

        studentList = findViewById(R.id.studentList)
        countView = findViewById(R.id.tvStudentCount)
        emptyView = findViewById(R.id.tvEmptyStudents)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<EditText>(R.id.etSearchStudent)
            .doAfterTextChanged { text ->
                render(text?.toString().orEmpty())
            }

        loadStudents()
    }

    private fun loadStudents() {
        countView.text = "Loading..."
        emptyView.visibility = View.VISIBLE
        emptyView.text = "Loading student records..."

        db.collection("students")
            .get()
            .addOnSuccessListener { snapshot ->
                students = snapshot.documents
                    .filter {
                        it.getString("role")
                            ?.equals("admin", ignoreCase = true) != true
                    }
                    .sortedBy {
                        (it.getString("name") ?: "").lowercase()
                    }

                loadFailed = false

                val query = findViewById<EditText>(
                    R.id.etSearchStudent
                ).text.toString()

                render(query)
            }
            .addOnFailureListener { error ->
                loadFailed = true
                countView.text = "Unavailable"
                studentList.removeAllViews()
                emptyView.visibility = View.VISIBLE
                emptyView.text = "Could not load student records.\nPlease check your connection and permissions."

                Toast.makeText(
                    this,
                    "Could not load students: ${error.localizedMessage ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun render(filter: String) {
        if (!::studentList.isInitialized) return

        studentList.removeAllViews()

        if (loadFailed) return

        val query = filter.trim()

        val shown = students.filter { student ->
            val name = student.getString("name") ?: ""
            val rollNumber = student.getString("rollNumber") ?: ""

            query.isEmpty() ||
                    name.contains(query, ignoreCase = true) ||
                    rollNumber.contains(query, ignoreCase = true)
        }

        countView.text = "${shown.size} student(s)"
        emptyView.visibility =
            if (shown.isEmpty()) View.VISIBLE else View.GONE

        if (shown.isEmpty()) {
            emptyView.text = if (students.isEmpty()) {
                "No registered students found yet."
            } else {
                "No students match your search.\nTry another name or roll number."
            }
            return
        }

        shown.forEachIndexed { index, student ->
            studentList.addView(createStudentCard(student, index))
        }
    }

    private fun createStudentCard(
        student: DocumentSnapshot,
        index: Int
    ): LinearLayout {

        val name = student.getString("name")
            ?.takeIf { it.isNotBlank() } ?: "Unnamed Student"

        val roll = student.getString("rollNumber")
            ?.takeIf { it.isNotBlank() } ?: "Not provided"

        val department = student.getString("department")
            ?.takeIf { it.isNotBlank() } ?: "Department not provided"

        val course = student.getString("course")
            ?.takeIf { it.isNotBlank() } ?: "Course not provided"

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(18))

            background = roundedBackground(
                Color.WHITE,
                22
            )

            elevation = dp(3).toFloat()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
        }

        // Colored initials avatar
        val initials = name.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifBlank { "S" }

        val avatar = TextView(this).apply {
            text = initials
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFF44356B.toInt())
            gravity = Gravity.CENTER
            background = roundedBackground(
                pastelColors[index % pastelColors.size],
                18
            )
            layoutParams = LinearLayout.LayoutParams(
                dp(58),
                dp(58)
            ).apply {
                marginEnd = dp(14)
            }
        }

        card.addView(avatar)

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        details.addView(TextView(this).apply {
            text = name
            textSize = 19f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFF30264D.toInt())
        })

        details.addView(TextView(this).apply {
            text = "Roll No: $roll"
            textSize = 15f
            setTextColor(0xFF655A80.toInt())
            setPadding(0, dp(5), 0, dp(5))
        })

        details.addView(TextView(this).apply {
            text = department
            textSize = 15f
            setTextColor(0xFF484052.toInt())
        })

        details.addView(TextView(this).apply {
            text = course
            textSize = 14f
            setTextColor(0xFF716782.toInt())
            setPadding(0, dp(3), 0, 0)
        })

        card.addView(details)

        return card
    }

    private fun roundedBackground(
        color: Int,
        radiusDp: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
        }
    }
}

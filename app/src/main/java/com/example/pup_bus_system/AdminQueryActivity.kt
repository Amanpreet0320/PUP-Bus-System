package com.example.pup_bus_system

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class AdminQueryActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null
    private lateinit var list: LinearLayout

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun roundedBackground(
        color: Int,
        radius: Int = 18,
        strokeColor: Int = Color.TRANSPARENT
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()

            if (strokeColor != Color.TRANSPARENT) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun makeText(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)

            if (bold) {
                setTypeface(typeface, Typeface.BOLD)
            }

            gravity = Gravity.CENTER_VERTICAL
            includeFontPadding = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_query)

        list = findViewById(R.id.queryList)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        listener = db.collection("queries")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (isFinishing || isDestroyed) return@addSnapshotListener

                if (error != null) {
                    Toast.makeText(
                        this,
                        "Could not load queries. Check your connection.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addSnapshotListener
                }

                list.removeAllViews()

                if (snapshot == null || snapshot.isEmpty) {
                    val emptyView = makeText(
                        "No student queries yet.",
                        17f,
                        Color.rgb(101, 90, 126)
                    ).apply {
                        gravity = Gravity.CENTER
                        setPadding(dp(16), dp(36), dp(16), dp(36))
                        background = roundedBackground(
                            Color.WHITE,
                            18,
                            Color.rgb(231, 224, 245)
                        )
                    }

                    list.addView(
                        emptyView,
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    )

                    return@addSnapshotListener
                }

                snapshot.documents.forEach { document ->
                    addCard(document)
                }
            }
    }

    private fun addCard(doc: DocumentSnapshot) {
        val name = doc.getString("studentName") ?: "Student"
        val query = doc.getString("query") ?: ""
        val reply = doc.getString("reply") ?: ""
        val answered = reply.isNotBlank()

        // Individual pastel query card
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = roundedBackground(
                Color.WHITE,
                18,
                Color.rgb(231, 224, 245)
            )
            elevation = dp(2).toFloat()
            isClickable = true
            isFocusable = true
            foreground = getDrawable(
                androidx.appcompat.R.drawable.abc_list_selector_background_transition_holo_light
            )
        }

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(16)
        }

        // Student name and status
        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val studentName = makeText(
            name,
            18f,
            Color.rgb(48, 38, 83),
            true
        )

        topRow.addView(
            studentName,
            LinearLayout.LayoutParams(0, dp(44), 1f)
        )

        val status = TextView(this).apply {
            text = if (answered) "Answered" else "Pending"
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(
                if (answered) Color.rgb(36, 100, 68)
                else Color.rgb(126, 73, 38)
            )
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = roundedBackground(
                if (answered) Color.rgb(221, 245, 232)
                else Color.rgb(255, 233, 223),
                30
            )
        }

        topRow.addView(status)
        card.addView(topRow)

        // Student's question
        val questionLabel = makeText(
            "STUDENT QUESTION",
            13f,
            Color.rgb(101, 90, 126),
            true
        ).apply {
            setPadding(0, dp(14), 0, dp(4))
        }

        card.addView(questionLabel)

        val questionText = makeText(
            query.ifBlank { "No question text available." },
            17f,
            Color.rgb(45, 45, 55)
        ).apply {
            gravity = Gravity.TOP
            setPadding(0, dp(4), 0, dp(12))
        }

        card.addView(questionText)

        // Existing reply, when available
        if (answered) {
            val replyBox = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(12), dp(14), dp(12))
                background = roundedBackground(
                    Color.rgb(237, 248, 242),
                    12
                )
            }

            replyBox.addView(
                makeText(
                    "ADMIN REPLY",
                    13f,
                    Color.rgb(36, 100, 68),
                    true
                )
            )

            replyBox.addView(
                makeText(
                    reply,
                    16f,
                    Color.rgb(49, 79, 63)
                ).apply {
                    gravity = Gravity.TOP
                    setPadding(0, dp(5), 0, 0)
                }
            )

            card.addView(replyBox)
        }

        val replyButton = TextView(this).apply {
            text = if (answered) "Edit Reply  →" else "Reply to Student  →"
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.rgb(81, 68, 119))
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = roundedBackground(
                Color.rgb(233, 226, 255),
                12
            )
            isClickable = true
            isFocusable = true
            contentDescription = if (answered) {
                "Edit reply to $name"
            } else {
                "Reply to $name"
            }
        }

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(14)
        }

        card.addView(replyButton, buttonParams)

        val openReply = View.OnClickListener {
            showReplyDialog(doc.id, query, reply)
        }

        card.setOnClickListener(openReply)
        replyButton.setOnClickListener(openReply)

        list.addView(card, cardParams)
    }

    private fun showReplyDialog(
        docId: String,
        query: String,
        oldReply: String
    ) {
        val input = EditText(this).apply {
            hint = "Write a helpful reply..."
            setText(oldReply)
            textSize = 17f
            minLines = 3
            maxLines = 6
            gravity = Gravity.TOP
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = roundedBackground(
                Color.WHITE,
                12,
                Color.rgb(210, 199, 233)
            )
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(8), dp(22), dp(4))
            addView(
                input,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        AlertDialog.Builder(this)
            .setTitle("Reply to Student")
            .setMessage(query)
            .setView(container)
            .setPositiveButton("Send Reply", null)
            .setNegativeButton("Cancel", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener {
                            val text = input.text.toString().trim()

                            if (text.isEmpty()) {
                                input.error = "Please enter a reply"
                                return@setOnClickListener
                            }

                            val button = dialog.getButton(
                                AlertDialog.BUTTON_POSITIVE
                            )
                            button.isEnabled = false

                            db.collection("queries")
                                .document(docId)
                                .update(
                                    mapOf(
                                        "reply" to text,
                                        "status" to "answered"
                                    )
                                )
                                .addOnSuccessListener {
                                    if (!isFinishing && !isDestroyed) {
                                        Toast.makeText(
                                            this,
                                            "Reply saved successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        dialog.dismiss()
                                    }
                                }
                                .addOnFailureListener {
                                    if (!isFinishing && !isDestroyed) {
                                        button.isEnabled = true
                                        Toast.makeText(
                                            this,
                                            "Failed to send reply",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        }
                }

                dialog.show()
            }
    }

    override fun onDestroy() {
        listener?.remove()
        listener = null
        super.onDestroy()
    }
}
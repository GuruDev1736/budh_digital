package com.budhdigital.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.budhdigital.app.databinding.ActivityAddQuestionBinding
import com.budhdigital.app.models.ForumQuestion

class AddQuestionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddQuestionBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityAddQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupEdgeToEdge()
        setupToolbar()
        setupButtons()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.toolbar.setPadding(
                0,
                insets.top,
                0,
                0
            )

            view.setPadding(
                0,
                0,
                0,
                insets.bottom
            )

            windowInsets
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupButtons() {
        binding.btnSubmitQuestion.setOnClickListener {
            submitQuestion()
        }
    }

    private fun submitQuestion() {
        val question = binding.etQuestion.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (question.isEmpty()) {
            binding.tilQuestion.error = "Please enter a question"
            return
        }

        binding.tilQuestion.error = null
        binding.btnSubmitQuestion.isEnabled = false

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            binding.btnSubmitQuestion.isEnabled = true
            return
        }

        database.reference.child("users").child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("fullName").getValue(String::class.java) ?: "Anonymous"
                    val userEmail = snapshot.child("email").getValue(String::class.java) ?: ""

                    val questionId = database.reference.child("forum_questions").push().key ?: return

                    val forumQuestion = ForumQuestion(
                        id = questionId,
                        userId = currentUser.uid,
                        userName = userName,
                        userEmail = userEmail,
                        question = question,
                        description = description,
                        timestamp = System.currentTimeMillis(),
                        replyCount = 0,
                        likeCount = 0,
                        dislikeCount = 0,
                        likedBy = emptyMap<String, Boolean>(),
                        dislikedBy = emptyMap<String, Boolean>()
                    )

                    database.reference.child("forum_questions").child(questionId)
                        .setValue(forumQuestion)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@AddQuestionActivity,
                                "Question posted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@AddQuestionActivity,
                                "Failed to post question",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.btnSubmitQuestion.isEnabled = true
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@AddQuestionActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnSubmitQuestion.isEnabled = true
                }
            })
    }
}


package com.budhdigital.app

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.budhdigital.app.adapters.ForumReplyAdapter
import com.budhdigital.app.databinding.ActivityQuestionDetailBinding
import com.budhdigital.app.models.ForumQuestion
import com.budhdigital.app.models.ForumReply
import java.text.SimpleDateFormat
import java.util.*

class QuestionDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var replyAdapter: ForumReplyAdapter
    private val repliesList = mutableListOf<ForumReply>()
    private var currentQuestion: ForumQuestion? = null
    private var questionId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityQuestionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        questionId = intent.getStringExtra("QUESTION_ID") ?: ""

        setupEdgeToEdge()
        setupToolbar()
        setupRecyclerView()
        setupButtons()
        loadQuestion()
        loadReplies()
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

    private fun setupRecyclerView() {
        replyAdapter = ForumReplyAdapter(
            repliesList,
            onLikeClick = { reply ->
                toggleReplyLike(reply)
            }
        )

        binding.recyclerViewReplies.apply {
            layoutManager = LinearLayoutManager(this@QuestionDetailActivity)
            adapter = replyAdapter
        }
    }

    private fun setupButtons() {
        binding.btnSendReply.setOnClickListener {
            submitReply()
        }
    }

    private fun loadQuestion() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView.visibility = View.GONE

        database.child("forum_questions").child(questionId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE

                    currentQuestion = snapshot.getValue(ForumQuestion::class.java)
                    currentQuestion?.let { displayQuestion(it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE
                }
            })
    }

    private fun displayQuestion(question: ForumQuestion) {
        binding.tvUserInitial.text = question.userName.firstOrNull()?.uppercase() ?: "U"
        binding.tvUserName.text = question.userName
        binding.tvTimestamp.text = getTimeAgo(question.timestamp)
        binding.tvQuestion.text = question.question
        binding.tvDescription.text = question.description.ifEmpty { "No description" }
        binding.tvLikeCount.text = question.likeCount.toString()
        binding.tvDislikeCount.text = question.dislikeCount.toString()

        val currentUserId = auth.currentUser?.uid ?: ""
        val userLiked = question.likedBy.containsKey(currentUserId) && question.likedBy[currentUserId] == true
        val userDisliked = question.dislikedBy.containsKey(currentUserId) && question.dislikedBy[currentUserId] == true

        if (userLiked) {
            binding.btnLike.setImageResource(R.drawable.ic_like_filled)
            binding.btnLike.setColorFilter(Color.parseColor("#4CAF50"))
        } else {
            binding.btnLike.setImageResource(R.drawable.ic_like_outline)
            binding.btnLike.setColorFilter(Color.parseColor("#9E9E9E"))
        }

        if (userDisliked) {
            binding.btnDislike.setImageResource(R.drawable.ic_dislike_filled)
            binding.btnDislike.setColorFilter(Color.parseColor("#F44336"))
        } else {
            binding.btnDislike.setImageResource(R.drawable.ic_dislike_outline)
            binding.btnDislike.setColorFilter(Color.parseColor("#9E9E9E"))
        }

        binding.btnLike.setOnClickListener {
            toggleQuestionLike()
        }

        binding.btnDislike.setOnClickListener {
            toggleQuestionDislike()
        }
    }

    private fun loadReplies() {
        database.child("forum_replies").child(questionId)
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    repliesList.clear()
                    for (replySnapshot in snapshot.children) {
                        val reply = replySnapshot.getValue(ForumReply::class.java)
                        reply?.let { repliesList.add(it) }
                    }

                    if (repliesList.isEmpty()) {
                        binding.tvEmptyReplies.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyReplies.visibility = View.GONE
                    }

                    replyAdapter.updateReplies(repliesList)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun submitReply() {
        val replyText = binding.etReply.text.toString().trim()

        if (replyText.isEmpty()) {
            binding.tilReply.error = "Please enter a reply"
            return
        }

        binding.tilReply.error = null
        binding.btnSendReply.isEnabled = false

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            binding.btnSendReply.isEnabled = true
            return
        }

        database.child("users").child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("fullName").getValue(String::class.java) ?: "Anonymous"
                    val userEmail = snapshot.child("email").getValue(String::class.java) ?: ""

                    val replyId = database.child("forum_replies").child(questionId).push().key ?: return

                    val forumReply = ForumReply(
                        id = replyId,
                        questionId = questionId,
                        userId = currentUser.uid,
                        userName = userName,
                        userEmail = userEmail,
                        reply = replyText,
                        timestamp = System.currentTimeMillis(),
                        likeCount = 0,
                        likedBy = emptyMap()
                    )

                    database.child("forum_replies").child(questionId).child(replyId)
                        .setValue(forumReply)
                        .addOnSuccessListener {
                            database.child("forum_questions").child(questionId)
                                .child("replyCount")
                                .setValue((currentQuestion?.replyCount ?: 0) + 1)

                            binding.etReply.setText("")
                            Toast.makeText(
                                this@QuestionDetailActivity,
                                "Reply posted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.btnSendReply.isEnabled = true
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@QuestionDetailActivity,
                                "Failed to post reply",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.btnSendReply.isEnabled = true
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@QuestionDetailActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnSendReply.isEnabled = true
                }
            })
    }

    private fun toggleQuestionLike() {
        val currentUserId = auth.currentUser?.uid ?: return
        val question = currentQuestion ?: return

        val questionRef = database.child("forum_questions").child(questionId)

        val userLiked = question.likedBy.containsKey(currentUserId) && question.likedBy[currentUserId] == true
        val userDisliked = question.dislikedBy.containsKey(currentUserId) && question.dislikedBy[currentUserId] == true

        val updates = mutableMapOf<String, Any?>()

        if (userLiked) {
            updates["likedBy/$currentUserId"] = null
            updates["likeCount"] = (question.likeCount - 1).coerceAtLeast(0)
        } else {
            updates["likedBy/$currentUserId"] = true
            updates["likeCount"] = question.likeCount + 1

            if (userDisliked) {
                updates["dislikedBy/$currentUserId"] = null
                updates["dislikeCount"] = (question.dislikeCount - 1).coerceAtLeast(0)
            }
        }

        questionRef.updateChildren(updates)
    }

    private fun toggleQuestionDislike() {
        val currentUserId = auth.currentUser?.uid ?: return
        val question = currentQuestion ?: return

        val questionRef = database.child("forum_questions").child(questionId)

        val userLiked = question.likedBy.containsKey(currentUserId) && question.likedBy[currentUserId] == true
        val userDisliked = question.dislikedBy.containsKey(currentUserId) && question.dislikedBy[currentUserId] == true

        val updates = mutableMapOf<String, Any?>()

        if (userDisliked) {
            updates["dislikedBy/$currentUserId"] = null
            updates["dislikeCount"] = (question.dislikeCount - 1).coerceAtLeast(0)
        } else {
            updates["dislikedBy/$currentUserId"] = true
            updates["dislikeCount"] = question.dislikeCount + 1

            if (userLiked) {
                updates["likedBy/$currentUserId"] = null
                updates["likeCount"] = (question.likeCount - 1).coerceAtLeast(0)
            }
        }

        questionRef.updateChildren(updates)
    }

    private fun toggleReplyLike(reply: ForumReply) {
        val currentUserId = auth.currentUser?.uid ?: return
        val replyRef = database.child("forum_replies").child(questionId).child(reply.id)

        val userLiked = reply.likedBy.containsKey(currentUserId) && reply.likedBy[currentUserId] == true

        val updates = mutableMapOf<String, Any?>()

        if (userLiked) {
            updates["likedBy/$currentUserId"] = null
            updates["likeCount"] = (reply.likeCount - 1).coerceAtLeast(0)
        } else {
            updates["likedBy/$currentUserId"] = true
            updates["likeCount"] = reply.likeCount + 1
        }

        replyRef.updateChildren(updates)
    }

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}

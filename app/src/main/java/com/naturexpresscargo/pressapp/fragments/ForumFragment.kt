package com.naturexpresscargo.pressapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.naturexpresscargo.pressapp.AddQuestionActivity
import com.naturexpresscargo.pressapp.QuestionDetailActivity
import com.naturexpresscargo.pressapp.R
import com.naturexpresscargo.pressapp.adapters.ForumQuestionAdapter
import com.naturexpresscargo.pressapp.databinding.FragmentForumBinding
import com.naturexpresscargo.pressapp.models.ForumQuestion

class ForumFragment : Fragment() {
    private var _binding: FragmentForumBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ForumQuestionAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val questionsList = mutableListOf<ForumQuestion>()
    private var questionsListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupRecyclerView()
        setupFab()
        loadQuestions()
    }

    private fun setupRecyclerView() {
        adapter = ForumQuestionAdapter(
            questionsList,
            onQuestionClick = { question ->
                openQuestionDetail(question)
            },
            onLikeClick = { question ->
                toggleLike(question)
            },
            onDislikeClick = { question ->
                toggleDislike(question)
            }
        )

        binding.recyclerViewQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ForumFragment.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddQuestion.setOnClickListener {
            val intent = Intent(requireContext(), AddQuestionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadQuestions() {
        _binding?.let { it.progressBar.visibility = View.VISIBLE }

        questionsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questionsList.clear()
                for (questionSnapshot in snapshot.children) {
                    val question = questionSnapshot.getValue(ForumQuestion::class.java)
                    question?.let { questionsList.add(0, it) }
                }

                // Check if binding is not null before accessing it
                _binding?.let { binding ->
                    binding.progressBar.visibility = View.GONE

                    if (questionsList.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.recyclerViewQuestions.visibility = View.GONE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                        binding.recyclerViewQuestions.visibility = View.VISIBLE
                    }
                }

                adapter.updateQuestions(questionsList)
            }

            override fun onCancelled(error: DatabaseError) {
                _binding?.let { binding ->
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        database.child("forum_questions")
            .orderByChild("timestamp")
            .addValueEventListener(questionsListener!!)
    }

    private fun openQuestionDetail(question: ForumQuestion) {
        val intent = Intent(requireContext(), QuestionDetailActivity::class.java)
        intent.putExtra("QUESTION_ID", question.id)
        startActivity(intent)
    }

    private fun toggleLike(question: ForumQuestion) {
        val currentUserId = auth.currentUser?.uid ?: return
        val questionRef = database.child("forum_questions").child(question.id)

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

    private fun toggleDislike(question: ForumQuestion) {
        val currentUserId = auth.currentUser?.uid ?: return
        val questionRef = database.child("forum_questions").child(question.id)

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

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove Firebase listener to prevent memory leaks
        questionsListener?.let {
            database.child("forum_questions").removeEventListener(it)
        }
        _binding = null
    }
}

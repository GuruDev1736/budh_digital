package com.naturexpresscargo.pressapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.naturexpresscargo.pressapp.R
import com.naturexpresscargo.pressapp.models.ForumQuestion
import java.text.SimpleDateFormat
import java.util.*

class ForumQuestionAdapter(
    private var questions: List<ForumQuestion>,
    private val onQuestionClick: (ForumQuestion) -> Unit,
    private val onLikeClick: (ForumQuestion) -> Unit,
    private val onDislikeClick: (ForumQuestion) -> Unit
) : RecyclerView.Adapter<ForumQuestionAdapter.QuestionViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardQuestion: CardView = itemView.findViewById(R.id.cardQuestion)
        val tvUserInitial: TextView = itemView.findViewById(R.id.tvUserInitial)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
        val btnDislike: ImageButton = itemView.findViewById(R.id.btnDislike)
        val tvDislikeCount: TextView = itemView.findViewById(R.id.tvDislikeCount)
        val tvReplyCount: TextView = itemView.findViewById(R.id.tvReplyCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forum_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]

        holder.tvUserInitial.text = question.userName.firstOrNull()?.uppercase() ?: "U"
        holder.tvUserName.text = question.userName
        holder.tvTimestamp.text = getTimeAgo(question.timestamp)
        holder.tvQuestion.text = question.question
        holder.tvDescription.text = question.description.ifEmpty { "No description" }
        holder.tvLikeCount.text = question.likeCount.toString()
        holder.tvDislikeCount.text = question.dislikeCount.toString()
        holder.tvReplyCount.text = "${question.replyCount} ${if (question.replyCount == 1) "reply" else "replies"}"

        val userLiked = question.likedBy.containsKey(currentUserId) && question.likedBy[currentUserId] == true
        val userDisliked = question.dislikedBy.containsKey(currentUserId) && question.dislikedBy[currentUserId] == true

        if (userLiked) {
            holder.btnLike.setImageResource(R.drawable.ic_like_filled)
            holder.btnLike.setColorFilter(Color.parseColor("#4CAF50"))
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_like_outline)
            holder.btnLike.setColorFilter(Color.parseColor("#9E9E9E"))
        }

        if (userDisliked) {
            holder.btnDislike.setImageResource(R.drawable.ic_dislike_filled)
            holder.btnDislike.setColorFilter(Color.parseColor("#F44336"))
        } else {
            holder.btnDislike.setImageResource(R.drawable.ic_dislike_outline)
            holder.btnDislike.setColorFilter(Color.parseColor("#9E9E9E"))
        }

        holder.cardQuestion.setOnClickListener {
            onQuestionClick(question)
        }

        holder.btnLike.setOnClickListener {
            onLikeClick(question)
        }

        holder.btnDislike.setOnClickListener {
            onDislikeClick(question)
        }
    }

    override fun getItemCount() = questions.size

    fun updateQuestions(newQuestions: List<ForumQuestion>) {
        questions = newQuestions
        notifyDataSetChanged()
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


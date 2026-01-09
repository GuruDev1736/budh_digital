package com.naturexpresscargo.pressapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.naturexpresscargo.pressapp.R
import com.naturexpresscargo.pressapp.models.ForumReply
import java.text.SimpleDateFormat
import java.util.*

class ForumReplyAdapter(
    private var replies: List<ForumReply>,
    private val onLikeClick: (ForumReply) -> Unit
) : RecyclerView.Adapter<ForumReplyAdapter.ReplyViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserInitial: TextView = itemView.findViewById(R.id.tvUserInitial)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val tvReply: TextView = itemView.findViewById(R.id.tvReply)
        val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tvLikeCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forum_reply, parent, false)
        return ReplyViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        val reply = replies[position]

        holder.tvUserInitial.text = reply.userName.firstOrNull()?.uppercase() ?: "U"
        holder.tvUserName.text = reply.userName
        holder.tvTimestamp.text = getTimeAgo(reply.timestamp)
        holder.tvReply.text = reply.reply
        holder.tvLikeCount.text = reply.likeCount.toString()

        val userLiked = reply.likedBy.containsKey(currentUserId) && reply.likedBy[currentUserId] == true

        if (userLiked) {
            holder.btnLike.setImageResource(R.drawable.ic_like_filled)
            holder.btnLike.setColorFilter(Color.parseColor("#4CAF50"))
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_like_outline)
            holder.btnLike.setColorFilter(Color.parseColor("#9E9E9E"))
        }

        holder.btnLike.setOnClickListener {
            onLikeClick(reply)
        }
    }

    override fun getItemCount() = replies.size

    fun updateReplies(newReplies: List<ForumReply>) {
        replies = newReplies
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


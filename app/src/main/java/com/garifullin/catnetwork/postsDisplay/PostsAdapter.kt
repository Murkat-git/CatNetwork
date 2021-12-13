package com.garifullin.catnetwork.postsDisplay

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Layer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.garifullin.catnetwork.R
import com.garifullin.catnetwork.models.Post
import com.garifullin.catnetwork.models.User
import com.google.firebase.firestore.FirebaseFirestore

class PostsAdapter(val context: Context) : PagingDataAdapter<Post, PostsAdapter.PostsViewHolder>(PostComparator) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    //override fun getItemCount() = posts.size

    inner class PostsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(post: Post) {
            post.userReference!!.addSnapshotListener { value, error ->
                if (error != null || value == null){
                    Log.d("mytag", "Ошибка")
                    return@addSnapshotListener
                }
                val user: User? = value.toObject(User::class.java)
                itemView.findViewById<TextView>(R.id.username).text = user?.username
                itemView.findViewById<TextView>(R.id.description).text = post.description
                Glide.with(context).load(post.imgUrl).placeholder(ColorDrawable(Color.GRAY)).into(itemView.findViewById(R.id.postImage))
                Glide.with(context).load(user?.avatarUrl).placeholder(ColorDrawable(Color.GRAY)).into(itemView.findViewById(R.id.avatar))
                itemView.findViewById<TextView>(R.id.timestamp).text = DateUtils.getRelativeTimeSpanString(post.created)
                itemView.findViewById<Layer>(R.id.post_header).setOnClickListener {
                    Log.e("mytag", "ok")
                    val intent = Intent(context, ProfileActivity::class.java)
                    intent.putExtra("userUid", user!!.uid)
                    context.startActivity(intent)
                }
            }

        }
    }
}
object PostComparator : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        // Id is unique.
        return oldItem.imgUrl == newItem.imgUrl
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
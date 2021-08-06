package com.garifullin.catnetwork

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Layer
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject

class PostsAdapter(val context: Context, val posts: List<Post>, val db: FirebaseFirestore) : RecyclerView.Adapter<PostsAdapter.PostsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size

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
                Glide.with(context).load(post.imgUrl).into(itemView.findViewById(R.id.postImage))
                Glide.with(context).load(user?.avatarUrl).into(itemView.findViewById(R.id.avatar))
                itemView.findViewById<TextView>(R.id.timestamp).text = DateUtils.getRelativeTimeSpanString(post.created)
                itemView.findViewById<Layer>(R.id.post_header).setOnClickListener {
                    Log.e("mytag", "ok")
                    var intent: Intent = Intent(context, ProfileActivity::class.java)
                    intent.putExtra("userUid", user!!.uid)
                    context.startActivity(intent)
                }
            }

        }
    }
}
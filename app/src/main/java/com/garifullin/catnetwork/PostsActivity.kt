package com.garifullin.catnetwork

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.math.BigInteger


class PostsActivity : AppCompatActivity() {
    lateinit var db: FirebaseFirestore
    lateinit var posts: MutableList<Post>
    lateinit var adapter: PostsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_CatNetwork)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        db = Firebase.firestore
        posts = mutableListOf()
        val rv = findViewById<RecyclerView>(R.id.rvPosts)
        adapter = PostsAdapter(this, posts, db)

        val query: Query = FirebaseFirestore.getInstance()
            .collection("posts")
            .orderBy("created")
            .limit(20)

        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)

        query.addSnapshotListener { value, error ->
            if (error != null || value == null){
                Log.d("mytag", "Ошибка")
                return@addSnapshotListener
            }
            Log.e("mytag", value.documents.toString())
            val postList = value.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
        }
    }
}
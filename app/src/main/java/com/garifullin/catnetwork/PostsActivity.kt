package com.garifullin.catnetwork

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.math.BigInteger


class PostsActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var posts: MutableList<Post>
    lateinit var adapter: PostsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_CatNetwork)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        posts = mutableListOf()
        val rv = findViewById<RecyclerView>(R.id.rvPosts)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            startActivity(Intent(this, CreatePost::class.java))
        }
        adapter = PostsAdapter(this, posts, db)

        val query: Query = FirebaseFirestore.getInstance()
            .collection("posts")
            .orderBy("created", Query.Direction.DESCENDING)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.profile){
            var intent: Intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userUid", auth.currentUser!!.uid)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}
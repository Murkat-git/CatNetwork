package com.garifullin.catnetwork

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {
    lateinit var db: FirebaseFirestore
    lateinit var posts: MutableList<Post>
    lateinit var adapter: PostsAdapter
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        db = Firebase.firestore
        auth = Firebase.auth
        val user: DocumentReference = db.collection("users").document(auth.currentUser?.uid.toString())
        posts = mutableListOf()
        val rv = findViewById<RecyclerView>(R.id.rvProfile)
        adapter = PostsAdapter(this, posts, db)

        val query: Query = FirebaseFirestore.getInstance()
            .collection("posts")
            .orderBy("created", Query.Direction.DESCENDING)
            .whereEqualTo("userReference", user)
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
        menuInflater.inflate(R.menu.menu_profile, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.signout){
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
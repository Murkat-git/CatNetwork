package com.garifullin.catnetwork.postsDisplay

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garifullin.catnetwork.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class PostsActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var adapter: PostsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_CatNetwork)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        val query: Query = FirebaseFirestore.getInstance()
            .collection("posts")
            .orderBy("created", Query.Direction.DESCENDING)
            .limit(5)

        val flow = Pager(PagingConfig(pageSize = 5)){
            PostPagingSource(query = query)
        }.flow
        val rv = findViewById<RecyclerView>(R.id.rvPosts)
        adapter = PostsAdapter(this)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            startActivity(Intent(this, CreatePost::class.java))
        }

        lifecycleScope.launch {
            flow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile -> {
                var intent: Intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("userUid", auth.currentUser!!.uid)
                startActivity(intent)
            }
            R.id.breeds -> {
                startActivity(Intent(this, BreedOnlyActivity::class.java))
            }
            R.id.refresh -> {
                recreate()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
package com.garifullin.catnetwork.postsDisplay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garifullin.catnetwork.*
import com.garifullin.catnetwork.models.Post
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.properties.Delegates


class PostsActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var posts: MutableList<Post>
    lateinit var adapter: PostsAdapter
    lateinit var lastItem: DocumentSnapshot
    var isLoading by Delegates.notNull<Boolean>()
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
            .limit(5)

        getData(query)

        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && !isLoading){
                    isLoading = true
                    Log.e("mytag", "About to empty")
                    val pagingQuery: Query = FirebaseFirestore.getInstance()
                        .collection("posts")
                        .orderBy("created", Query.Direction.DESCENDING)
                        .startAfter(lastItem)
                        .limit(5)
                    Log.e("mytag", lastItem.toString())
                    getData(pagingQuery)
                }
            }

        })

    }

    private fun getData(query: Query){
        query.get().addOnSuccessListener { value ->
            if (value.isEmpty){
                return@addOnSuccessListener
            }
            //Log.e("mytag", value.toString())
            lastItem = value.documents.last()
            //Log.e("mytag", value.documents.toString())
            val postList = value.toObjects(Post::class.java)
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
            isLoading = false
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
        else if (item.itemId == R.id.breeds){
            startActivity(Intent(this, BreedOnlyActivity::class.java))
        }
        else if (item.itemId == R.id.refresh){
            recreate()
        }
        return super.onOptionsItemSelected(item)
    }
}
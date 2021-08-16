package com.garifullin.catnetwork.postsDisplay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.garifullin.catnetwork.*
import com.garifullin.catnetwork.R
import com.garifullin.catnetwork.accountRelated.SettingsActivity
import com.garifullin.catnetwork.models.Post
import com.garifullin.catnetwork.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.properties.Delegates

class ProfileActivity : AppCompatActivity() {
    lateinit var db: FirebaseFirestore
    lateinit var posts: MutableList<Post>
    lateinit var adapter: PostsAdapter
    lateinit var rv: RecyclerView

    lateinit var uid: String
    private lateinit var auth: FirebaseAuth
    lateinit var lastItem: DocumentSnapshot
    lateinit var userRef: DocumentReference
    var isLoading by Delegates.notNull<Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        initialize()

        val query: Query = FirebaseFirestore.getInstance()
            .collection("posts")
            .orderBy("created", Query.Direction.DESCENDING)
            .whereEqualTo("userReference", userRef)
            .limit(5)

        getData(query)

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && !isLoading){
                    isLoading = true
                    Log.e("mytag", "About to empty")
                    val pagingQuery: Query = FirebaseFirestore.getInstance()
                        .collection("posts")
                        .orderBy("created", Query.Direction.DESCENDING)
                        .whereEqualTo("userReference", userRef)
                        .startAfter(lastItem)
                        .limit(5)
                    Log.e("mytag", lastItem.toString())
                    getData(pagingQuery)
                }
            }

        })
    }

    private fun initialize() {
        db = Firebase.firestore
        auth = Firebase.auth
        uid = intent.getStringExtra("userUid").toString()
        userRef = db.collection("users").document(uid)
        userRef.addSnapshotListener { value, error ->
            if (error != null || value == null){
                Log.d("mytag", "Ошибка")
                return@addSnapshotListener
            }
            val user: User = value.toObject(User::class.java)!!
            Glide.with(this).load(user.avatarUrl).into(findViewById(R.id.profileAvatar))
            findViewById<TextView>(R.id.profileUsername).text = user.username
        }
        posts = mutableListOf()
        rv = findViewById<RecyclerView>(R.id.rvProfile)
        adapter = PostsAdapter(this, posts, db)

        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)
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
        if (uid == auth.currentUser!!.uid) {
            menuInflater.inflate(R.menu.menu_profile, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == 16908332) {
            finish()
        }
        else if (id == R.id.settings){
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        else if (id == R.id.refresh){
            recreate()
        }
        return super.onOptionsItemSelected(item)
    }
}
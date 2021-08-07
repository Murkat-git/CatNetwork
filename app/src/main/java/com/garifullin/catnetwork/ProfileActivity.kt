package com.garifullin.catnetwork

import android.app.TaskStackBuilder
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
    lateinit var uid: String
    private lateinit var auth: FirebaseAuth
    lateinit var lastItem: DocumentSnapshot
    var isLoading by Delegates.notNull<Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        db = Firebase.firestore
        auth = Firebase.auth
        uid = intent.getStringExtra("userUid").toString()
        val userRef: DocumentReference = db.collection("users").document(uid)
        userRef.addSnapshotListener { value, error ->
            if (error != null || value == null){
                Log.d("mytag", "Ошибка")
                return@addSnapshotListener
            }
            val user: User? = value.toObject(User::class.java)
            if (user != null) {
                Glide.with(this).load(user.avatarUrl).into(findViewById(R.id.profileAvatar))
                findViewById<TextView>(R.id.profileUsername).text = user.username
            }
        }
        posts = mutableListOf()
        val rv = findViewById<RecyclerView>(R.id.rvProfile)
        adapter = PostsAdapter(this, posts, db)

        val query: Query = FirebaseFirestore.getInstance()
            .collection("posts")
            .orderBy("created", Query.Direction.DESCENDING)
            .whereEqualTo("userReference", userRef)
            .limit(5)


        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)

//        query.get().addOnCompleteListener { task ->
//            if (task.isSuccessful){
//                lastItem = task.result.documents.last()
//                Log.e("mytag", task.result.documents.toString())
//                val postList = task.result.toObjects(Post::class.java)
//                posts.addAll(postList)
//                adapter.notifyDataSetChanged()
//            }
//            else{
//                Log.d("mytag", task.exception.toString())
//            }
//        }

        query.get().addOnSuccessListener { value ->
            lastItem = value.documents.last()
            Log.e("mytag", value.documents.toString())
            val postList = value.toObjects(Post::class.java)
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
        }
        isLoading = false

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
                    pagingQuery.get().addOnSuccessListener { value ->
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
            }

        })
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
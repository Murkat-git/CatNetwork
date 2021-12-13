package com.garifullin.catnetwork.postsDisplay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.garifullin.catnetwork.*
import com.garifullin.catnetwork.R
import com.garifullin.catnetwork.accountRelated.SettingsActivity
import com.garifullin.catnetwork.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    lateinit var db: FirebaseFirestore
    lateinit var adapter: PostsAdapter
    lateinit var rv: RecyclerView

    lateinit var uid: String
    private lateinit var auth: FirebaseAuth
    lateinit var userRef: DocumentReference
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

        val flow = Pager(PagingConfig(pageSize = 5)){
            PostPagingSource(query = query)
        }.flow

        lifecycleScope.launch {
            flow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
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
        rv = findViewById(R.id.rvProfile)
        adapter = PostsAdapter(this)
        rv.adapter = adapter
        rv.layoutManager = LinearLayoutManager(this)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (uid == auth.currentUser!!.uid) {
            menuInflater.inflate(R.menu.menu_profile, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            16908332 -> {
                finish()
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.refresh -> {
                recreate()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
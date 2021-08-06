package com.garifullin.catnetwork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BreedOnlyActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var posts: MutableList<Post>
    lateinit var postsAdapter: PostsAdapter
    lateinit var breedList: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breed_only)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        posts = mutableListOf()
        val rv = findViewById<RecyclerView>(R.id.rv)

        postsAdapter = PostsAdapter(this, posts, db)

        rv.adapter = postsAdapter
        rv.layoutManager = LinearLayoutManager(this)

        val breedSpinner: Spinner = findViewById(R.id.breeds)
        breedList = resources.getStringArray(R.array.breeds).toList()
        var adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this, R.array.breeds, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        breedSpinner.adapter = adapter


        breedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("mytag", "Ошибка выбора породы")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                posts.clear()
                postsAdapter.notifyDataSetChanged()
                val query: Query = FirebaseFirestore.getInstance()
                    .collection("posts")
                    .orderBy("created", Query.Direction.DESCENDING)
                    .whereEqualTo("breed", breedList[position])
                    .limit(20)

                query.addSnapshotListener { value, error ->
                    if (error != null || value == null){
                        Log.d("mytag", "Ошибка")
                        return@addSnapshotListener
                    }
                    Log.e("mytag", value.documents.toString())
                    val postList = value.toObjects(Post::class.java)
                    posts.clear()
                    posts.addAll(postList)
                    postsAdapter.notifyDataSetChanged()
                }
            }

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == 16908332) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
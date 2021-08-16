package com.garifullin.catnetwork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.properties.Delegates

class BreedOnlyActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var posts: MutableList<Post>
    lateinit var postsAdapter: PostsAdapter
    lateinit var rv: RecyclerView
    lateinit var breedList: List<String>
    lateinit var lastItem: DocumentSnapshot
    lateinit var currentBreed: String
    var isLoading by Delegates.notNull<Boolean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breed_only)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        initialize()

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
                currentBreed = breedList[position]
                Log.e("mytag", currentBreed)
                val query: Query = FirebaseFirestore.getInstance()
                    .collection("posts")
                    .whereEqualTo("breed", breedList[position])
                    .orderBy("created", Query.Direction.DESCENDING)
                    .limit(5)
                getData(query)
                isLoading = false
            }

        }

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && !isLoading){
                    isLoading = true
                    Log.e("mytag", "About to empty")
                    val pagingQuery: Query = FirebaseFirestore.getInstance()
                        .collection("posts")
                        .orderBy("created", Query.Direction.DESCENDING)
                        .whereEqualTo("breed", currentBreed)
                        .startAfter(lastItem)
                        .limit(5)
                    Log.e("mytag", lastItem.toString())

                }
            }

        })
    }

    private fun initialize() {
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        posts = mutableListOf()
        rv = findViewById<RecyclerView>(R.id.rv)

        postsAdapter = PostsAdapter(this, posts, db)

        rv.adapter = postsAdapter
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
            postsAdapter.notifyDataSetChanged()
            isLoading = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_breeds_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == 16908332) {
            finish()
        }
        else if(id == R.id.refresh){
            recreate()
        }
        return super.onOptionsItemSelected(item)
    }
}
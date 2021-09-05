package com.garifullin.catnetwork.postsDisplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.garifullin.catnetwork.R
import com.garifullin.catnetwork.models.SpinnerItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BreedOnlyActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var postsAdapter: PostsAdapter
    lateinit var rv: RecyclerView
    lateinit var breedKeys: List<String>
    lateinit var currentBreed: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breed_only)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        initialize()

        val breedSpinner: Spinner = findViewById(R.id.breeds)
        breedKeys = resources.getStringArray(R.array.breeds_keys).toList()
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this,
            R.array.breeds, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        breedSpinner.adapter = adapter

        breedSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("mytag", "Ошибка выбора породы")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentBreed = breedKeys[position]
                Log.e("mytag", currentBreed)
                val query: Query = FirebaseFirestore.getInstance()
                    .collection("posts")
                    .whereEqualTo("breed", currentBreed)
                    .orderBy("created", Query.Direction.DESCENDING)
                    .limit(5)

                val flow = Pager(PagingConfig(pageSize = 5)){
                    PostPagingSource(query = query)
                }.flow

                lifecycleScope.launch {
                    flow.collectLatest { pagingData ->
                        postsAdapter.submitData(pagingData)
                    }
                }
            }

        }
    }

    private fun initialize() {
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        rv = findViewById(R.id.rv)

        postsAdapter = PostsAdapter(this)

        rv.adapter = postsAdapter
        rv.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_breeds_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            16908332 -> {
                finish()
            }
            R.id.refresh -> {
                recreate()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
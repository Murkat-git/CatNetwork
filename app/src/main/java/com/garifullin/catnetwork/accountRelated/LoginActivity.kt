package com.garifullin.catnetwork.accountRelated

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.garifullin.catnetwork.R
import com.garifullin.catnetwork.postsDisplay.PostsActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        initialize()

        val email: EditText = findViewById(R.id.email)
        val password: EditText = findViewById(R.id.password)
        val login: Button = findViewById(R.id.login)
        login.setOnClickListener(View.OnClickListener { View ->
            login.isEnabled = false
            if (email.text.isBlank() or password.text.isBlank()){
                Toast.makeText(this, "Поля не должны быть пустыми", Toast.LENGTH_LONG).show()
                return@OnClickListener
            }
            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener {
                if (it.isSuccessful){
                    updateUI(auth.currentUser)
                }
                login.isEnabled = true
            }
        })
    }

    private fun initialize() {
        auth = Firebase.auth
        if (auth.currentUser != null) {
            startActivity(Intent(this, PostsActivity::class.java))
            finish()
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null){
            finish()
            startActivity(Intent(this, PostsActivity::class.java))
        }
    }

    fun doRegister(view: View){
        finish()
        startActivity(Intent(this, RegisterActivity::class.java))
    }


}
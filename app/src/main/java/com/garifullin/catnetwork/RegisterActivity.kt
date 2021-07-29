package com.garifullin.catnetwork

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth

        val username: EditText = findViewById(R.id.username)
        val email: EditText = findViewById(R.id.email)
        val password: EditText = findViewById(R.id.password)
        val login: Button = findViewById(R.id.login)

        login.setOnClickListener(View.OnClickListener { View ->
            login.isEnabled = false
            if (username.text.isBlank() or email.text.isBlank() or password.text.isBlank()){
                Toast.makeText(this, "Поля не должны быть пустыми", Toast.LENGTH_LONG).show()
            }
            else{
                auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnSuccessListener {
                        updateUI(auth.currentUser)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG)
                    }
            }
            login.isEnabled = true
        })
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null){
            finish()
            startActivity(Intent(this, PostsActivity::class.java))
        }
    }
}
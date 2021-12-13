package com.garifullin.catnetwork.accountRelated

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.garifullin.catnetwork.R
import com.garifullin.catnetwork.models.User
import com.garifullin.catnetwork.postsDisplay.PostsActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.roger.catloadinglibrary.CatLoadingView

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var storageRef: StorageReference
    lateinit var pickedAvatarUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        val catLoadingView = CatLoadingView()
        val username: EditText = findViewById(R.id.username)
        val email: EditText = findViewById(R.id.email)
        val password: EditText = findViewById(R.id.password)
        val login: Button = findViewById(R.id.login)
        login.setOnClickListener(View.OnClickListener { View ->
            login.isEnabled = false
            if (username.text.isBlank() or email.text.isBlank() or password.text.isBlank() or !this::pickedAvatarUri.isInitialized){
                Toast.makeText(this, "Поля не должны быть пустыми", Toast.LENGTH_LONG).show()
            }
            else{
                auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnSuccessListener {
                        try {
                            catLoadingView.show(supportFragmentManager, "")
                            catLoadingView.setText("Регистрация...")
                            val ref = storageRef.child("avatars/" + auth.currentUser?.uid)
                            val uploadTask = ref.putFile(pickedAvatarUri)
                            val urlTask = uploadTask.continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let {
                                        throw it
                                    }
                                }
                                ref.downloadUrl
                            }
                            urlTask.addOnCompleteListener { task ->
                                catLoadingView.dialog!!.cancel()
                                if (task.isSuccessful) {
                                    val downloadUri = task.result
                                    val createdUser = User()
                                    createdUser.uid = auth.currentUser!!.uid
                                    createdUser.username = username.text.toString()
                                    createdUser.avatarUrl = downloadUri.toString()
                                    db.collection("users").document(auth.currentUser?.uid.toString()).set(createdUser)
                                    updateUI(auth.currentUser)
                                }
                            }

                        }
                        catch (e: Exception){
                            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                        }

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
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

    fun pickAvatar(view: View) {
        ImagePicker.with(this)
            .galleryOnly()
            .cropSquare()   			//Crop image(Optional), Check Customization for more option
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(512, 512)	//Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val uri: Uri = data?.data!!
            pickedAvatarUri = uri
            findViewById<ImageButton>(R.id.avatar).setImageURI(uri)
            // Use Uri object instead of File to avoid storage permissions
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

}
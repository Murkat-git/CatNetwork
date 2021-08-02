package com.garifullin.catnetwork

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.roger.catloadinglibrary.CatLoadingView
import java.util.*

class CreatePost : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth;
    lateinit var db: FirebaseFirestore
    lateinit var storageRef: StorageReference
    lateinit var pickedImgUri: Uri
    lateinit var catLoadingView: CatLoadingView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        catLoadingView = CatLoadingView()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        ImagePicker.with(this)
            .galleryOnly()
            .cropSquare()   			//Crop image(Optional), Check Customization for more option
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val uri: Uri = data?.data!!
            pickedImgUri = uri
            findViewById<ImageView>(R.id.pickImage).setImageURI(uri)
            // Use Uri object instead of File to avoid storage permissions
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create_post, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.home) {
            finish()
        }
        else if (id == R.id.submit){
            if (this::pickedImgUri.isInitialized){
                var text = findViewById<EditText>(R.id.description).text.toString()
                catLoadingView.show(supportFragmentManager, "")
                catLoadingView.setText("Загрузка")
                val time = System.currentTimeMillis()
                val ref = storageRef.child("postImages/" + auth.currentUser!!.uid + time)
                val uploadTask = ref.putFile(pickedImgUri)
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
                        val createdPost: Post = Post()
                        createdPost.imgUrl = downloadUri.toString()
                        createdPost.userReference = db.collection("users").document(auth.currentUser!!.uid)
                        createdPost.description = text
                        createdPost.created = time
                        db.collection("posts").add(createdPost)
                        finish()
                    }
                }

            }
            else{
                Toast.makeText(this, "Выберите изображение", Toast.LENGTH_LONG).show()
            }

        }

        return super.onOptionsItemSelected(item)
    }

    fun pickImg(view: View) {
        ImagePicker.with(this)
            .galleryOnly()
            .cropSquare()   			//Crop image(Optional), Check Customization for more option
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }
}
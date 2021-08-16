package com.garifullin.catnetwork

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.garifullin.catnetwork.models.Post
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.roger.catloadinglibrary.CatLoadingView
import java.io.IOException

class CreatePost : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth;
    lateinit var db: FirebaseFirestore
    lateinit var storageRef: StorageReference
    lateinit var pickedImgUri: Uri
    lateinit var predictedBreed: String
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
        pickImage()
    }

    private fun pickImage() {
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
            var catOnImage = false
            val image: InputImage
            try {
                image = InputImage.fromFilePath(this, uri)
                val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        for (i in labels){
                            Log.d("mytag",i.text)
                            if (i.text.equals("Cat")){
                                pickedImgUri = uri
                                findViewById<ImageView>(R.id.pickImage).setImageURI(uri)
                                catOnImage = true
                                Log.d("mytag", catOnImage.toString())
                                break
                            }
                        }
                        if (catOnImage){
                            val localModel = LocalModel.Builder()
                                .setAssetFilePath("model.tflite")
                                .build()

                            val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
                                .setConfidenceThreshold(0.3f)
                                .setMaxResultCount(1)
                                .build()

                            val Customlabeler = ImageLabeling.getClient(customImageLabelerOptions)
                            Customlabeler.process(image)
                                .addOnSuccessListener { labels ->
                                    if (labels.size > 0){
                                        predictedBreed = labels.get(0).text
                                    }
                                    else{
                                        predictedBreed = "Unlabeled"
                                    }
                                }
                                .addOnFailureListener { e ->
                                    predictedBreed = "Unlabeled"
                                }

                        }
                        else{
                            Toast.makeText(this, "На фотографии не обнаружен кот", Toast.LENGTH_LONG).show()
                            pickImage()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        // ...
                    }



            } catch (e: IOException) {
                e.printStackTrace()
            }
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
        if (id == 16908332) {
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
                        createdPost.breed = predictedBreed
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
        pickImage()
    }
}
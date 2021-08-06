package com.garifullin.catnetwork

import android.app.Activity
import android.app.AlertDialog
import android.app.TaskStackBuilder
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.roger.catloadinglibrary.CatLoadingView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)


        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        lateinit var auth: FirebaseAuth
        lateinit var currentUserDocRef: DocumentReference
        lateinit var storageRef: StorageReference
        lateinit var currentUser: FirebaseUser
        lateinit var catLoadingView: CatLoadingView
        lateinit var password: String
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            catLoadingView = CatLoadingView()
            catLoadingView.setText("Обновление")

            val inputEditTextField = EditText(requireActivity())
            val reAuthDialog = AlertDialog.Builder(requireContext())
                .setTitle("Введите пароль")
                .setView(inputEditTextField)
                .setNegativeButton("Отмена", null)
                .create()

            auth = FirebaseAuth.getInstance()
            currentUser = auth.currentUser!!
            currentUserDocRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
            storageRef = FirebaseStorage.getInstance().reference

            var changeUsernamePref = findPreference<EditTextPreference>("change_username")!!
            currentUserDocRef.get().addOnCompleteListener { task ->
                changeUsernamePref.text = task.result.toObject(User::class.java)!!.username
            }
            changeUsernamePref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                catLoadingView.show(parentFragmentManager, "")
                currentUserDocRef.update("username", newValue.toString()).addOnCompleteListener {
                    catLoadingView.dialog!!.cancel()
                }
                true
            }
            var changeEmailPref = findPreference<EditTextPreference>("change_email")!!
            changeEmailPref.text = currentUser.email
            changeEmailPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                reAuthDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ок", DialogInterface.OnClickListener { dialogInterface, i ->
                    val credential = EmailAuthProvider
                        .getCredential(currentUser.email.toString(), inputEditTextField.text.toString())
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { it ->
                        if (it.isSuccessful){
                            catLoadingView.show(parentFragmentManager, "")
                            currentUser.updateEmail(newValue.toString()).addOnCompleteListener { task ->
                                catLoadingView.dialog!!.cancel()
                                if(!task.isSuccessful) {
                                    Log.e("mytag", task.exception.toString())
                                }
                            }
                        }
                        else{
                            Toast.makeText(activity, "Неправильный пароль", Toast.LENGTH_LONG).show()
                        }
                    }


                })
                reAuthDialog.show()

                true
            }

            var changePasswordPref = findPreference<EditTextPreference>("change_password")!!
            changePasswordPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                reAuthDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ок", DialogInterface.OnClickListener { dialogInterface, i ->
                    val credential = EmailAuthProvider
                        .getCredential(currentUser.email.toString(), inputEditTextField.text.toString())
                    auth.signInWithCredential(credential).addOnCompleteListener { it ->
                        if (it.isSuccessful){
                            catLoadingView.show(parentFragmentManager, "")
                            currentUser.updatePassword(newValue.toString()).addOnCompleteListener {
                                catLoadingView.dialog!!.cancel()
                            }
                        }
                        else{
                            Toast.makeText(activity, "Неправильный пароль", Toast.LENGTH_LONG).show()
                        }
                    }


                })
                reAuthDialog.show()
                true
            }

            var changeAvatarPref = findPreference<Preference>("change_avatar")!!
            changeAvatarPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                ImagePicker.with(this)
                    .galleryOnly()
                    .cropSquare()   			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(512, 512)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start()
                true
            }

            var exitProfilePref = findPreference<Preference>("exit_profile")!!
            exitProfilePref.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
                auth.signOut()
                TaskStackBuilder.create(activity)
                    .addNextIntent(Intent(activity, LoginActivity::class.java))
                    .startActivities()
                true
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!
                catLoadingView.show(parentFragmentManager, "")
                val ref = storageRef.child("avatars/" + currentUser.uid)
                val uploadTask = ref.putFile(uri)
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
                        currentUserDocRef.update("avatarUrl", downloadUri.toString())
                        Log.e("mytag", downloadUri.toString())
                    }
                }
                // Use Uri object instead of File to avoid storage permissions
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(activity, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.e("mytag", item.itemId.toString())
        if (item.itemId == 16908332){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


}
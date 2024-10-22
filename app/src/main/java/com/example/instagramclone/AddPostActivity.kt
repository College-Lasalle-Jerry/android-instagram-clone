package com.example.instagramclone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddPostActivity : AppCompatActivity() {


    private lateinit var descriptionEditText: EditText
    private lateinit var imageAddedImageView: ImageView
    private lateinit var closeImageView: ImageView
    private lateinit var postTextView: TextView

    // image -> uri, url
    private lateinit var imageUri: Uri
    private lateinit var imageUrl: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_post)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        descriptionEditText = findViewById(R.id.description_edittext)
        imageAddedImageView = findViewById(R.id.image_added)
        closeImageView = findViewById(R.id.close)
        postTextView = findViewById(R.id.post_textview)

        closeImageView.setOnClickListener {
            finish() // this will finish the activity
        }

        postTextView.setOnClickListener {
            // we will writing some code.
            saveData()

        }

        // Step 1
        val activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    imageUri = data?.data!!
                    imageAddedImageView.setImageURI(imageUri)
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                }
            }
// Step 2
        imageAddedImageView.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            activityResultLauncher.launch(photoPicker) // this will get the image from the files.
        }
    }

    // step 3
    private fun saveData() {
        // is to upload the image to firebase storage.
        // storage ref to firebase
        val storageReference = FirebaseStorage.getInstance().reference.child("Images").child(imageUri?.lastPathSegment?:"")
        val builder = AlertDialog.Builder(this@AddPostActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()

        dialog.show() // to show the dialog box

        // uploading the file
        storageReference.putFile(imageUri!!).addOnSuccessListener {
                taskSnapshot ->

            val uriTask = taskSnapshot.storage.downloadUrl
            while(!uriTask.isComplete);
            val urlImage = uriTask.result
            imageUrl = urlImage.toString()?: ""
            uploadData()
            dialog.dismiss()
        }.addOnFailureListener{
            dialog.dismiss()
        }
    }

    // step 4
    private fun uploadData() {
        // is to upload the post details to firebase realtime database.

        val description = descriptionEditText.text.toString().trim()
        val reference = FirebaseDatabase.getInstance().getReference("PostsAndroid")

        val postId = reference.push().key ?: return // if it is null, return
        val hashMap = hashMapOf(
            "postid" to postId,
            "postimage" to imageUrl,
            "description" to description,
            "publisher" to FirebaseAuth.getInstance().currentUser?.uid // logged in user.
        )

        reference.child(postId).setValue(hashMap)
        startActivity(Intent(this, MainActivity::class.java))
        finish() // close the post activity.
    }
}
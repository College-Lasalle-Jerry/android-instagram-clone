package com.example.instagramclone

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.instagramclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
//import com.rengwuxian.materialedittext.MaterialEditText
import com.squareup.picasso.Picasso

class EditProfileActivity : AppCompatActivity() {

    private lateinit var close: ImageView
    private lateinit var image_profile: ImageView
    private lateinit var save: TextView
    private lateinit var tv_change: TextView
    private lateinit var fullname: EditText
    private lateinit var username: EditText
    private lateinit var bio: EditText

    private lateinit var firebaseUser: FirebaseUser

    private lateinit var imageUri: Uri

    private lateinit var storageRef: StorageReference

    private lateinit var imageUrl: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        close = findViewById(R.id.close)
        image_profile = findViewById(R.id.image_profile)
        save = findViewById(R.id.save)
        tv_change = findViewById(R.id.tv_change)
        fullname = findViewById(R.id.fullname)
        username = findViewById(R.id.username)
        username.isEnabled = false
        bio = findViewById(R.id.bio)

        Toast.makeText(
            this@EditProfileActivity,
            "You can't change your username! Contact admin.",
            Toast.LENGTH_SHORT
        ).show()
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageRef = FirebaseStorage.getInstance().getReference("Images")

        val reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: User? = dataSnapshot.getValue(User::class.java)
                fullname.setText(user!!.name)
                username.setText(user.username)
                bio.setText(user.bio)
                Picasso.get().load(user.imageurl).placeholder(R.drawable.ic_image_added)
                    .into(image_profile)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FIREBASE USER ERROR", databaseError.message)
            }
        })

        close.setOnClickListener { finish() }

        val activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    imageUri = data?.data!!
                    image_profile.setImageURI(imageUri)

                    saveData()
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                }
            }

        tv_change.setOnClickListener {
            val photoPicker =
                Intent(Intent.ACTION_PICK) // opens the interface to pick the image from the gallery.
            photoPicker.setType("image/*")
            activityResultLauncher.launch(photoPicker)
        }

        save.setOnClickListener {
            updateProfile(fullname.text.toString().trim { it <= ' ' },
                username.text.toString(),
                bio.text.toString().trim { it <= ' ' })
        }
    }

    private fun saveData() {
        val storageReference = FirebaseStorage.getInstance().reference.child("Images")
            .child(imageUri.lastPathSegment!!)

        val builder = AlertDialog.Builder(this@EditProfileActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        storageReference.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
            val uriTask = taskSnapshot.storage.downloadUrl
            while (!uriTask.isComplete);
            val urlImage = uriTask.result
            imageUrl = if (urlImage.toString().isEmpty()) "" else urlImage.toString()
            uploadData()
            dialog.dismiss()
            finish()
        }.addOnFailureListener { dialog.dismiss() }
    }

    private fun uploadData() {
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)

        val hashMap = HashMap<String, Any>()
        hashMap["imageurl"] = imageUrl
        reference.updateChildren(hashMap)
        startActivity(Intent(this@EditProfileActivity, MainActivity::class.java))
        finish()
    }

    private fun updateProfile(fullname: String, username: String, bio: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)

        val hashMap = java.util.HashMap<String, Any>()
        hashMap["fullname"] = fullname
        hashMap["username"] = username
        hashMap["bio"] = bio

        reference.updateChildren(hashMap)
    }
}
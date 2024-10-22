package com.example.instagramclone

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginTextView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var mRootRef: DatabaseReference
    private lateinit var pd: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        auth = Firebase.auth
        mRootRef = FirebaseDatabase.getInstance().reference
        // initialise the views
        usernameEditText = findViewById(R.id.edittext_username)
        nameEditText = findViewById(R.id.edittext_name)
        emailEditText = findViewById(R.id.edittext_register_email)
        passwordEditText = findViewById(R.id.edittext_register_password)
        registerButton = findViewById(R.id.button_register)
        loginTextView = findViewById(R.id.textview_login)

        pd = ProgressDialog(this)


        loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(
                    password
                )
            ) {
                Toast.makeText(this, "Empty Credentials", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password too Short... (6 characters min)", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // register user functionality.
                registerUser(username, name, email, password)
            }
        }
    }

    private fun registerUser(
        username: String,
        name: String,
        email: String,
        password: String
    ) {
        pd.setMessage("Please wait...")
        pd.show()

        // register user to firebase.
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // user is successfully registered.
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                pd.dismiss()
                // we will add the user object in the database as well.
                // realtime database.
                // hashmap
                val user = hashMapOf(
                    "username" to username,
                    "name" to name,
                    "email" to email,
                    "password" to password,
                    "id" to auth.currentUser?.uid,
                    "bio" to "",
                    "imageurl" to "default"
                )
                mRootRef.child("Users").child(auth.currentUser!!.uid).setValue(user)
                    .addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            Toast.makeText(this, "User added to database", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish() // we cannot go back to the Register page.
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(
                            this,
                            "Registration Failed: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        pd.dismiss()
                    }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Registration Failed: ${exception.message}", Toast.LENGTH_SHORT)
                .show()
            pd.dismiss()
        }
    }
}
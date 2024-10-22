package com.example.instagramclone

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var pd: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        // initialise the views.
        emailEditText = findViewById(R.id.edittext_login_email)
        passwordEditText = findViewById(R.id.edittext_login_password)
        loginButton = findViewById(R.id.button_register)
        registerTextView = findViewById(R.id.textview_register)

        pd = ProgressDialog(this)
        auth = Firebase.auth

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Empty Credentials", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(
                    this,
                    "Password too Short... (6 characters min)",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // login user functionality.
                loginUser(email, password)
            }
        }

    }

    private fun loginUser(
        email: String,
        password: String
    ) {

        pd.setMessage("Please wait...")
        pd.show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    pd.dismiss()
                    // we will move to the Main Activity.
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                    finish() // we cannot go back to the login page.

                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Login Failed: ${exception.message}", Toast.LENGTH_SHORT)
                    .show()
                pd.dismiss()
            }

    }
}
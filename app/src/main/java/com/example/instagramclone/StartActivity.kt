package com.example.instagramclone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class StartActivity : AppCompatActivity() {

    private lateinit var iconImage: ImageView
    private lateinit var linearLayout: LinearLayout
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onStart() {
        super.onStart()

        // user persistance.
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // to not go back to the start activity page.
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        iconImage = findViewById(R.id.icon_image)
        linearLayout = findViewById(R.id.linear_layout)
        loginButton = findViewById(R.id.login)
        registerButton = findViewById(R.id.register)

        linearLayout.animate().alpha(0f).setDuration(10)
        val animation: TranslateAnimation = TranslateAnimation(0.0f, 0.0f, 0.0f, -1500.0f)
        animation.duration = 1000
        animation.fillAfter = false
        animation.setAnimationListener(myAnimationListener())

        iconImage.animation = animation

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    inner class myAnimationListener : AnimationListener {
        override fun onAnimationStart(animation: Animation?) {

        }

        override fun onAnimationEnd(animation: Animation?) {
            iconImage.clearAnimation()
            iconImage.setVisibility(View.INVISIBLE)
            linearLayout.animate().alpha(1f).setDuration(1000)
        }

        override fun onAnimationRepeat(animation: Animation?) {

        }

    }
}
package com.example.instagramclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.instagramclone.fragments.HomeFragment
import com.example.instagramclone.fragments.NotificationsFragment
import com.example.instagramclone.fragments.ProfileFragment
import com.example.instagramclone.fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var selectorFragment: Fragment

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        auth = FirebaseAuth.getInstance()
        selectorFragment = HomeFragment()
        loadFragment(selectorFragment)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            val menuItemId = item.itemId

            when (menuItemId) {
                R.id.nav_home -> {
                    selectorFragment = HomeFragment()
                }

                R.id.nav_search -> {
                    selectorFragment = SearchFragment()
                }

                R.id.nav_add -> {
                    // to a activity.
                    startActivity(Intent(this, AddPostActivity::class.java))
                }

                R.id.nav_heart -> {
                    selectorFragment = NotificationsFragment()
                }

                R.id.nav_profile -> {

                    val editor = baseContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit()
                    editor.putString("profileid", FirebaseAuth.getInstance().currentUser!!.uid)
                    editor.apply()

                    Log.d(
                        "AFTER INTENT",
                        baseContext.getSharedPreferences("PREFS", MODE_PRIVATE)
                            .getString("profileid", "none")!!
                    )
                    selectorFragment = ProfileFragment()
                }
            }

            loadFragment(selectorFragment)
            true // always return true
        }
        val intent = intent.extras
        if (intent != null) {
            bottomNavigationView.selectedItemId = R.id.nav_profile
            /// profile id of the user
            val profileId = intent.getString("publisherId") //

            // profile
            val sharedPreferences = getSharedPreferences(
                "PREFS",
                MODE_PRIVATE
            ) // we are using a private variable in the project
            sharedPreferences.edit().putString("profileid", profileId).apply()

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()

        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
        }





    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
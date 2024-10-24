package com.example.instagramclone

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.adapters.UserAdapter
import com.example.instagramclone.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FollowersActivity : AppCompatActivity() {

    private lateinit var id: String
    private lateinit var title: String

    private lateinit var idList: MutableList<String>

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: MutableList<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_followers)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val intent = intent
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = mutableListOf()
        userAdapter = UserAdapter(this, userList, false)
        recyclerView.adapter = userAdapter

        idList = ArrayList()

        when (title) {
            "Likes" -> getLikes()
            "Following" -> getFollowing()
            "Followers" -> getFollowers()
            "views" -> getViews()
        }


    }

    private fun getViews() {
        val reference = FirebaseDatabase.getInstance().getReference("Story")
            .child(id).child(intent.getStringExtra("storyid")!!).child("views")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                idList.clear()
                for (snapshot in dataSnapshot.children) {
                    idList.add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun getLikes() {
        val reference = FirebaseDatabase.getInstance().getReference("Likes").child(id)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                idList.clear()
                for (snapshot in dataSnapshot.children) {
                    idList.add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun getFollowing() {
        val reference =
            FirebaseDatabase.getInstance().getReference("Follow").child(id).child("following")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                idList.clear()
                for (snapshot in dataSnapshot.children) {
                    idList.add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun getFollowers() {
        val reference =
            FirebaseDatabase.getInstance().getReference("Follow").child(id).child("followers")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                idList.clear()
                for (snapshot in dataSnapshot.children) {
                    idList.add(snapshot.key!!)
                }
                showUsers()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun showUsers() {
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userList.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)
                    for (id in idList) {
                        checkNotNull(user)
                        if (user.id.equals(id)) userList.add(user)
                    }
                }

                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}
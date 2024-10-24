package com.example.instagramclone

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.adapters.CommentAdapter
import com.example.instagramclone.models.Comment
import com.example.instagramclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class CommentsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentList: MutableList<Comment>

    private lateinit var addcomment: EditText
    private lateinit var image_profile: ImageView
    private lateinit var post: TextView

    private lateinit var postid: String
    private lateinit var publisherid: String

    var firebaseUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comments)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Comments"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val intent = intent
        postid = intent.getStringExtra("postid")!!
        publisherid = intent.getStringExtra("publisherid")!!

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        commentList = mutableListOf()
        commentAdapter = CommentAdapter(this, commentList, postid)
        recyclerView.adapter = commentAdapter

        addcomment = findViewById(R.id.add_comment)
        image_profile = findViewById(R.id.image_profile)
        post = findViewById(R.id.post)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        post.setOnClickListener {
            if (TextUtils.isEmpty(addcomment.text.toString())) {
                Toast.makeText(
                    this@CommentsActivity,
                    "No comment added!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                addComment()
            }
        }

        getImage()
        readComments()
    }

    private fun addComment() {
        val reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid)

        val commentid = reference.push().key

        val hashMap = HashMap<String, Any?>()
        hashMap["comment"] = addcomment.text.toString()
        hashMap["publisher"] = firebaseUser!!.uid
        hashMap["commentid"] = commentid

        reference.child(commentid!!).setValue(hashMap)
        addNotifications()
        addcomment.setText("")
    }

    private fun addNotifications() {
        val reference =
            FirebaseDatabase.getInstance().getReference("Notifications").child(publisherid)

        val hashMap = HashMap<String, Any>()
        hashMap["userid"] = firebaseUser!!.uid
        hashMap["text"] = "commented: " + addcomment.text.toString()
        hashMap["postid"] = postid
        hashMap["ispost"] = true

        reference.push().setValue(hashMap)
    }

    private fun getImage() {
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(
            firebaseUser!!.uid
        )

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: User? = dataSnapshot.getValue(User::class.java)
                Picasso.get().load(user!!.imageurl).placeholder(R.drawable.ic_image_added)
                    .into(image_profile)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun readComments() {
        val reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                commentList.clear()

                for (snapshot in dataSnapshot.children) {
                    val comment = snapshot.getValue(Comment::class.java)
                    Log.e("COMMENT", comment!!.comment!!)
                    commentList.add(comment!!)
                }

                commentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }
}
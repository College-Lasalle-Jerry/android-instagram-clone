package com.example.instagramclone.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.EditProfileActivity
import com.example.instagramclone.FollowersActivity
import com.example.instagramclone.OptionsActivity
import com.example.instagramclone.R
import com.example.instagramclone.adapters.PhotoAdapter
import com.example.instagramclone.models.Post
import com.example.instagramclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.Collections


class ProfileFragment : Fragment() {
    private lateinit var image_profile: ImageView
    private lateinit var options: ImageView
    private lateinit var posts: TextView
    private lateinit var followers: TextView
    private lateinit var following: TextView
    private lateinit var fullname: TextView
    private lateinit var bio: TextView
    private lateinit var username: TextView
    private lateinit var myPhotos: ImageButton
    private lateinit var savedPhotos: ImageButton
    private lateinit var editprofile: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var myPhotoAdapter: PhotoAdapter
    private lateinit var postList: MutableList<Post>

    private lateinit var mySaves: MutableList<String>
    private lateinit var recyclerView_saves: RecyclerView
    private lateinit var myPhotAdapterSaves: PhotoAdapter
    private lateinit var postList_saves: MutableList<Post>

    private var firebaseUser: FirebaseUser? = null
    var profileid: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val prefs = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        Log.d("SHARED PREF START PROFILE", prefs.getString("profileid", "none")!!)
        profileid = prefs.getString("profileid", firebaseUser!!.uid)


        image_profile = view.findViewById<ImageView>(R.id.image_profile)
        options = view.findViewById(R.id.options)
        posts = view.findViewById(R.id.posts)
        followers = view.findViewById(R.id.followers)
        following = view.findViewById(R.id.following)
        fullname = view.findViewById(R.id.fullname)
        bio = view.findViewById(R.id.bio)
        myPhotos = view.findViewById<ImageButton>(R.id.my_fotos)
        username = view.findViewById(R.id.username)
        savedPhotos = view.findViewById<ImageButton>(R.id.saved_fotos)
        editprofile = view.findViewById<Button>(R.id.edit_profile)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerView.layoutManager = linearLayoutManager
        postList = mutableListOf()
        myPhotoAdapter = PhotoAdapter(container!!.context, postList)
        recyclerView.adapter = myPhotoAdapter


        recyclerView_saves = view.findViewById<RecyclerView>(R.id.recycler_view_save)
        recyclerView_saves.setHasFixedSize(true)
        val linearLayoutManager1: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerView_saves.setLayoutManager(linearLayoutManager1)
        postList_saves = mutableListOf()
        myPhotAdapterSaves = PhotoAdapter(container.context, postList_saves)
        recyclerView_saves.setAdapter(myPhotAdapterSaves)


        recyclerView.visibility = View.VISIBLE
        recyclerView_saves.visibility = View.GONE

        userInfo()
        getFollowers()
        getNrPosts()
        myFotos()
        mysaves()

        if (profileid == firebaseUser!!.uid) {
            editprofile.text = "Edit Profile"
        } else {
            checkFollow()
            savedPhotos.setVisibility(View.GONE)
        }

        editprofile.setOnClickListener {
            val btn = editprofile.text.toString()

            if (btn == "Edit Profile") {
                startActivity(Intent(context, EditProfileActivity::class.java))
            } else if (btn == "follow") {
                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser!!.uid)
                    .child("following").child(profileid!!).setValue(true)

                FirebaseDatabase.getInstance().reference.child("Follow").child(profileid!!)
                    .child("followers").child(firebaseUser!!.uid).setValue(true)

                addNotifications()
            } else if (btn == "following") {
                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser!!.uid)
                    .child("following").child(profileid!!).removeValue()

                FirebaseDatabase.getInstance().reference.child("Follow").child(profileid!!)
                    .child("followers").child(firebaseUser!!.uid).removeValue()
            }
        }

        options.setOnClickListener {
            val intent = Intent(
                context,
                OptionsActivity::class.java
            )
            startActivity(intent)
        }

        myPhotos.setOnClickListener {
            recyclerView.visibility = View.VISIBLE
            recyclerView_saves.visibility = View.GONE
        }

        savedPhotos.setOnClickListener {
            recyclerView.visibility = View.GONE
            recyclerView_saves.visibility = View.VISIBLE
        }

        followers.setOnClickListener {
            val intent = Intent(
                context,
                FollowersActivity::class.java
            )
            intent.putExtra("id", profileid)
            intent.putExtra("title", "Followers")
            startActivity(intent)
        }

        following.setOnClickListener {
            val intent = Intent(
                context,
                FollowersActivity::class.java
            )
            intent.putExtra("id", profileid)
            intent.putExtra("title", "Following")
            startActivity(intent)
        }

        return view
    }

    private fun addNotifications() {
        val reference = FirebaseDatabase.getInstance().getReference("Notifications").child(
            profileid!!
        )

        val hashMap = HashMap<String, Any>()
        hashMap["userid"] = firebaseUser!!.uid
        hashMap["text"] = "started following you"
        hashMap["postid"] = ""
        hashMap["ispost"] = false

        reference.push().setValue(hashMap)
    }

    private fun userInfo() {
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(
            profileid!!
        )

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (context == null) {
                    return
                }

                val user: User? = dataSnapshot.getValue(User::class.java)
                Log.d("USER PROFILE IMAGE", user!!.imageurl)
                //                if(!user.getImageurl().equals("default")){
//
//                }
//                Picasso.get().load(user.imageurl).placeholder(R.drawable.ic_profile)
//                    .into(image_profile)
                if (!user.imageurl.isNullOrEmpty()) {
                    Picasso.get().load(user.imageurl)
                        .placeholder(R.drawable.ic_profile)  // Default placeholder
                        .into(image_profile)
                } else {
                    // Load a placeholder image directly if the URL is empty
                    Picasso.get().load(R.drawable.ic_profile)
                        .into(image_profile)
                }
                //                image_profile.setImageResource(R.drawable.default_avatar);
                username.setText(user.username)
                fullname.setText(user.name)
                bio.setText(user.bio)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun checkFollow() {
        val reference = FirebaseDatabase.getInstance().reference
            .child("Follow").child(firebaseUser!!.uid).child("following")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(profileid!!).exists()) {
                    editprofile.text = "following"
                } else {
                    editprofile.text = "follow"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun getFollowers() {
        val reference = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileid!!).child("followers")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                followers.text = "" + dataSnapshot.childrenCount
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        val reference1 = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileid!!).child("following")

        reference1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                following.text = "" + dataSnapshot.childrenCount
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun getNrPosts() {
        val reference = FirebaseDatabase.getInstance().getReference("Posts")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var i = 0
                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(Post::class.java)
                    if (post!!.publisher.equals(profileid)) {
                        i++
                    }
                }

                posts.text = "" + i
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun myFotos() {
        val reference = FirebaseDatabase.getInstance().getReference("PostsAndroid")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList.clear()
                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(Post::class.java)
                    if (post!!.publisher.equals(profileid)) {
                        postList.add(post)
                    }
                }

                Collections.reverse(postList)
                myPhotoAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun mysaves() {
        mySaves = mutableListOf()

        val reference = FirebaseDatabase.getInstance().reference.child("Saves")
            .child(firebaseUser!!.uid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    mySaves.add(snapshot.key!!)
                }

                readSaves()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun readSaves() {
        val reference = FirebaseDatabase.getInstance().getReference("PostsAndroid")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList_saves.clear()
                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(Post::class.java)

                    for (id in mySaves) {
                        if (post!!.postid.equals(id)) {
                            postList_saves.add(post!!)
                        }
                    }
                }

                myPhotAdapterSaves.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

}
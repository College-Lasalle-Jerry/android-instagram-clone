package com.example.instagramclone.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.adapters.PostAdapter
import com.example.instagramclone.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: MutableList<Post>

    private lateinit var followingList: MutableList<String>

    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
//        recyclerView.setHasFixedSize(true)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        postList = mutableListOf<Post>()
        postAdapter = PostAdapter(container!!.context, postList)
        recyclerView.setAdapter(postAdapter)

        Log.d("POST Adapter", "Posts: ${postAdapter.itemCount}")


        Log.d("POST Adapter", "Posts" + postAdapter.itemCount)
//
//        recyclerView_story = view.findViewById<RecyclerView>(R.id.recycler_view_story)
//        recyclerView_story.setHasFixedSize(true)
//        val linearLayoutManager1 = LinearLayoutManager(
//            context,
//            LinearLayoutManager.HORIZONTAL, false
//        )x
//        recyclerView_story.setLayoutManager(linearLayoutManager1)
//        storyList = ArrayList<Story>()
//        storyAdapter = StoryAdapter(context, storyList)
//        recyclerView_story.setAdapter(storyAdapter)

        progressBar = view.findViewById(R.id.progress_circular)

        checkFollowing()

        return view
    }

    private fun checkFollowing() {
        followingList = java.util.ArrayList()

        val reference = FirebaseDatabase.getInstance().getReference("Follow")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("following")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                followingList.clear()
                for (snapshot in dataSnapshot.children) {
                    followingList.add(snapshot.key!!)
                }

                readPosts()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun readPosts() {
        val reference = FirebaseDatabase.getInstance().reference.child("PostsAndroid")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList.clear()
                Log.d("SIZE OF DATA", "Count " + dataSnapshot.childrenCount)
                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(Post::class.java)
                    Log.d("POST VALUE", post!!.description)
//                    for (id in followingList) {
//                        if (post!!.publisher.equals(id)) {
//                            postList.add(post) // to only show following or follower feeds.
//                        }
//                    }
                    postList.add(post)
                    //                    postList.add(post); Only used for testing.
                }

                Log.d("POST FETCH", "COUNT " + postList.size)
                postAdapter.notifyDataSetChanged()
                progressBar!!.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Error Fetch", "Failed to read value.", databaseError.toException())
            }
        })
    }


}
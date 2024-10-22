
package com.example.instagramclone.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.adapters.PostAdapter
import com.example.instagramclone.models.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PostDetailFragment : Fragment() {

    private lateinit var postid: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: MutableList<Post>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_post_detail, container, false)
        val sharedPreferences = container!!.context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        postid = sharedPreferences.getString("postid", "none")!!

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        postList = mutableListOf()
        postAdapter = PostAdapter(container.context, postList)
        recyclerView.adapter = postAdapter

        readPost()

        return view
    }


    private fun readPost() {
        val reference = FirebaseDatabase.getInstance().getReference("PostsAndroid").child(postid) // Use Posts instead of PostsAndroid

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList.clear()
                val post = dataSnapshot.getValue(Post::class.java)
                postList.add(post!!)

                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

}
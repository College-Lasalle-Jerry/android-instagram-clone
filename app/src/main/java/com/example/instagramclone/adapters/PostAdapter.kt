package com.example.instagramclone.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.CommentsActivity
import com.example.instagramclone.FollowersActivity
import com.example.instagramclone.R
import com.example.instagramclone.fragments.PostDetailFragment
import com.example.instagramclone.fragments.ProfileFragment
import com.example.instagramclone.models.Post
import com.example.instagramclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class PostAdapter(
    private val mContext: Context,
    private val mPost: MutableList<Post>,
    private var firebaseUser: FirebaseUser? = null
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get().load(post.postimage).placeholder(R.drawable.ic_profile)
            .resize(500, 500)
            .into(holder.post_image)

        if (post.description.isEmpty()) {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.description
        }

        publisherInfo(holder.image_profile, holder.username, holder.publisher, post.publisher)
        isLikes(post.postid, holder.like)
        isSaved(post.postid, holder.save)
        noLikes(holder.likes, post.postid)
        getComments(post.postid, holder.comments)

        holder.image_profile.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("profileid", post.publisher)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.username.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("profileid", post.publisher)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack("home")
                .commit()
        }

        holder.publisher.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("profileid", post.publisher)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .addToBackStack("home")
                .commit()
        }

        holder.post_image.setOnClickListener {
            val editor =
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postid", post.postid)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                PostDetailFragment()
            ).commit()
        }

        holder.save.setOnClickListener {
            Toast.makeText(mContext, "Saved!", Toast.LENGTH_SHORT).show()
            if (holder.save.tag == "save") {
                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser!!.uid)
                    .child(post.postid).setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser!!.uid)
                    .child(post.postid).removeValue()
            }
        }

        holder.like.setOnClickListener {

            if (holder.like.tag == "like") {
                FirebaseDatabase.getInstance().reference.child("Likes")
                    .child(post.postid)
                    .child(firebaseUser!!.uid)
                    .setValue(true)

                addNotification(post.publisher, post.postid)
            } else {
                FirebaseDatabase.getInstance().reference.child("Likes")
                    .child(post.postid).removeValue()
            }
        }

        holder.comment.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postid", post.postid)
            intent.putExtra("publisherid", post.publisher)
            mContext.startActivity(intent)
        }

        holder.comments.setOnClickListener {
            val intent = Intent(mContext, CommentsActivity::class.java)
            intent.putExtra("postid", post.postid)
            intent.putExtra("publisherid", post.publisher)
            mContext.startActivity(intent)
        }

        // we will on this in the next part.

        holder.more.setOnClickListener {
            val popupMenu = PopupMenu(mContext, holder.more)
            popupMenu.setOnMenuItemClickListener { item ->
                val menuID = item.itemId
                if (menuID == R.id.edit) {
                    editPost(post.postid)
                } else if (menuID == R.id.delete) {
                    FirebaseDatabase.getInstance().getReference("Posts")
                        .child(post.postid).removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else if (menuID == R.id.report) {
                    Toast.makeText(mContext, "Report Sent!", Toast.LENGTH_SHORT).show()
                }
                true
            }

            popupMenu.inflate(R.menu.post_menu)
            if (!post.publisher.equals(firebaseUser!!.uid)) {
                popupMenu.menu.findItem(R.id.edit).setVisible(false)
                popupMenu.menu.findItem(R.id.delete).setVisible(false)
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int {
        return mPost.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image_profile: ImageView = itemView.findViewById(R.id.image_profile)
        var post_image: ImageView = itemView.findViewById(R.id.post_image)
        var like: ImageView = itemView.findViewById(R.id.like)
        var comment: ImageView = itemView.findViewById(R.id.comment)
        var save: ImageView = itemView.findViewById(R.id.save)
        var more: ImageView = itemView.findViewById(R.id.more)

        var username: TextView = itemView.findViewById(R.id.username)
        var likes: TextView = itemView.findViewById(R.id.likes)
        var publisher: TextView = itemView.findViewById(R.id.publisher)
        var description: TextView = itemView.findViewById(R.id.description)
        var comments: TextView = itemView.findViewById(R.id.comments)
    }

    // editPost
    private fun editPost(postId: String) {
        val alertDialog = AlertDialog.Builder(mContext)
        alertDialog.setTitle("Edit Post")

        val editText = EditText(mContext)
        // create a linear layout
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        editText.layoutParams = lp
        alertDialog.setView(editText)

        getText(postId, editText)

        alertDialog.setPositiveButton("Edit") { dialog, which ->
            val hashMap = HashMap<String, Any>()
            hashMap["description"] = editText.text.toString()

            FirebaseDatabase.getInstance().reference
                .child("Posts")
                .child(postId)
        }

        alertDialog.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    // getText
    private fun getText(postId: String, editText: EditText) {
        val reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                editText.setText(dataSnapshot.getValue(Post::class.java)?.description ?: "")
            }

            override fun onCancelled(error: DatabaseError) {
                // Nothing to do here.
            }
        })
    }

    // Add Notification -> is a like or a comment is given the publisher should be notified.
    private fun addNotification(userId: String, postId: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Notifications").child(postId)

        val hashMap = HashMap<String, Any>()
        hashMap["userid"] = userId
        hashMap["text"] = "liked your post"
        hashMap["postid"] = postId
        hashMap["ispost"] = true

        reference.push().setValue(hashMap)

    }


    // get the publisher info
    private fun publisherInfo(
        image_profile: ImageView,
        username: TextView,
        publisher: TextView,
        userid: String
    ) {
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: User? = dataSnapshot.getValue(User::class.java)

                // for the profile pic of the user. -> picasso
                Picasso.get().load(user?.imageurl).placeholder(R.drawable.ic_profile)
                    .into(image_profile)

                username.text = user?.username
                publisher.text = user?.name
            }

            override fun onCancelled(error: DatabaseError) {
                // nothing to be done here.
            }

        })
    }

    // isSaved -> Book Mark
    private fun isSaved(postId: String, imageView: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val reference = FirebaseDatabase.getInstance().reference.child("Saves")
            .child(firebaseUser!!.uid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(postId).exists()) {
                    imageView.setImageResource(R.drawable.ic_save_black)
                    imageView.tag = "saved"
                } else {
                    imageView.setImageResource(R.drawable.ic_save)
                    imageView.tag = "save"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // nothing to be done here.
            }

        })
    }

    // no of likes
    private fun noLikes(likes: TextView, postId: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Likes")
            .child(postId)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                likes.text = dataSnapshot.childrenCount.toString() + " likes"
                Log.d("Like count", dataSnapshot.child(postId).childrenCount.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                // nothing to be done here.
            }

        })
    }

    // is Liked

    private fun isLikes(postid: String, imageView: ImageView) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val reference = FirebaseDatabase.getInstance().reference.child("Likes")
            .child(postid).child(firebaseUser!!.uid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    imageView.setImageResource(R.drawable.ic_liked)
                    imageView.tag = "liked"
                } else {
                    imageView.setImageResource(R.drawable.ic_like)
                    imageView.tag = "like"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // nothing to be done here.
            }

        })

    }

    // comments -> textview

    private fun getComments(postid: String, comments: TextView) {
        val reference = FirebaseDatabase.getInstance().reference.child("Comments")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                comments.text =
                    "View all " + dataSnapshot.child(postid).childrenCount.toString() + " Comments"
            }

            override fun onCancelled(error: DatabaseError) {
                // nothing to be done here.
            }

        })
    }


}
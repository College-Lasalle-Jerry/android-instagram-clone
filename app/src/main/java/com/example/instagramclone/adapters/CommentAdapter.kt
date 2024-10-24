package com.example.instagramclone.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.MainActivity
import com.example.instagramclone.R
import com.example.instagramclone.models.Comment
import com.example.instagramclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class CommentAdapter(
    private val mContext: Context,
    private val mComment: MutableList<Comment>,
    private val postid: String,
    private var firebaseUser: FirebaseUser? = null
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val comment = mComment[position]

        holder.comment.setText(comment.comment)
        getUserInfo(holder.image_profile, holder.username, comment.publisher!!)

        holder.comment.setOnClickListener {
            val intent = Intent(mContext, MainActivity::class.java)
            intent.putExtra("publisherid", comment.publisher)
            mContext.startActivity(intent)
        }

        holder.image_profile.setOnClickListener {
            val intent = Intent(mContext, MainActivity::class.java)
            intent.putExtra("publisherid", comment.publisher)
            mContext.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            if (comment.publisher!!.endsWith(firebaseUser!!.uid)) {
                val alertDialog = AlertDialog.Builder(mContext).create()
                alertDialog.setTitle("Do you want to delete?")

                alertDialog.setButton(
                    AlertDialog.BUTTON_NEUTRAL, "No"
                ) { dialog, which -> dialog.dismiss() }

                alertDialog.setButton(
                    AlertDialog.BUTTON_POSITIVE, "Yes"
                ) { dialog, which ->
                    FirebaseDatabase.getInstance().getReference("Comments")
                        .child(postid).child(comment.commentid!!)
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) Toast.makeText(
                                mContext,
                                "Deleted!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    dialog.dismiss()
                }
                alertDialog.show()
            }
            true
        }


    }

    override fun getItemCount(): Int {
        return mComment.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var image_profile = itemView.findViewById<ImageView>(R.id.image_profile)
        var username = itemView.findViewById<TextView>(R.id.username)
        var comment = itemView.findViewById<TextView>(R.id.comment)

    }

    private fun getUserInfo(imageView: ImageView, username: TextView, publisherid: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Users").child(publisherid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: User? = dataSnapshot.getValue(User::class.java)
                Picasso.get().load(user!!.imageurl).placeholder(R.drawable.ic_image_added)
                    .into(imageView)
                username.setText(user.username)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }


}
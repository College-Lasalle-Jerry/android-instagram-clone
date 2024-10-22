package com.example.instagramclone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(
    private val mContext: Context,
    private val mUsers: MutableList<User>,
    private val isFragment: Boolean,
    private var firebaseUser: FirebaseUser? = null
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username = itemView.findViewById<android.widget.TextView?>(R.id.username)
        val fullname = itemView.findViewById<android.widget.TextView?>(R.id.fullname)
        val image_profile = itemView.findViewById<CircleImageView?>(R.id.image_profile)
        val btn_follow = itemView.findViewById<android.widget.Button?>(R.id.btn_follow)
    }

    private fun isFollowed(userid: String, button: Button) {
        val reference = FirebaseDatabase.getInstance().reference.child("Follow").child(
            firebaseUser!!.uid
        ).child("following")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(userid).exists()) {
                    button.text = "following"
                } else {
                    button.text = "follow"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val user = mUsers[position]
        holder.btn_follow.visibility = View.VISIBLE

        holder.username.setText(user.username)
        holder.fullname.setText(user.name)
        Picasso.get().load(user.imageurl).placeholder(R.drawable.ic_profile)
            .into(holder.image_profile)
        isFollowed(user.id, holder.btn_follow)

        if (user.id.equals(firebaseUser!!.uid)) {
            holder.btn_follow.visibility = View.GONE
        }

        holder.btn_follow.setOnClickListener {
            if (holder.btn_follow.text.toString() == "follow") {
                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser!!.uid)
                    .child("following").child(user.id).setValue(true)

                FirebaseDatabase.getInstance().reference.child("Follow").child(user.id)
                    .child("followers").child(firebaseUser!!.uid).setValue(true)

                addNotifications(user.id)
            } else {
                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser!!.uid)
                    .child("following").child(user.id).removeValue()

                FirebaseDatabase.getInstance().reference.child("Follow").child(user.id)
                    .child("followers").child(firebaseUser!!.uid).removeValue()
            }
        }
    }

    private fun addNotifications(userid: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid)

        val hashMap = HashMap<String, Any>()
        hashMap["userid"] = firebaseUser!!.uid
        hashMap["text"] = "started following you"
        hashMap["postid"] = ""
        hashMap["ispost"] = false

        reference.push().setValue(hashMap)
    }

}
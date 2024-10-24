package com.example.instagramclone.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.MainActivity
import com.example.instagramclone.R
import com.example.instagramclone.fragments.PostDetailFragment
import com.example.instagramclone.fragments.ProfileFragment
import com.example.instagramclone.models.Notification
import com.example.instagramclone.models.Post
import com.example.instagramclone.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class NotificationAdapter(
    private val mContext: Context,
    private val mNotifications: MutableList<Notification>,
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.ViewHolder {
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationAdapter.ViewHolder, position: Int) {
        val notification = mNotifications[position]
        holder.text.setText(notification.text)

        getUserInfo(holder.image_profile, holder.username, notification.userid!!)

        if (notification.isIspost) {
            holder.post_image.visibility = View.VISIBLE
            getPostImage(holder.post_image, notification.postid!!)
        } else {
            holder.post_image.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (notification.isIspost) {
                val editor =
                    mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                        .edit()
                editor.putString("postid", notification.postid)
                editor.apply()

                (mContext as MainActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    PostDetailFragment()
                ).commit()
            } else {
                val editor =
                    mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                        .edit()
                editor.putString("profileid", notification.postid)
                editor.apply()

                (mContext as MainActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    ProfileFragment()
                ).commit()
            }
        }
    }

    override fun getItemCount(): Int {
        return mNotifications.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var image_profile = itemView.findViewById<android.widget.ImageView?>(R.id.image_profile)
        var post_image = itemView.findViewById<android.widget.ImageView?>(R.id.post_image)
        var username = itemView.findViewById<android.widget.TextView?>(R.id.username)
        var text = itemView.findViewById<android.widget.TextView?>(R.id.comment)
    }

    private fun getUserInfo(imageView: ImageView, username: TextView, publisherid: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: User? = dataSnapshot.getValue(User::class.java)
                Picasso.get().load(user!!.imageurl).placeholder(R.drawable.ic_profile)
                    .into(imageView)
                username.setText(user.username)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun getPostImage(imageView: ImageView, postid: String) {
        val reference = FirebaseDatabase.getInstance().getReference("PostsAndroid").child(postid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val post: Post = checkNotNull(dataSnapshot.getValue(Post::class.java))
                Picasso.get().load(post.postimage).into(imageView)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }


}
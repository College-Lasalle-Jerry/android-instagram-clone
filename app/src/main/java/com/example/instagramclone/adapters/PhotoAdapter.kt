package com.example.instagramclone.adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.fragments.PostDetailFragment
import com.example.instagramclone.models.Post
import com.squareup.picasso.Picasso

class PhotoAdapter(
    private val mContext: Context,
    private val mPost: MutableList<Post>,
) : RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.photo_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = mPost[position]

        Picasso.get().load(post.postimage).placeholder(R.drawable.ic_profile)
            .resize(100,100)
            .into(holder.post_image)

        holder.post_image.setOnClickListener {
            val editor: SharedPreferences.Editor =
                mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
            editor.putString("postid", post.postid)
            editor.apply()

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                PostDetailFragment()
            ).addToBackStack("profile").commit()
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var post_image: ImageView = itemView.findViewById(R.id.post_image)
    }
}
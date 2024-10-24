package com.example.instagramclone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.example.instagramclone.adapters.NotificationAdapter
import com.example.instagramclone.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections


class NotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notificationList: MutableList<Notification>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_notifications, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        notificationList = ArrayList()
        notificationAdapter = NotificationAdapter(container!!.context, notificationList)
        recyclerView.adapter = notificationAdapter

        readNotification()

        return view
    }


    private fun readNotification() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Notifications").child(
            firebaseUser!!.uid
        )
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                notificationList.clear()
                for (snapshot in dataSnapshot.children) {
                    val notification = snapshot.getValue(Notification::class.java)
                    notificationList.add(notification!!)
                }

                Collections.reverse(notificationList)
                notificationAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

}
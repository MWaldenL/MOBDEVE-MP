package com.mobdeve.s15.group8.mobdeve_mp.controller.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ExpandableListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.mobdeve.s15.group8.mobdeve_mp.F
import com.mobdeve.s15.group8.mobdeve_mp.R
import com.mobdeve.s15.group8.mobdeve_mp.GoogleSingleton
import com.mobdeve.s15.group8.mobdeve_mp.controller.adapters.DashboardTaskGroupAdapter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DashboardActivity : AppCompatActivity() {
//    private lateinit var textGreeting: TextView
//    private lateinit var buttonSignOut: Button

    private lateinit var taskGroups: ArrayList<String>
    private lateinit var tasksChildren: HashMap<String, ArrayList<String>>

    private lateinit var elvTaskGroup: ExpandableListView
    private lateinit var taskGroupAdapter: DashboardTaskGroupAdapter

    private val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> }

    private fun mGenerateSampleTasksChildren(): HashMap<String, ArrayList<String>> {
        var sampleTaskGroup: HashMap<String, ArrayList<String>> = HashMap()

        var plants: ArrayList<String> = ArrayList()
        plants.add("Snake Plant")
        plants.add("Oregano")
        plants.add("Basil")
        plants.add("San Francisco")
        plants.add("Johnny")

        val grpKeys = arrayListOf(*resources.getStringArray(R.array.actions_array))
        for (key in grpKeys) {
            sampleTaskGroup[key] = plants
        }

        return sampleTaskGroup
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        elvTaskGroup = findViewById(R.id.elv_task_group)
        taskGroups = arrayListOf(*resources.getStringArray(R.array.actions_array))
        tasksChildren = mGenerateSampleTasksChildren()
        taskGroupAdapter = DashboardTaskGroupAdapter(applicationContext, taskGroups, tasksChildren)
        elvTaskGroup.setAdapter(taskGroupAdapter)

        /*textGreeting = findViewById(R.id.text_user)
        textGreeting.text = F.auth.currentUser?.displayName
        buttonSignOut = findViewById(R.id.btn_signout)
        buttonSignOut.setOnClickListener { mSignOut() }*/
    }

    private fun mSignOut() {
        F.auth.signOut()
        GoogleSignIn.getClient(this, GoogleSingleton.googleSigninOptions).signOut()
        loginLauncher.launch(Intent(this@DashboardActivity, MainActivity::class.java))
    }
}
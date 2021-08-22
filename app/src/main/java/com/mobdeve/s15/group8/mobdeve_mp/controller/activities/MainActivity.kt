package com.mobdeve.s15.group8.mobdeve_mp.controller.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldValue
import com.mobdeve.s15.group8.mobdeve_mp.singletons.F
import com.mobdeve.s15.group8.mobdeve_mp.singletons.GoogleSingleton
import com.mobdeve.s15.group8.mobdeve_mp.R
import com.mobdeve.s15.group8.mobdeve_mp.controller.activities.fragments.dialogs.AppFeedbackDialogFragment
import com.mobdeve.s15.group8.mobdeve_mp.controller.activities.fragments.dialogs.DailyNotificationsDialogFragment
import com.mobdeve.s15.group8.mobdeve_mp.controller.receivers.AlarmReceiver
import com.mobdeve.s15.group8.mobdeve_mp.model.services.DBService
import com.mobdeve.s15.group8.mobdeve_mp.model.services.DateTimeService
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity:
    AppCompatActivity(),
    AppFeedbackDialogFragment.AppFeedbackDialogListener,
    DailyNotificationsDialogFragment.DailyNotificationsDialogListener
{
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabAddPlant: FloatingActionButton

    private lateinit var btnSetAlarm: Button
    private lateinit var btnCancel: Button

    // TODO: Remove these in final impl, only here for manual cancel
    private lateinit var alarmManager: AlarmManager
    private lateinit var notificationIntent: Intent
    private lateinit var pendingIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottom_nav_view)
        val navFragment = supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment
        val navController = navFragment.navController
        bottomNav.setupWithNavController(navController)

        fabAddPlant = findViewById(R.id.fab_add_plant)
        fabAddPlant.setOnClickListener {
            val addPlantIntent = Intent(this@MainActivity, AddPlantActivity::class.java)
            startActivity(addPlantIntent)
        }

        mHandleDailyNotificationsReady()

        // TODO: Remove these in final impl, only here for manual cancel
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        notificationIntent = Intent(this, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 1, notificationIntent, 0)

        btnSetAlarm = findViewById(R.id.btn_set_alarm)
        btnSetAlarm.setOnClickListener {
            mSetAlarm()
        }

        btnCancel = findViewById(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            alarmManager.cancel(pendingIntent)
        }
    }

    // push notifications function

    private fun mHandleDailyNotificationsReady() {
        val uid = F.auth.currentUser?.uid
        if (uid != null) {
            F.usersCollection
                .document(uid)
                .get()
                .addOnSuccessListener {
                    if (it != null) {
                        val doc = it.data
                        if (doc != null) {
                            if (doc["pushAsked"] == false) {
                                val fragment = DailyNotificationsDialogFragment()
                                fragment.show(supportFragmentManager, "daily_notifications")
                            } else {
                                mHandleFeedbackReady()
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d("fail", it.toString())
                }
        }
    }

    override fun onPushAccept(dialog: DialogFragment) {
        val id = F.auth.currentUser?.uid

        DBService.updateDocument(
            F.usersCollection,
            id,
            "pushAsked",
            true
        )

        DBService.updateDocument(
            F.usersCollection,
            id,
            "pushAllowed",
            true
        )

        mSetAlarm()
        mHandleFeedbackReady()
    }

    override fun onPushDecline(dialog: DialogFragment) {
        val id = F.auth.currentUser?.uid

        DBService.updateDocument(
            F.usersCollection,
            id,
            "pushAsked",
            true
        )

        DBService.updateDocument(
            F.usersCollection,
            id,
            "pushAllowed",
            false
        )

        mHandleFeedbackReady()
    }

    private fun mSetAlarm() {
        val c = Calendar.getInstance()

        // uncomment for final impl
//        c.set(Calendar.HOUR_OF_DAY, 10)
//        c.set(Calendar.MINUTE, 0)
//        c.set(Calendar.SECOND, 0)
//
//        if (c.before(Calendar.getInstance()))
//            c.add(Calendar.DATE, 1)
//
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent) // change third arg to millis (min 60000) to test repeated

        // code for testing
        c.add(Calendar.SECOND, 30)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.timeInMillis, pendingIntent) // use this to check for exact time
    }

    // feedback functions

    private fun mHandleFeedbackReady() {
        val uid = F.auth.currentUser?.uid
        if (uid != null) {
            F.usersCollection
                .document(uid)
                .get()
                .addOnSuccessListener {
                    if (it != null) {
                        val doc = it.data
                        if (doc != null) {

                            if (doc["feedbackLastSent"] == doc["dateJoined"]) { // first time receiving the prompt
                                val then = DateTimeService.stringToDate(doc["dateJoined"].toString())
                                val now = Date()
                                val diff = now.time - then.time
                                val hours = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS)

                                if (hours >= 48) // two days
                                    mTriggerFeedback()
                            } else { // further times
                                if (doc["feedbackStop"] == false) {
                                    val then = DateTimeService.stringToDate(doc["feedbackLastSent"].toString())
                                    val now = Date()
                                    val diff = now.time - then.time
                                    val hours = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS)

                                    if (hours >= 336) // one week
                                        mTriggerFeedback()
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d("fail", it.toString())
                }
        }
    }

    private fun mTriggerFeedback() {
        val fragment = AppFeedbackDialogFragment()
        fragment.show(supportFragmentManager, "feedback")
    }

    override fun onFeedbackContinue(dialog: DialogFragment, feedbackRating: Float, feedbackComment: String) {
        val id = F.auth.currentUser?.uid
        val toAdd = hashMapOf(
            "rating" to feedbackRating,
            "comment" to feedbackComment
        )

        DBService.updateDocument(
            F.usersCollection,
            id,
            "feedback",
            FieldValue.arrayUnion(toAdd)
        )

        DBService.updateDocument(
            F.usersCollection,
            id,
            "feedbackStop",
            false
        )

        DBService.updateDocument(
            F.usersCollection,
            id,
            "feedbackLastSent",
            DateTimeService.getCurrentDateTime()
        )
    }

    override fun onFeedbackStop(dialog: DialogFragment) {
        val id = F.auth.currentUser?.uid

        DBService.updateDocument(
            F.usersCollection,
            id,
            "feedbackStop",
            true
        )

        DBService.updateDocument(
            F.usersCollection,
            id,
            "feedbackLastSent",
            DateTimeService.getCurrentDateTime()
        )
    }

    override fun onFeedbackCancel(dialog: DialogFragment) {
        val id = F.auth.currentUser?.uid

        DBService.updateDocument(
            F.usersCollection,
            id,
            "feedbackStop",
            false
        )

        DBService.updateDocument(
            F.usersCollection,
            id,
            "feedbackLastSent",
            DateTimeService.getCurrentDateTime()
        )
    }
}

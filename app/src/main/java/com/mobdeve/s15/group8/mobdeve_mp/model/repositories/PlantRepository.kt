package com.mobdeve.s15.group8.mobdeve_mp.model.repositories

import com.mobdeve.s15.group8.mobdeve_mp.F
import com.mobdeve.s15.group8.mobdeve_mp.controller.interfaces.DBCallback
import com.mobdeve.s15.group8.mobdeve_mp.controller.interfaces.RefreshCallback
import com.mobdeve.s15.group8.mobdeve_mp.model.dataobjects.Journal
import com.mobdeve.s15.group8.mobdeve_mp.model.dataobjects.Plant
import com.mobdeve.s15.group8.mobdeve_mp.model.dataobjects.Task
import com.mobdeve.s15.group8.mobdeve_mp.model.services.DBService

object PlantRepository: DBCallback {
    private var mDBListener: DBCallback? = null
    private var mRefreshListener: RefreshCallback? = null
    var plantList: ArrayList<Plant> = ArrayList()
    private var mUserDoc: MutableMap<String, Any> = HashMap()
    private var mPlantDoc: MutableMap<String, Any> = HashMap()

    init {
        DBService.setDBCallbackListener(this)
    }

    fun setOnDataFetchedListener(listener: DBCallback?) {
        mDBListener = listener
    }

    fun setRefreshedListener(listener: RefreshCallback) {
        mRefreshListener = listener
    }

    fun getData() {
        plantList = ArrayList()
        F.auth.currentUser?.uid?.let {
            DBService.readDocument(
                collection=F.usersCollection,
                id=it)
        }
    }

    override fun onDataRetrieved(doc: MutableMap<String, Any>, id: String, type: String) {
        if (type == "users") {
            mUserDoc = doc
            val plants = mUserDoc["plants"] as ArrayList<String>

            // Create a plant object for each id
            for (plantId in plants) {
                DBService.readDocument(
                    collection=F.plantsCollection,
                    id=plantId)
            }

            // Notify listeners on data fetch completion
            if (mDBListener != null) {
                mDBListener?.onComplete()
            }
            mRefreshListener?.onDataFetched()
        } else { // Fetch the plant and add to plant list
            mPlantDoc = doc
            val journal = ArrayList<Journal>()
            val tasks = ArrayList<Task>()
            val docTasks = mPlantDoc["tasks"] as ArrayList<HashMap<*, *>>
            val docJournal = mPlantDoc["journal"] as ArrayList<HashMap<*, *>>
            for (t in docTasks) {
                tasks.add(Task(
                    action=t["action"].toString(),
                    startDate=t["startDate"].toString(),
                    repeat=t["repeat"].toString().toInt(),
                    occurrence=t["occurrence"].toString()))
            }
            for (j in docJournal) {
                journal.add(Journal(
                    body=j["body"].toString(),
                    date=j["date"].toString()))
            }
            plantList.add(Plant(
                id,
                imageUrl=mPlantDoc["imageUrl"].toString(),
                filePath="",
                name=mPlantDoc["name"].toString(),
                nickname=mPlantDoc["nickname"].toString(),
                dateAdded=mPlantDoc["dateAdded"].toString(),
                death=mPlantDoc["death"] as Boolean,
                tasks,
                journal
            ))
        }
    }

    override fun onComplete() {
    }
}

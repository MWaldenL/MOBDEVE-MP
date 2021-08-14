package com.mobdeve.s15.group8.mobdeve_mp.controller

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group8.mobdeve_mp.R
import com.mobdeve.s15.group8.mobdeve_mp.controller.adapters.PlantListAdapter
import com.mobdeve.s15.group8.mobdeve_mp.model.PlantRepository
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// Can be converted to fragment later on for tabbed interface
class ViewAllPlantsActivity: AppCompatActivity(), CoroutineScope {
    private lateinit var recyclerView: RecyclerView
    private val viewPlantLauncher = registerForActivityResult(StartActivityForResult()) { result -> }
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_plants)
        val pr = PlantRepository()
        launch {
            pr.getData()
            withContext(Dispatchers.Main) {
                recyclerView = findViewById(R.id.recyclerview_plant)
                recyclerView.adapter = PlantListAdapter(pr.plantList, viewPlantLauncher)
                recyclerView.layoutManager = GridLayoutManager(applicationContext, 2)
            }
        }
    }
}
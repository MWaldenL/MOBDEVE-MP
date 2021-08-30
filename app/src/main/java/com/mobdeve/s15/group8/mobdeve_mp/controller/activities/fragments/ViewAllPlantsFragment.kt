package com.mobdeve.s15.group8.mobdeve_mp.controller.activities.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mobdeve.s15.group8.mobdeve_mp.R
import com.mobdeve.s15.group8.mobdeve_mp.controller.adapters.PlantListAdapter
import com.mobdeve.s15.group8.mobdeve_mp.controller.interfaces.NewPlantCallback
import com.mobdeve.s15.group8.mobdeve_mp.controller.interfaces.RefreshCallback
import com.mobdeve.s15.group8.mobdeve_mp.model.dataobjects.Plant
import com.mobdeve.s15.group8.mobdeve_mp.model.repositories.NewPlantInstance
import com.mobdeve.s15.group8.mobdeve_mp.model.repositories.PlantRepository
import com.mobdeve.s15.group8.mobdeve_mp.singletons.LayoutType

class ViewAllPlantsFragment: Fragment(), NewPlantCallback, RefreshCallback {
    private lateinit var ibGridView: ImageButton
    private lateinit var recyclerViewAlive: RecyclerView
    private lateinit var recyclerViewDead: RecyclerView
    private lateinit var swipeToRefreshLayout: SwipeRefreshLayout
    private lateinit var mSharedPref: SharedPreferences
    private lateinit var mEditor: SharedPreferences.Editor
    private lateinit var plantAliveAdapter: PlantListAdapter
    private lateinit var plantDeadAdapter: PlantListAdapter
    private var mPlantListViewType = LayoutType.GRID_VIEW.ordinal // default to grid view
    private var mAlive = arrayListOf<Plant>()
    private var mDead = arrayListOf<Plant>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_all_plants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ibGridView = view.findViewById(R.id.ib_gridview)
        recyclerViewAlive = view.findViewById(R.id.recyclerview_plant)
        recyclerViewDead = view.findViewById(R.id.recyclerview_dead)
        swipeToRefreshLayout = view.findViewById(R.id.sr_layout_view_all_plants)

        // Setup listeners
        NewPlantInstance.setOnNewPlantListener(this) // listen for new plant added
        PlantRepository.setRefreshedListener(this) // listen for data fetch complete
        ibGridView.setOnClickListener { mToggleLayout() }

        // Setup shared preferences
        mSharedPref = this
            .requireActivity()
            .getSharedPreferences(getString(R.string.SP_NAME), Context.MODE_PRIVATE)
        mEditor = mSharedPref.edit()
        mPlantListViewType = mSharedPref.getInt(getString(R.string.SP_VIEW_KEY), 0)

        swipeToRefreshLayout.setOnRefreshListener { PlantRepository.getData() }
        mRefreshRecyclerViews()
    }

    private fun mToggleLayout() {
        mPlantListViewType = 1 - mPlantListViewType
        mEditor.putInt(getString(R.string.SP_VIEW_KEY), mPlantListViewType)
        mEditor.apply()
        ibGridView.setImageResource(if (mPlantListViewType == LayoutType.LINEAR_VIEW.ordinal)
            R.drawable.ic_bento_24 else
            R.drawable.ic_card_24)
        mRefreshRecyclerViews()
    }

    private fun mRefreshRecyclerViews() {
        plantAliveAdapter = PlantListAdapter(mAlive)
        plantDeadAdapter = PlantListAdapter(mDead)
        plantAliveAdapter.viewType = mPlantListViewType
        plantDeadAdapter.viewType = mPlantListViewType
        recyclerViewAlive.adapter = plantAliveAdapter
        recyclerViewDead.adapter = plantDeadAdapter
        recyclerViewAlive.layoutManager = if (mPlantListViewType == LayoutType.LINEAR_VIEW.ordinal)
            LinearLayoutManager(requireContext()) else
            GridLayoutManager(requireContext(), 2)
        recyclerViewDead.layoutManager = if (mPlantListViewType == LayoutType.LINEAR_VIEW.ordinal)
            LinearLayoutManager(requireContext()) else
            GridLayoutManager(requireContext(), 2)
    }

    override fun onStart() {
        super.onStart()
        Log.d("HELLO", "onStart - ${mPlantListViewType}")
        mPlantListViewType = mSharedPref.getInt(getString(R.string.SP_VIEW_KEY), mPlantListViewType)
        mDead.clear()
        mAlive.clear()
        for (plant in PlantRepository.plantList) {
            if (plant.death)
                mDead.add(plant)
            else
                mAlive.add(plant)
        }
        onPlantAdded()
    }

    override fun onResume() {
        super.onResume()
        Log.d("HELLO", "onResume - ${mPlantListViewType}")
        mPlantListViewType = mSharedPref.getInt(getString(R.string.SP_VIEW_KEY), mPlantListViewType)
    }

    override fun onPlantAdded() {
        recyclerViewAlive.adapter?.notifyDataSetChanged()
        recyclerViewDead.adapter?.notifyDataSetChanged()
    }

    override fun onRefreshSuccess() {
        swipeToRefreshLayout.isRefreshing = false
    }
}
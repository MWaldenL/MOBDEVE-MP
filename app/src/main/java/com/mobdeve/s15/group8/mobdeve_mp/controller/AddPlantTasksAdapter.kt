package com.mobdeve.s15.group8.mobdeve_mp.controller

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group8.mobdeve_mp.R
import com.mobdeve.s15.group8.mobdeve_mp.model.Task
import java.lang.StringBuilder

class AddPlantTasksAdapter(
        private val data: ArrayList<Task>
    ) : RecyclerView.Adapter<AddPlantTasksAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val actionTV: TextView = view.findViewById(R.id.tv_action)
        val startDateTV: TextView = view.findViewById(R.id.tv_start_date)
        val repeatTV: TextView = view.findViewById(R.id.tv_repeat)

        val deleteTaskIBtn: ImageButton = view.findViewById(R.id.ibtn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater =LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_task, parent, false)


        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repeatStringBuilder = StringBuilder()
        repeatStringBuilder.append(data[position].repeat)
                            .append(" times ")
                            .append(data[position].occurrence)

        holder.actionTV.text = data[position].action
        holder.startDateTV.text = data[position].startDate
        holder.repeatTV.text = repeatStringBuilder.toString()

        holder.deleteTaskIBtn.setOnClickListener {
            val task = data[holder.adapterPosition]
            data.remove(task)
            notifyItemRemoved(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}
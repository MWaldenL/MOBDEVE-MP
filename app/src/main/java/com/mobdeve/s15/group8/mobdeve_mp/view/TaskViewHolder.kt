package com.mobdeve.s15.group8.mobdeve_mp.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s15.group8.mobdeve_mp.model.dataobjects.Task

class TaskViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//    private val cbTask: CheckBox = itemView.findViewById(R.id.cb_task)

    fun bindData(task: Task) {
//        cbTask.text = task.action
    }
}
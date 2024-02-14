package com.ozan.familytask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ozan.familytask.model.Task

class mytasks : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private var taskList: MutableList<Task> = mutableListOf()
    val memberId = FamilyActivity.memberId

    companion object {
        private const val TAG = "mytasks"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_mytasks, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TaskAdapter(taskList)
        recyclerView.adapter = adapter

        fetchTasks()

        return view
    }

    private fun fetchTasks() {
        val db = FirebaseFirestore.getInstance()



        val memberId = FamilyActivity.memberId

        if (memberId != null) {
            val collectionReference = db.collection("member_tasks")
                .whereEqualTo("member_id", memberId)
                .whereEqualTo("m_task_approve", "1")

            collectionReference.get()
                .addOnSuccessListener { documents ->
                    taskList.clear()
                    for (document in documents) {
                        val taskId = document.getString("m_task_id")
                        val taskText = document.getString("m_task_text")
                        val memberId = document.getString("member_id")
                        val dueDate = document.getString("m_dueDate")

                        if (taskId != null && taskText != null && memberId != null && dueDate != null) {
                            val task = Task(taskId, taskText, memberId, dueDate)
                            taskList.add(task)
                        }
                    }

                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    // Hata durumunda uygun şekilde işlem yapın
                }
        }
    }
}

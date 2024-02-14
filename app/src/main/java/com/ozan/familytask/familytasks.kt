package com.ozan.familytask

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.ozan.familytask.model.Task

class familytasks : Fragment() {

    private lateinit var memberAuthority: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private var taskList: MutableList<Task> = mutableListOf()
    var familyId: String? = null

    companion object {
        private const val TAG = "familytasks"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_familytasks, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        memberAuthority = arguments?.getString("memberAuthority", "") ?: ""
        adapter = TaskAdapter(taskList)
        recyclerView.adapter = adapter

        fetchTasks()

        // FloatingActionButton'ı bul
        val fab: FloatingActionButton = view.findViewById(R.id.btnAdd)

        // FloatingActionButton'a onClickListener ekle
        fab.setOnClickListener {
            // Intent oluştur
            val intent = when (memberAuthority) {
                "Member" -> Intent(activity, MemberAddActivity::class.java)
                "Admin" -> {
                    val adminIntent = Intent(activity, AdminTaskActivity::class.java)
                    val familyId = requireContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE).getString("familyId", null)
                    familyId?.let { adminIntent.putExtra("familyId", it) }
                    adminIntent
                }
                else -> null  // Belirtilen üye otoritesi yoksa null döndür
            }

            // Eğer intent null değilse, startActivity ile aktiviteyi başlat
            intent?.let {
                startActivity(intent)
            }
        }

        return view
    }

    private fun fetchTasks() {
        val db = FirebaseFirestore.getInstance()
        val familyId = requireContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE).getString("familyId", null)

        Log.d(TAG, "Family ID: $familyId")

        if (familyId != null) {
            val collectionReference = db.collection("family_tasks")
                .whereEqualTo("family_id", familyId)
                .whereEqualTo("task_approve", "1")

            collectionReference.get()
                .addOnSuccessListener { documents ->
                    taskList.clear()
                    for (document in documents) {
                        val taskId = document.getString("task_id")
                        val taskText = document.getString("task_text")
                        val memberId = document.getString("family_id")
                        val dueDate = document.getString("dueDate")

                        if (taskId != null && taskText != null && memberId != null && dueDate != null) {
                            val task = Task(taskId, taskText, memberId, dueDate)
                            taskList.add(task)
                        }
                    }

                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching tasks: ${exception.message}", exception)
                }
        } else {
            Log.e(TAG, "Family ID is null")
            // familyId değeri null ise uygun bir hata işlemi yapabilirsiniz
        }
    }

}

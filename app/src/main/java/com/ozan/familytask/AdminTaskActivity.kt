package com.ozan.familytask

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ozan.familytask.model.Task

class AdminTaskActivity : AppCompatActivity() {

    private lateinit var taskList: ArrayList<Task>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminTaskAdapter
    private lateinit var familyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_task)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // FamilyActivity'den aktarılan familyId değerini al
        familyId = intent.getStringExtra("familyId") ?: ""

        // Firestore'dan görevleri al ve listeye bağla
        fetchTasks()
    }

    private fun fetchTasks() {
        val db = FirebaseFirestore.getInstance()
        val memberTasksRef = db.collection("member_tasks")
        val familyTasksRef = db.collection("family_tasks")

        // Task listesini başlat
        taskList = ArrayList()

        // Member tasks sorgusu
        db.collection("member")
            .whereEqualTo("family_id", familyId)
            .get()
            .addOnSuccessListener { members ->
                val memberIds = members.documents.mapNotNull { it.getString("member_id") }
                if (memberIds.isNotEmpty()) { // Member IDs listesi boş değilse devam et
                    val memberTasksQuery = memberTasksRef
                        .whereIn("member_id", memberIds)
                        .whereEqualTo("m_task_approve", "0")

                    memberTasksQuery.get()
                        .addOnSuccessListener { memberTaskDocuments ->
                            taskList = ArrayList()
                            for (document in memberTaskDocuments) {
                                val taskId = document.getString("m_task_id")
                                val taskText = document.getString("m_task_text")
                                val memberId = document.getString("member_id")
                                val dueDate = document.getString("m_dueDate")

                                if (taskId != null && taskText != null && memberId != null && dueDate != null) {
                                    val task = Task(taskId, taskText, memberId, dueDate)
                                    taskList.add(task)
                                }
                            }
                            processTaskList(taskList)
                        }
                        .addOnFailureListener { exception ->
                            // Hata durumunda uygun şekilde işlem yapın
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Hata durumunda uygun şekilde işlem yapın
            }

        // Family tasks sorgusu
        familyTasksRef
            .whereEqualTo("family_id", familyId)
            .whereEqualTo("task_approve", "0")
            .get()
            .addOnSuccessListener { familyDocuments ->
                for (document in familyDocuments) {
                    val taskId = document.getString("task_id")
                    val taskText = document.getString("task_text")
                    val memberId = document.getString("family_id")
                    val dueDate = document.getString("dueDate")

                    if (taskId != null && taskText != null && memberId != null && dueDate != null) {
                        val task = Task(taskId, taskText, memberId, dueDate)
                        taskList.add(task)
                    }
                }
                processTaskList(taskList)
            }
            .addOnFailureListener { exception ->
                // Hata durumunda uygun şekilde işlem yapın
            }
    }


    private fun processTaskList(tasks: List<Task>) {
        // RecyclerView'ı oluşturun ve bağlayın
        adapter = AdminTaskAdapter(taskList)
        recyclerView.adapter = adapter
    }
}

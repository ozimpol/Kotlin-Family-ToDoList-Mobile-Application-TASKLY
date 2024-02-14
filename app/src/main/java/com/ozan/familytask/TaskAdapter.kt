package com.ozan.familytask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ozan.familytask.model.Task

class TaskAdapter(private val taskList: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val db = FirebaseFirestore.getInstance()
        private val textTaskText: TextView = itemView.findViewById(R.id.textTaskText)
        private val textDueDate: TextView = itemView.findViewById(R.id.textDueDate)
        private val textTaskId: TextView = itemView.findViewById(R.id.textInvisible)
        private val buttonApprove: Button = itemView.findViewById(R.id.buttonApprove)

        fun bind(task: Task) {
            textTaskId.text = task.taskId
            textTaskText.text = task.taskText
            textDueDate.text = task.dueDate

            buttonApprove.setOnClickListener {
                val taskId = textTaskId.text.toString()
                val memberId = task.memberId
                val familyId = FamilyActivity.familyId
                val memberId2 = FamilyActivity.memberId

                if (memberId == familyId) {

                    deleteTaskFromFirestore(taskId)
                } else {
                    deleteTaskFromMemberTasks(taskId)
                }
            }
        }

        private fun deleteTaskFromFirestore(taskId: String) {
            val collectionReference = db.collection("family_tasks")

            collectionReference
                .whereEqualTo("task_id", taskId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val docId = document.id
                        // Dokümanı sil
                        db.collection("family_tasks")
                            .document(docId)
                            .delete()
                            .addOnSuccessListener {

                                taskList.removeAll { it.taskId == taskId }
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener { exception ->

                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Firestore'dan veri alma hatası olduğunda uygun şekilde işlem yapın
                }
        }

        private fun deleteTaskFromMemberTasks(taskId: String) {
            val collectionReference = db.collection("member_tasks")

            collectionReference
                .whereEqualTo("m_task_id", taskId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val docId = document.id
                        // Dokümanı sil
                        db.collection("member_tasks")
                            .document(docId)
                            .delete()
                            .addOnSuccessListener {
                                // Silme işlemi başarılı olduğunda ilgili task'ı Task listesinden de kaldır
                                taskList.removeAll { it.taskId == taskId }
                                notifyDataSetChanged()
                            }
                            .addOnFailureListener { exception ->
                                // Silme işlemi başarısız olduğunda uygun şekilde işlem yapın
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Firestore'dan veri alma hatası olduğunda uygun şekilde işlem yapın
                }
        }

    }

}

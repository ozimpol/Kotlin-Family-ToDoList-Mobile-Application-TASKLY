package com.ozan.familytask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ozan.familytask.model.Task

class AdminTaskAdapter(private val taskList: MutableList<Task>) :
    RecyclerView.Adapter<AdminTaskAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item_layout, parent, false)
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
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textTaskText: TextView = itemView.findViewById(R.id.textTaskText)
        private val textDueDate: TextView = itemView.findViewById(R.id.textDueDate)
        private val textTaskId: TextView = itemView.findViewById(R.id.textInvisible)
        private val buttonApprove: Button = itemView.findViewById(R.id.buttonApprove)
        private val buttonReject: Button = itemView.findViewById(R.id.buttonReject)

        fun bind(task: Task) {
            textTaskId.text = task.taskId
            textTaskText.text = task.taskText
            textDueDate.text = task.dueDate

            if (task.memberId == FamilyActivity.familyId) {
                textName.text = "All Family"
            } else {
                // Member adını almak için Firestore'dan sorgu yap
                db.collection("member")
                    .whereEqualTo("member_id", task.memberId)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val memberName = documents.documents[0].getString("member_name")
                            textName.text = memberName
                        }
                    }
            }

            buttonApprove.setOnClickListener {
                if (task.memberId == FamilyActivity.familyId) {
                    approveTaskFamily(task.taskId)
                } else {
                    approveTaskMember(task.taskId)
                }
            }

            buttonReject.setOnClickListener {
                if (task.memberId == FamilyActivity.familyId) {
                    rejectTaskFamily(task.taskId)
                } else {
                    rejectTaskMember(task.taskId)
                }
            }
        }

        private fun approveTaskMember(taskId: String) {
            // Firestore'dan task verisini güncelle
            db.collection("member_tasks")
                .whereEqualTo("m_task_id", taskId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val docId = documents.documents[0].id
                        db.collection("member_tasks")
                            .document(docId)
                            .update("m_task_approve", "1")
                            .addOnSuccessListener {
                                taskList.removeAll { it.taskId == taskId }
                                notifyDataSetChanged()
                            }
                    }
                }
        }

        private fun rejectTaskMember(taskId: String) {
            // Firestore'dan task verisini sil
            db.collection("member_tasks")
                .whereEqualTo("m_task_id", taskId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val docId = documents.documents[0].id
                        db.collection("member_tasks")
                            .document(docId)
                            .delete()
                            .addOnSuccessListener {
                                taskList.removeAll { it.taskId == taskId }
                                notifyDataSetChanged()
                            }
                    }
                }
        }

        private fun approveTaskFamily(taskId: String) {
            // Firestore'dan task verisini güncelle
            db.collection("family_tasks")
                .whereEqualTo("task_id", taskId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val docId = documents.documents[0].id
                        db.collection("family_tasks")
                            .document(docId)
                            .update("task_approve", "1")
                            .addOnSuccessListener {
                                taskList.removeAll { it.taskId == taskId }
                                notifyDataSetChanged()
                            }
                    }
                }
        }

        private fun rejectTaskFamily(taskId: String) {
            // Firestore'dan task verisini sil
            db.collection("family_tasks")
                .whereEqualTo("task_id", taskId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val docId = documents.documents[0].id
                        db.collection("family_tasks")
                            .document(docId)
                            .delete()
                            .addOnSuccessListener {
                                taskList.removeAll { it.taskId == taskId }
                                notifyDataSetChanged()
                            }
                    }
                }
        }
    }
}

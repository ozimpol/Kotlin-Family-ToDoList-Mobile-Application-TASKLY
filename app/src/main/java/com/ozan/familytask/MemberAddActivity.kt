package com.ozan.familytask

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.UUID

class MemberAddActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var editTextDate: EditText
    private lateinit var buttonAdd: Button
    private lateinit var taskText: EditText
    private lateinit var familyId: String

    private val db = FirebaseFirestore.getInstance()
    private val memberTasksRef = db.collection("member_tasks")
    private val familyTasksRef = db.collection("family_tasks")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        familyId = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE).getString("familyId", "") ?: ""

        setContentView(R.layout.activity_member_add)

        spinner = findViewById(R.id.spinner)
        editTextDate = findViewById(R.id.editTextDate2)
        buttonAdd = findViewById(R.id.button5)
        taskText = findViewById(R.id.editTextTextMultiLine)

        // Spinner'a memberNames listesini bağla
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Choose a family member") + ("All Family") + family.memberNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        editTextDate.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val ddmmyyyy = "DDMMYYYY"
            private val cal = Calendar.getInstance()

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    var clean = s.toString().replace("[^\\d.]".toRegex(), "")
                    val cleanC = current.replace("[^\\d.]".toRegex(), "")

                    val cl = clean.length
                    var sel = cl
                    var i = 2
                    while (i <= cl && i < 6) {
                        sel++
                        i += 2
                    }
                    // Fix for pressing delete next to a forward slash
                    if (clean == cleanC) sel--

                    if (clean.length < 8) {
                        clean += ddmmyyyy.substring(clean.length)
                    } else {
                        var day = Integer.parseInt(clean.substring(0, 2))
                        var mon = Integer.parseInt(clean.substring(2, 4))
                        var year = Integer.parseInt(clean.substring(4, 8))

                        if (mon > 12) mon = 12
                        cal[Calendar.MONTH] = mon - 1

                        year = if (year < 2024) 2024 else if (year > 2100) 2100 else year
                        cal[Calendar.YEAR] = year

                        day = if (day > cal.getActualMaximum(Calendar.DATE)) cal.getActualMaximum(
                            Calendar.DATE
                        ) else day
                        clean = String.format("%02d%02d%02d", day, mon, year)
                    }

                    clean = String.format(
                        "%s/%s/%s",
                        clean.substring(0, 2),
                        clean.substring(2, 4),
                        clean.substring(4, 8)
                    )

                    sel = if (sel < 0) 0 else sel
                    current = clean
                    editTextDate.setText(current)
                    editTextDate.setSelection(if (sel < current.length) sel else current.length)
                }
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {}
        })


        buttonAdd.setOnClickListener {
            val selectedMember = spinner.selectedItem.toString()
            val enteredDate = editTextDate.text.toString()
            val enteredTask = taskText.text.toString()

            val calToday = Calendar.getInstance()
            val calEnteredDate = Calendar.getInstance()
            calEnteredDate.set(
                enteredDate.substring(6, 10).toInt(),
                enteredDate.substring(3, 5).toInt() - 1,
                enteredDate.substring(0, 2).toInt()
            )

            if (selectedMember == "Choose a family member") {
                Toast.makeText(this, "Please choose a responsible for the task.", Toast.LENGTH_SHORT).show()
            } else if (enteredDate.length != 10 || enteredDate[2] != '/' || enteredDate[5] != '/') {
                Toast.makeText(this, "Write the date in right format (DD/MM/YYYY).", Toast.LENGTH_SHORT).show()
            } else if (enteredDate == "DD/MM/YYYY" || enteredTask.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            } else if (calEnteredDate.before(calToday)) {
                Toast.makeText(this, "Please enter a date later than today.", Toast.LENGTH_SHORT).show()
            } else {
                addTaskToFirestore(selectedMember, enteredDate, enteredTask)
            }
        }
    }

    private fun addTaskToFirestore(selectedMember: String, enteredDate: String, enteredTask: String) {
        // Belirtilen üyenin id'sini depolamak için bir değişken oluştur
        var memberId = ""

        // Firestore'a yeni veri eklemek için bir Map oluştur
        val taskData: HashMap<String, Any> = if (selectedMember != "All Family") {
            hashMapOf(
                "m_dueDate" to enteredDate,
                "m_task_approve" to "0",
                "m_task_done" to "no",
                "m_task_id" to UUID.randomUUID().toString(), // Benzersiz kimlik oluştur
                "m_task_text" to enteredTask
            )
        } else {
            hashMapOf(
                "dueDate" to enteredDate,
                "family_id" to familyId,
                "task_approve" to "0",
                "task_done" to "no",
                "task_id" to UUID.randomUUID().toString(), // Benzersiz kimlik oluştur
                "task_text" to enteredTask
            )
        }

        if (selectedMember != "All Family") {
            db.collection("member")
                .whereEqualTo("family_id", familyId) // family_id'ye göre filtrele
                .whereEqualTo("member_name", selectedMember) // member_name'e göre filtrele
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        memberId = documents.documents[0].id // İlk belgeyi al ve member_id'yi al
                        taskData["member_id"] = memberId // Map'e member_id'yi ekle
                        addTaskToFirestoreHelper(taskData, "member_tasks")
                    } else {
                        // Üye bulunamadı
                        Toast.makeText(this, "Member not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Sorgu yaparken hata oluştu
                    Toast.makeText(this, "Error fetching member: $e", Toast.LENGTH_SHORT).show()
                }
        } else {
            // "All Family" seçildiyse family_tasks tablosuna ekle
            addTaskToFirestoreHelper(taskData, "family_tasks")
        }
    }

    // Firestore'a yeni task eklemek için yardımcı fonksiyon
    private fun addTaskToFirestoreHelper(taskData: HashMap<String, Any>, collectionName: String) {
        db.collection(collectionName)
            .add(taskData)
            .addOnSuccessListener {
                // Veri ekleme başarılı oldu
                Toast.makeText(this, "Task added successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Veri eklerken hata oluştu
                Toast.makeText(this, "Error adding task: $e", Toast.LENGTH_SHORT).show()
            }
    }


}

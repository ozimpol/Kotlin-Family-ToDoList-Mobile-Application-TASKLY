package com.ozan.familytask

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginFamilyActivity : AppCompatActivity() {

    private lateinit var editTextFamilyId: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var loginButton: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_family)

        editTextFamilyId = findViewById(R.id.editTextText6)
        editTextPassword = findViewById(R.id.editTextText7)
        loginButton = findViewById(R.id.button3)

        loginButton.setOnClickListener {
            val familyId = editTextFamilyId.text.toString()
            val password = editTextPassword.text.toString()

            if (familyId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Firestore verilerini kontrol etme
                db.collection("families")
                    .whereEqualTo("family_id", familyId)
                    .whereEqualTo("family_pw", password)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            Toast.makeText(this, "Invalid family ID or password", Toast.LENGTH_SHORT).show()
                        } else {
                            val sharedPref = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("familyId", familyId)
                                apply()
                            }

                            val intent = Intent(this, LoginMemberActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error checking login credentials", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
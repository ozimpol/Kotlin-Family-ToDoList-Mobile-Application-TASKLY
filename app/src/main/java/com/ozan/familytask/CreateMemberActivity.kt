package com.ozan.familytask

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class CreateMemberActivity : AppCompatActivity() {

    private lateinit var editTextNickname: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var imageButton: ImageButton
    private lateinit var createButton: Button
    private lateinit var selectedImageUri: Uri
    private lateinit var storageRef: StorageReference
    private val db = FirebaseFirestore.getInstance()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    selectedImageUri = data.data!!
                    imageButton.setImageURI(selectedImageUri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_member)

        editTextNickname = findViewById(R.id.editTextText2)
        editTextName = findViewById(R.id.editTextText3)
        editTextPassword = findViewById(R.id.editTextText4)
        imageButton = findViewById(R.id.imageButton2)
        createButton = findViewById(R.id.button2)

        editTextPassword.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        storageRef = FirebaseStorage.getInstance().reference

        val sharedPref = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        val familyId = sharedPref.getString("familyId", null)

        if (familyId != null) {

            imageButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                getContent.launch(intent)
            }

            createButton.setOnClickListener {
                val nickname = editTextNickname.text.toString()
                val name = editTextName.text.toString()
                val password = editTextPassword.text.toString()

                if (TextUtils.isEmpty(nickname) || TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else {
                    db.collection("member")
                        .whereEqualTo("member_id", nickname)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                db.collection("member")
                                    .whereEqualTo("family_id", familyId)
                                    .get()
                                    .addOnSuccessListener { familyMembers ->
                                        val memberAuthority = if (familyMembers.isEmpty) {
                                            "Admin"
                                        } else {
                                            "Member"
                                        }

                                        uploadImageAndAddData(nickname, name, password, familyId, memberAuthority)
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.w("CreateMemberActivity", "Error getting family members: ", exception)
                                    }
                            } else {
                                Toast.makeText(this, "Nickname is already in use", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w("CreateFamilyActivity", "Error getting documents: ", exception)
                        }
                }
            }
        } else {
            Toast.makeText(this, "Error: Family ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageAndAddData(
        nickname: String,
        name: String,
        password: String,
        familyId: String,
        memberAuthority: String
    ) {
        val imageRef = storageRef.child("images/$nickname.jpg")

        imageRef.putFile(selectedImageUri)
            .addOnSuccessListener { uploadTask ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()

                    val familyData = hashMapOf(
                        "member_id" to nickname,
                        "member_name" to name,
                        "member_pw" to password,
                        "member_photo" to imageUrl,
                        "family_id" to familyId,
                        "member_authority" to memberAuthority
                    )

                    db.collection("member")
                        .document(nickname)
                        .set(familyData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Member record created successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginMemberActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("CreateMemberActivity", "Error adding family member record to Firestore", exception)
                            Toast.makeText(this, "Error adding family member record to Firestore", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CreateMemberActivity", "Error uploading image to Firebase Storage", exception)
                Toast.makeText(this, "Error uploading image to Firebase Storage", Toast.LENGTH_SHORT).show()
            }
    }
}

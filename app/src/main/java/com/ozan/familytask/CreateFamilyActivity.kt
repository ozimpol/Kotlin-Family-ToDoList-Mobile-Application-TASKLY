package com.ozan.familytask

import android.app.Activity
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

class CreateFamilyActivity : AppCompatActivity() {

    private lateinit var editTextNickname: EditText
    private lateinit var editTextFamilyName: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var imageButton: ImageButton
    private lateinit var createButton: Button
    private lateinit var selectedImageUri: Uri
    private lateinit var storageRef: StorageReference
    private val db = FirebaseFirestore.getInstance()

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
        setContentView(R.layout.activity_create_family)

        editTextNickname = findViewById(R.id.editTextText2)
        editTextFamilyName = findViewById(R.id.editTextText3)
        editTextPassword = findViewById(R.id.editTextText4)
        imageButton = findViewById(R.id.imageButton2)
        createButton = findViewById(R.id.button2)


        editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD


        storageRef = FirebaseStorage.getInstance().reference

        imageButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getContent.launch(intent)
        }

        createButton.setOnClickListener {

            val nickname = editTextNickname.text.toString()
            val familyName = editTextFamilyName.text.toString()
            val password = editTextPassword.text.toString()

            if (TextUtils.isEmpty(nickname) || TextUtils.isEmpty(familyName) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                db.collection("families")
                    .whereEqualTo("family_id", nickname)
                    .get()
                    .addOnSuccessListener { documents ->

                        if (documents.isEmpty) {
                            uploadImageAndAddFamilyData(nickname, familyName, password)
                        } else {
                            Toast.makeText(this, "Nickname is already in use", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("CreateFamilyActivity", "Error getting documents: ", exception)
                    }
            }
        }
    }

    private fun uploadImageAndAddFamilyData(nickname: String, familyName: String, password: String) {
        val imageRef = storageRef.child("images/$nickname.jpg")

        imageRef.putFile(selectedImageUri)
            .addOnSuccessListener { uploadTask ->

                imageRef.downloadUrl.addOnSuccessListener { uri ->

                    val imageUrl = uri.toString()

                    val familyData = hashMapOf(
                        "family_id" to nickname,
                        "family_name" to familyName,
                        "family_pw" to password,
                        "family_photo" to imageUrl
                    )

                    db.collection("families")
                        .document(nickname)
                        .set(familyData)
                        .addOnSuccessListener {

                            Toast.makeText(this, "Family record created successfully", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { exception ->

                            Log.e("CreateFamilyActivity", "Error adding family record to Firestore", exception)
                            Toast.makeText(this, "Error adding family record to Firestore", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->

                Log.e("CreateFamilyActivity", "Error uploading image to Firebase Storage", exception)
                Toast.makeText(this, "Error uploading image to Firebase Storage", Toast.LENGTH_SHORT).show()
            }
    }
}

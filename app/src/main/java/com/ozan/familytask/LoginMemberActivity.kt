package com.ozan.familytask

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LoginMemberActivity : AppCompatActivity() {

    private lateinit var editTextMemberId: EditText
    private lateinit var editTextMemberPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var linkTextView: TextView
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login_member)

        editTextMemberId = findViewById(R.id.editTextText6)
        editTextMemberPassword = findViewById(R.id.editTextText7)
        loginButton = findViewById(R.id.button3)
        linkTextView = findViewById(R.id.linkTextView)

        linkTextView.setOnClickListener {
            val intent = Intent(this, CreateMemberActivity::class.java)
            startActivity(intent)
        }

        linkTextView.paintFlags = linkTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        loginButton.setOnClickListener {
            // EditText'lerin içeriğini al
            val memberId = editTextMemberId.text.toString()
            val memberPassword = editTextMemberPassword.text.toString()

            // Boş olup olmadığını kontrol et
            if (TextUtils.isEmpty(memberId) || TextUtils.isEmpty(memberPassword)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Database sorgusu yap
                db.collection("member")
                    .whereEqualTo("member_id", memberId)
                    .whereEqualTo("member_pw", memberPassword)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.size() > 0) {
                            // Giriş başarılıysa kullanıcı bilgilerini Intent'e ekle
                            val intent = Intent(this, FamilyActivity::class.java)
                            val member = documents.first() // İlk belgeyi alabilirsiniz, çünkü memberId benzersiz olmalıdır
                            val memberAuthority = member.getString("member_authority")
                            val familyId = member.getString("family_id")

                            // Intent'e ek veri ekle
                            intent.putExtra("memberId", memberId)
                            intent.putExtra("memberAuthority", memberAuthority)
                            intent.putExtra("familyId", familyId)

                            // FamilyActivity'e yönlendir
                            startActivity(intent)
                        } else {
                            // Kullanıcı bulunamazsa hata mesajı göster
                            Toast.makeText(this, "Invalid login credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Hata durumunda hata mesajı göster
                        Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}

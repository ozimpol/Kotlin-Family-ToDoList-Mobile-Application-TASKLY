package com.ozan.familytask

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ozan.familytask.model.Member

class family : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FamilyAdapter

    companion object {
        var memberNames: ArrayList<String> = ArrayList() // Static olarak kullanılacak ArrayList
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_family, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Firestore'dan verileri al ve RecyclerView'a bağla
        fetchMembers()

        return view
    }

    private fun fetchMembers() {
        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("member")
        val familyId = requireContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE).getString("familyId", "")

        // FamilyActivity'den alınan familyId ile member tablosundaki family_id verilerini eşleştir
        collectionReference
            .whereEqualTo("family_id", familyId)
            .get()
            .addOnSuccessListener { documents ->
                val members = mutableListOf<Member>()
                memberNames.clear() // Önceki verileri temizle

                for (document in documents) {
                    val memberName = document.getString("member_name")
                    val memberAuthority = document.getString("member_authority")
                    val memberPhoto = document.getString("member_photo")

                    // Verileri aldıktan sonra Member nesnelerini oluşturun ve listeye ekleyin
                    if (memberName != null && memberAuthority != null && memberPhoto != null) {
                        val member = Member(memberName, memberAuthority, memberPhoto)
                        members.add(member)
                        memberNames.add(memberName) // member_name verisini ArrayList'e ekle
                    }
                }

                // Adapter oluştur ve RecyclerView'a bağla
                adapter = FamilyAdapter(members)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                // Hata durumunda uygun şekilde işlem yapın
            }
    }
}

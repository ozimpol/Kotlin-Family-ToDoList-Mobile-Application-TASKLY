package com.ozan.familytask

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.ozan.familytask.databinding.FragmentFamlyBinding
import com.squareup.picasso.Picasso

class famly : Fragment() {

    private lateinit var binding : FragmentFamlyBinding
    private lateinit var textViewFamilyName: TextView
    private lateinit var imageViewFamilyPhoto: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentFamlyBinding.inflate(layoutInflater)
        val view = binding.root

        // Initialize views
        textViewFamilyName = binding.textView2
        imageViewFamilyPhoto = binding.imageView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firestore'dan aile verilerini al
        val db = FirebaseFirestore.getInstance()
        val familyId = requireContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE).getString("familyId", "")

        if (!familyId.isNullOrEmpty()) {
            db.collection("families")
                .document(familyId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val familyName = document.getString("family_name")
                        val familyPhotoUrl = document.getString("family_photo")

                        // Family adını textView2'ye ata
                        textViewFamilyName.text = familyName+"s"

                        // Aile resmini imageView'da göster (Picasso kullanarak)
                        Picasso.get().load(familyPhotoUrl).into(imageViewFamilyPhoto)
                    } else {
                        Log.d("FamilyFragment", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    // Veri alınırken hata oluştu
                    Log.e("FamilyFragment", "Error getting family data", exception)
                }
        } else {
            Log.d("FamilyFragment", "Family ID is null or empty")
        }
    }
}

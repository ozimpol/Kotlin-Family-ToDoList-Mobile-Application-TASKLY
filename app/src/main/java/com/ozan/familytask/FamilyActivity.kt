package com.ozan.familytask

import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ozan.familytask.databinding.ActivityFamilyBinding


class FamilyActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFamilyBinding

    companion object {
        var memberId: String? = null
        var memberAuthority: String? = null
        var familyId: String? = null
        private const val TAG = "familyactivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFamilyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(famly())

        // Gelen Intent'ten verileri al
        val intent = intent
        memberId = intent.getStringExtra("memberId")
        memberAuthority = intent.getStringExtra("memberAuthority")
        familyId = intent.getStringExtra("familyId")

        Log.d(TAG, "member ID: $memberId")

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.mytasks -> replaceFragment(mytasks())
                R.id.family -> replaceFragment(family())
                R.id.familytasks -> replaceFragment(familytasks())

                else ->{

                }

            }
            true
        }
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Fragmentı oluştururken arguments ile memberAuthority değerini ileteceğiz
        val fragment = when (fragment) {
            is mytasks -> {
                mytasks().apply {
                    arguments = Bundle().apply {
                        putString("memberAuthority", memberAuthority)
                        putString("memberId", memberId)
                        putString("familyId", familyId)
                    }
                }
            }
            is family -> {
                family().apply {
                    arguments = Bundle().apply {
                        putString("memberAuthority", memberAuthority)
                        putString("memberId", memberId)
                        putString("familyId", familyId)
                    }
                }
            }
            is familytasks -> {
                familytasks().apply {
                    arguments = Bundle().apply {
                        putString("memberAuthority", memberAuthority)
                        putString("memberId", memberId)
                        putString("familyId", familyId)
                    }
                }
            }
            else -> fragment
        }

        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }

}

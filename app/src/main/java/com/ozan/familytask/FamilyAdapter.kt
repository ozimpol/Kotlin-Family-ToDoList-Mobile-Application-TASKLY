package com.ozan.familytask;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ozan.familytask.model.Member
import com.squareup.picasso.Picasso

class FamilyAdapter(private val members: List<Member>) :
    RecyclerView.Adapter<FamilyAdapter.FamilyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamilyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return FamilyViewHolder(view)
    }

    override fun onBindViewHolder(holder: FamilyViewHolder, position: Int) {
        val member = members[position]
        holder.bind(member)
    }

    override fun getItemCount(): Int {
        return members.size
    }

    class FamilyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvNameItem)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPhoneItem)
        private val imgItem: ImageView = itemView.findViewById(R.id.imgItem)

        fun bind(member: Member) {
            tvName.text = member.name
            tvPhone.text = member.phone
            // Picasso kütüphanesi kullanarak resmi yükleme
            Picasso.get().load(member.imageUrl).into(imgItem)
        }
    }
}


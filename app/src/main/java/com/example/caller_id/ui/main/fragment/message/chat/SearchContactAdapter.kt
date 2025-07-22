package com.example.caller_id.ui.main.fragment.message.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.caller_id.databinding.ItemSearchContactBinding
import com.example.caller_id.model.ContactModel

class SearchContactAdapter(
    var list: MutableList<ContactModel>,
    val onClick: (ContactModel) -> Unit
) : RecyclerView.Adapter<SearchContactAdapter.ContactHolder>() {
    inner class ContactHolder(val binding: ItemSearchContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: ContactModel) {
            binding.tvName.text = contact.name
            binding.tvNumber.text = contact.number
            binding.root.setOnClickListener {
                onClick(contact)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val binding =
            ItemSearchContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        val contact = list[position]
        holder.bind(contact)
    }

    fun submitList(list: MutableList<ContactModel>) {
        this.list.clear()
        this.list = list
        notifyDataSetChanged()
    }
}
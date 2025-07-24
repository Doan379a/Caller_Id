package com.example.caller_id.ui.keypad

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.caller_id.databinding.ItemCallLogBinding
import com.example.caller_id.databinding.ItemRcvContactBinding
import com.example.caller_id.model.ContactModel
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.visible
import com.example.caller_id.widget.visibleOrGone
import kotlin.math.absoluteValue

class KeypadAdapter(var list: MutableList<ContactModel>) :
    RecyclerView.Adapter<KeypadAdapter.ContactViewHolder>() {
    var onClickItem: ((item: ContactModel) -> Unit)? = null
    var onClickCall: ((call: String) -> Unit)? = null

    inner class ContactViewHolder(val binding: ItemRcvContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: ContactModel) {
            binding.imgSms.gone()
            binding.tvAvatar.text = contact.name.firstOrNull()?.uppercaseChar()?.toString()
                ?: contact.number.firstOrNull()?.toString().orEmpty()
            binding.txtName.text = contact.name
            binding.tvPhone.text = contact.number
            binding.cardAvatar.setCardBackgroundColor(getColorFromAddress(contact.number))
            binding.imgCall.tap {
                onClickCall?.invoke(contact.number)
            }
            binding.root.tap {
                onClickItem?.invoke(contact)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding =
            ItemRcvContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size
    fun updateList(list: MutableList<ContactModel>) {
        this.list.clear()
        this.list = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentItem = list[position]
        holder.bind(currentItem)
        val params = holder.binding.root.layoutParams as ViewGroup.MarginLayoutParams
        if (position == itemCount - 1) {
            params.bottomMargin =
                (150 * holder.binding.root.context.resources.displayMetrics.density).toInt()
        } else {
            params.bottomMargin = 0
        }
        holder.binding.root.layoutParams = params
    }

    fun getColorFromAddress(address: String): Int {
        val colors = listOf(
            Color.parseColor("#EF5350"), // Đỏ
            Color.parseColor("#AB47BC"), // Tím
            Color.parseColor("#42A5F5"), // Xanh dương
            Color.parseColor("#26A69A"), // Xanh ngọc
            Color.parseColor("#FFA726"), // Cam
            Color.parseColor("#66BB6A"), // Xanh lá
            Color.parseColor("#FF7043"), // Cam đậm
        )

        val index = (address.hashCode().absoluteValue) % colors.size
        return colors[index]
    }
}
package com.example.caller_id.ui.filtercall

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.caller_id.R
import com.example.caller_id.databinding.ItemRcvFileterCallHomeBinding
import com.example.caller_id.model.CallLogItem
import com.example.caller_id.widget.tap

class FilterCallHomeAdapter(val list: MutableList<CallLogItem>) :
    RecyclerView.Adapter<FilterCallHomeAdapter.FilterCallHomeViewHolder>() {
    var onClickItem: ((callLogItem: CallLogItem) -> Unit)? = null

    inner class FilterCallHomeViewHolder(val binding: ItemRcvFileterCallHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CallLogItem) {
            binding.tvAvatar.text =
                data.name?.firstOrNull()?.uppercaseChar()?.toString() ?: data.number.firstOrNull()
                    ?.toString().orEmpty()
            binding.cardAvatar.setCardBackgroundColor(getColorFromAddress())
            if (data.type == "Incoming") {
                binding.imgIcon.setImageResource(R.drawable.ic_call_incoming)
            } else if (data.type == "Outgoing") {
                binding.imgIcon.setImageResource(R.drawable.ic_call_outgoing)
            } else if (data.type == "Missed") {
                binding.imgIcon.setImageResource(R.drawable.ic_call_missed)
            } else {
                binding.imgIcon.setImageResource(R.drawable.ic_call_reject)
            }
            binding.txtName.text = data.name ?: data.number
            binding.tvDate.text = data.date
            binding.root.tap { onClickItem?.invoke(data) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterCallHomeViewHolder {
        val binding = ItemRcvFileterCallHomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FilterCallHomeViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    fun updateList(list: MutableList<CallLogItem>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: FilterCallHomeViewHolder, position: Int) {
        val currentItem = list[position]
        holder.bind(currentItem)
    }

    fun getColorFromAddress(): Int {
        val colors = listOf(
            Color.parseColor("#EF5350"), // Đỏ
            Color.parseColor("#AB47BC"), // Tím
            Color.parseColor("#42A5F5"), // Xanh dương
            Color.parseColor("#26A69A"), // Xanh ngọc
            Color.parseColor("#FFA726"), // Cam
            Color.parseColor("#66BB6A"), // Xanh lá
            Color.parseColor("#FF7043"), // Cam đậm
        )

        return colors.random()
    }
}
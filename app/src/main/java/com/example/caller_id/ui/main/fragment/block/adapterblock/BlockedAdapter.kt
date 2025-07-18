package com.example.caller_id.ui.main.fragment.block.adapterblock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.caller_id.R
import com.example.caller_id.base.BaseAdapter
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.databinding.ItemBlockedBinding
import com.example.caller_id.databinding.ItemCallLogBinding
import com.example.caller_id.model.CallLogItem
import com.example.caller_id.widget.setDrawableStartWithTint
import com.example.caller_id.widget.tap
import kotlin.math.absoluteValue

class BlockedAdapter(val context: Context,
                     val onClick :(callLogItem: BlockedCalled)->Unit
) :BaseAdapter<ItemBlockedBinding, BlockedCalled>() {
    private var filterList: List<BlockedCalled> = listData
    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemBlockedBinding {
        return ItemBlockedBinding.inflate(inflater, parent, false)
    }

    override fun creatVH(binding: ItemBlockedBinding): RecyclerView.ViewHolder =
        CallVH(binding)

    inner class CallVH(binding: ItemBlockedBinding) : BaseVH<BlockedCalled>(binding) {
        override fun onItemClickListener(data: BlockedCalled) {
            super.onItemClickListener(data)
        }

        override fun bind(data: BlockedCalled) {
            super.bind(data)
            binding.tvAvatar.text = data.number.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            binding.cardAvatar.setCardBackgroundColor(getColorFromAddress())
            if (data.type == "number") {
                binding.txtName.text = data.number
            } else if (data.type == "sender") {
                binding.txtName.text = context.getString(R.string.sender) + " (${data.number})"
            } else if (data.type == "country") {
                binding.txtName.text =
                    context.getString(R.string.country_code) + " (${data.number})"
            } else if (data.type == "numberstart") {
                binding.txtName.text =
                    context.getString(R.string.number_start_with) + " (${data.number})"
            }
            binding.ivClear.tap {
                onClick.invoke(data)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<BlockedCalled>) {
        listData.clear()
        listData.addAll(newList)
        filterList = listData
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filtered(query: String) {
        filterList = if (query.isEmpty()) {
            listData
        } else {
            listData.filter {
                it.number.contains(query, true)
            }
        }
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return filterList.size
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = filterList[position]
        (holder as CallVH).bind(item)
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


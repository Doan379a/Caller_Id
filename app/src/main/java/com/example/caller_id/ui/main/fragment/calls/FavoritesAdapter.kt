package com.example.caller_id.ui.main.fragment.calls

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
import com.example.caller_id.databinding.ItemCallLogBinding
import com.example.caller_id.model.CallLogItem
import com.example.caller_id.widget.gone

class FavoritesAdapter(val context: Context,
                       val onClick :(callLogItem: BlockedCalled)-> Unit
) :BaseAdapter<ItemCallLogBinding, BlockedCalled>() {
    var onClickSms :((sms: String)-> Unit)? = null
    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemCallLogBinding {
        return ItemCallLogBinding.inflate(inflater,parent,false)
    }

    override fun creatVH(binding: ItemCallLogBinding): RecyclerView.ViewHolder =
        CallVH(binding)

    inner class CallVH(binding: ItemCallLogBinding) : BaseVH<BlockedCalled>(binding) {
        override fun onItemClickListener(data: BlockedCalled) {
            super.onItemClickListener(data)
            onClick.invoke(data)
        }

        override fun bind(data: BlockedCalled) {
            super.bind(data)
            binding.txtDate.gone()
            binding.tvAvatar.text = data.name.firstOrNull()?.uppercaseChar()?.toString()  ?: data.number.firstOrNull()?.toString().orEmpty()
            binding.cardAvatar.setCardBackgroundColor(getColorFromAddress())
            binding.txtName.text = if (!data.name.isNullOrBlank()) data.name else data.number
            binding.imgSms.setOnClickListener{
                onClickSms?.invoke(data.number)
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<BlockedCalled>) {
        listData.clear()
        listData.addAll(newList)
        notifyDataSetChanged()
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
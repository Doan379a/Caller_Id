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
import com.example.caller_id.databinding.ItemCallLogBinding
import com.example.caller_id.model.CallLogItem
import com.example.caller_id.widget.setDrawableStartWithTint
import kotlin.math.absoluteValue

class CallLogAdapter(val context: Context,
                     val onClick :(callLogItem: CallLogItem)-> Unit
) :BaseAdapter<ItemCallLogBinding, CallLogItem>() {
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

    inner class CallVH(binding: ItemCallLogBinding) : BaseVH<CallLogItem>(binding) {
        override fun onItemClickListener(data: CallLogItem) {
            super.onItemClickListener(data)
            onClick.invoke(data)
        }

        override fun bind(data: CallLogItem) {
            super.bind(data)
            binding.tvAvatar.text = data.name?.firstOrNull()?.uppercaseChar()?.toString()  ?: data.number.firstOrNull()?.toString().orEmpty()
            binding.cardAvatar.setCardBackgroundColor(getColorFromAddress())
            val countText = if (data.count > 1) " (${data.count})" else ""
            if (data.type == "Incoming"){
                binding.txtDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_incoming, 0, 0, 0)
            }else if (data.type == "Outgoing"){
                binding.txtDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_outgoing, 0, 0, 0)
            }else if (data.type == "Missed"){
                binding.txtDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_missed, 0, 0, 0)
            }else{
                binding.txtDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_call_reject, 0, 0, 0)
            }
            Log.d("dataName", data.name.toString())
            if (data.name != null){
                binding.txtName.text = data.name + countText
            }else{
                binding.txtName.text = data.number + countText
            }
            binding.txtDate.text = data.date
            binding.imgSms.setOnClickListener{
                onClickSms?.invoke(data.number ?: "")
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<CallLogItem>) {
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
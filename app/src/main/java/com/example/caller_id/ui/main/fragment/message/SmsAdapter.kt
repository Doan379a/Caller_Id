package com.example.caller_id.ui.main.fragment.message

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.caller_id.R
import com.example.caller_id.databinding.ItemRcvSmsBinding
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.utils.SmsUtils.formatSmsTimestamp
import com.example.caller_id.utils.SmsUtils.lookupContactName
import com.example.caller_id.utils.SmsUtils.toNational
import java.text.Normalizer.normalize
import kotlin.math.absoluteValue

class SmsAdapter(
    var conversations: MutableList<SmsConversation>,
    private val onClick: (String, String, Int) -> Unit
) :
    RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {

    inner class SmsViewHolder(val binding: ItemRcvSmsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val binding = ItemRcvSmsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SmsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val sms = conversations[position]
        val binding = holder.binding
        val params = holder.binding.root.layoutParams as ViewGroup.MarginLayoutParams
        if (position == itemCount - 1) {
            params.bottomMargin =
                (150 * binding.root.context.resources.displayMetrics.density).toInt()
        } else {
            params.bottomMargin = 0
        }
        holder.binding.root.layoutParams = params
        val local = toNational(sms.address)
        val namePhone = lookupContactName(binding.root.context, local ?: sms.address)

//        binding.tvAddress.text = normalized
        val default = sms.address ?: "?"
        binding.tvAddress.text = if (namePhone !== "") namePhone else local ?: sms.address
        binding.tvBody.text = sms.latestMessage
        binding.tvTime.text = formatSmsTimestamp(binding.root.context, sms.date)
        binding.tvAvatar.text =
            namePhone.firstOrNull()?.uppercaseChar()?.toString() ?: default.firstOrNull()
                ?.uppercaseChar()?.toString() ?: "?"
        val textColor = if (sms.unreadCount > 0) {
            ContextCompat.getColor(binding.root.context, R.color.color_262626)
        } else {
            ContextCompat.getColor(binding.root.context, R.color.color_505050)
        }
        val textBodyColor = if (sms.unreadCount > 0) {
            ContextCompat.getColor(binding.root.context, R.color.color_262626)
        } else {
            ContextCompat.getColor(binding.root.context, R.color.color_7C7E81)
        }
        binding.tvBadge.visibility = if (sms.unreadCount > 0) View.VISIBLE else View.GONE
        binding.tvAddress.setTextColor(textColor)
        binding.tvBody.setTextColor(textBodyColor)
        binding.tvTime.setTextColor(textColor)
        binding.cardAvatar.setCardBackgroundColor(getColorFromAddress(sms.address))
        binding.root.setOnClickListener {
            val color = getColorFromAddress(sms.address)
            onClick(sms.displayName, sms.address, color)
        }
    }

    override fun getItemCount() = conversations.size

    fun updateData(newItems: MutableList<SmsConversation>) {
        Log.d("SmsAdapter", "Cập nhật danh sách ${newItems.size} cuộc hội thoại")
        conversations = newItems
        notifyDataSetChanged()
    }

    val currentList: List<SmsConversation>
        get() = conversations

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

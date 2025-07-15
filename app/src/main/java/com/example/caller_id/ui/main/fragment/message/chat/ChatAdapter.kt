package com.example.caller_id.ui.main.fragment.message.chat

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.caller_id.R
import com.example.caller_id.databinding.ItemMessageReceivedBinding
import com.example.caller_id.databinding.ItemMessageSentBinding
import com.example.caller_id.model.SmsMessage
import com.example.caller_id.model.SmsSendStatus
import com.example.caller_id.utils.SmsUtils.formatSmsTimestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatAdapter (private val items: List<SmsMessage>,
                  private val colorAvatar: Int? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isSentByMe) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sms = items[position]
        val showHeader = position == 0 || !isSameDay(sms.date, items[position - 1].date)


        if (holder is SentViewHolder) {
            val headerText = getHeaderDateText(holder.binding.root.context,sms.date)
            holder.binding.tvDateHeader.apply {
                visibility = if (showHeader) View.VISIBLE else View.GONE
                text = headerText
            }
            holder.binding.tvMessage.text = sms.body
            holder.binding.tvTime.text = formatSmsTimestamp(holder.binding.root.context, sms.date)

            when (sms.status) {
                SmsSendStatus.SENDING -> holder.binding.ivStatus.setImageResource(0)
                SmsSendStatus.SENT -> holder.binding.ivStatus.setImageResource(R.drawable.ic_check)
                SmsSendStatus.FAILED -> holder.binding.ivStatus.setImageResource(R.drawable.ic_failed)
            }
        } else if (holder is ReceivedViewHolder) {
            val headerText = getHeaderDateText(holder.binding.root.context,sms.date)
            holder.binding.tvDateHeader.apply {
                visibility = if (showHeader) View.VISIBLE else View.GONE
                text = headerText
            }
            colorAvatar?.let { holder.binding.cardAvatar.setCardBackgroundColor(it) }
            holder.binding.tvAvatar.text = sms.address.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            holder.binding.tvMessage.text = sms.body
            holder.binding.tvTime.text = formatSmsTimestamp(holder.binding.root.context, sms.date)
        }
    }

    override fun getItemCount() = items.size

    class SentViewHolder(val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root)
    class ReceivedViewHolder(val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root)

    private fun formatTime(timestamp: Long): String {
        val now = Calendar.getInstance()
        val smsTime = Calendar.getInstance().apply { timeInMillis = timestamp }

        return if (now.get(Calendar.DAY_OF_YEAR) == smsTime.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
        } else {
            SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
        }
    }
    private fun isSameDay(ts1: Long, ts2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = ts1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = ts2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun getHeaderDateText(context: Context,timestamp: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        return when {
            isSameDay(timestamp, System.currentTimeMillis()) -> context.getString(R.string.today)
            isSameDay(timestamp, System.currentTimeMillis() - 86400000L) -> context.getString(R.string.yesterday)
            else -> {
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                val year = calendar.get(Calendar.YEAR)
                "$day $month $year"
            }
        }
    }

}

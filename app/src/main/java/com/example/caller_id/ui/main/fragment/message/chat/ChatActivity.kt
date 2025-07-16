package com.example.caller_id.ui.main.fragment.message.chat

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.BlockedNumberContract
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.ActivityChatBinding
import com.example.caller_id.dialog.ChatMenuPopup
import com.example.caller_id.model.SmsMessage
import com.example.caller_id.model.SmsSendStatus
import com.example.caller_id.service.RealTimeSmsReceiver
import com.example.caller_id.service.SmsReceiver
import com.example.caller_id.utils.SmsUtils.getThreadIdForAddress
import com.example.caller_id.utils.SmsUtils.loadSmsByAddress
import com.example.caller_id.widget.getTagDebug
import com.example.caller_id.widget.showSnackBar
import com.example.caller_id.widget.tap
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : BaseActivity<ActivityChatBinding>() {
    private val blockViewModel: BlockViewModel by viewModels()
    private lateinit var address: String
    private var colorAvatar: Int? = null
    private val smsList = mutableListOf<SmsMessage>()
    private lateinit var adapter: ChatAdapter


    // Broadcast nhận khi gửi xong
    private val smsSentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val result = resultCode
            val lastIndex = smsList.indexOfLast { it.status == SmsSendStatus.SENDING }

            if (lastIndex != -1) {
                val sms = smsList[lastIndex]
                sms.status =
                    if (result == Activity.RESULT_OK) SmsSendStatus.SENT else SmsSendStatus.FAILED
                adapter.notifyItemChanged(lastIndex)

                if (result == Activity.RESULT_OK) {
                    // Tự ghi vào DB vì hệ thống không làm
                    val values = ContentValues().apply {
                        put("address", sms.address)
                        put("body", sms.body)
                        put("read", 1)
                        put("date", sms.date)
                        put("type", 2) // 2 = sent
                    }
                    contentResolver.insert(Uri.parse("content://sms/sent"), values)
                    loadMessages()
                    val refreshIntent = Intent("com.example.caller_id.ACTION_REFRESH_SMS")
                    context?.sendBroadcast(refreshIntent)

                }
            }
        }
    }


    // Broadcast nhận khi có tin nhắn đến
    private val smsRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) {
            Log.d("LOIII", "Activity received refresh")
            if (i?.action == RealTimeSmsReceiver.ACTION_REFRESH) {
                showSnackBar("tin mới ")
                loadMessages()
            }
        }
    }

    override fun setViewBinding(): ActivityChatBinding {
        return ActivityChatBinding.inflate(layoutInflater)
    }

    override fun initView() {
        address = intent.getStringExtra("address") ?: ""
        val displayName = intent.getStringExtra("displayName") ?: ""
        Log.d(getTagDebug("DOAN_2"), "Address: $address, Display Name: $displayName")
        colorAvatar = intent.getIntExtra("color", Color.GRAY)
        binding.tvAddress.apply {
            text = displayName
            if (displayName.isBlank()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
            }
        }
        binding.tvPhone.text = address
        adapter = ChatAdapter(smsList, colorAvatar)
        binding.rcvMessages.layoutManager = LinearLayoutManager(this)
        binding.rcvMessages.adapter = adapter
        loadMessages()
    }

    override fun viewListener() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.btnSend.setOnClickListener {
            val text = binding.edtMessage.text.toString()
            if (text.isNotBlank()) {
                sendSmsWithStatus(address, text)
                binding.edtMessage.text.clear()
            }
        }
        binding.ivMenu.tap {
            val popup = ChatMenuPopup(this,
                onSpam = { /* Xử lý */ },
                onDelete = {
                    val threadId = getThreadIdForAddress(this, address) ?: return@ChatMenuPopup
                    val uri = ContentUris.withAppendedId(
                        Uri.parse("content://sms/conversations"),
                        threadId
                    )
                    val deletedCount = contentResolver.delete(uri, null, null)
                    showSnackBar("Đã xóa $deletedCount tin nhắn")
                  finish()

                },
                onBlock = {
                    val canBlock = BlockedNumberContract.canCurrentUserBlockNumbers(this)
                    if (!canBlock) {
                        showSnackBar("Thiết bị không hỗ trợ chặn số")
                        return@ChatMenuPopup
                    }
                    try {
//                        val values = ContentValues().apply {
//                            put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, address)
//                        }
//                        val uri = contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
                        blockViewModel.block(address)
                        showSnackBar("Đã chặn số $address")
//                        Log.d(getTagDebug("DOAN_2"), "Chặn số thành công: $uri")
//                        finish()
                    } catch (e: Exception) {
                        showSnackBar("Không thể chặn số này: ${e.message}")
                        Log.d(getTagDebug("DOAN_2"), "Chặn số thành công: $e.message")

                    }
                }
            )
            popup.showAtView(binding.ivMenu)
        }
    }

    override fun dataObservable() {}

    private fun loadMessages() {
        smsList.clear()
        val loaded = loadSmsByAddress(this, address)
        smsList.addAll(loaded)
        Log.d(getTagDebug("DOAN_2"), "Loaded ${loaded.size} messages for address: $address")
        loaded.filter { !it.read && !it.isSentByMe }.forEach {
            markSmsAsRead(this, it.address, it.body)
        }
        adapter.notifyItemInserted(smsList.size - 1)
        binding.rcvMessages.scrollToPosition(smsList.size - 1)
    }

    // Gửi tin nhắn
    private fun sendSmsWithStatus(address: String, body: String) {
        val sms = SmsMessage(
            address = address,
            body = body,
            date = System.currentTimeMillis(),
            read = true,
            isSentByMe = true,
            status = SmsSendStatus.SENDING
        )

        val sentIntent = Intent("SMS_SENT")
        val sentPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            sentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        SmsManager.getDefault().sendTextMessage(address, null, body, sentPendingIntent, null)

        smsList.add(sms)
        adapter.notifyItemInserted(smsList.size - 1)
        binding.rcvMessages.scrollToPosition(smsList.size - 1)
    }




    override fun onResume() {
        super.onResume()
        registerReceiver(smsSentReceiver, IntentFilter("SMS_SENT"))
        Log.d("LOIII", "Register refresh receiver")
        ContextCompat.registerReceiver(
            this,
            smsRefreshReceiver,
            IntentFilter(RealTimeSmsReceiver.ACTION_REFRESH),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()
        Log.d("LOIII", "Unregister refresh receiver")
        unregisterReceiver(smsSentReceiver)
        unregisterReceiver(smsRefreshReceiver)
    }

    private fun markSmsAsRead(context: Context, from: String, body: String) {
        val uri = Uri.parse("content://sms/inbox")
        val selection = "address = ? AND body = ? AND read = 0"
        val args = arrayOf(from, body)
        val values = ContentValues().apply { put("read", 1) }
        val updated = context.contentResolver.update(uri, values, selection, args)
        Log.d("SMS", "Đã đánh dấu $updated tin nhắn là đã đọc")
    }
}

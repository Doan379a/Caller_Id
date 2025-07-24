package com.example.caller_id.ui.main.fragment.message.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.provider.BlockedNumberContract
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.base.BaseActivity2
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.ActivityChatBinding
import com.example.caller_id.dialog.ChatMenuPopup
import com.example.caller_id.model.SmsMessage
import com.example.caller_id.model.SmsSendStatus
import com.example.caller_id.service.RealTimeSmsReceiver
import com.example.caller_id.utils.SmsUtils.getCheckAddress
import com.example.caller_id.utils.SmsUtils.getThreadIdForAddress
import com.example.caller_id.utils.SmsUtils.isShortCode
import com.example.caller_id.utils.SmsUtils.loadSmsByAddress
import com.example.caller_id.utils.SmsUtils.lookupContactName
import com.example.caller_id.utils.SmsUtils.markSmsAsRead
import com.example.caller_id.utils.SmsUtils.normalizePhone
import com.example.caller_id.widget.getLogDebug
import com.example.caller_id.widget.getTagDebug
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.hideKeyboard
import com.example.caller_id.widget.showSnackBar
import com.example.caller_id.widget.showToast
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.visible
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.i18n.phonenumbers.PhoneNumberUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class ChatAllActivity : BaseActivity2<ActivityChatBinding>() {
    private val blockViewModel: BlockViewModel by viewModels()
    private lateinit var address: String
    private var displayName: String = ""
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


    private fun handleSendToIntent(intent: Intent) {
        Log.d("Chat", "handleSendToIntent called: action=${intent.action}, data=${intent.data}")
        if (intent.action == Intent.ACTION_SENDTO) {
            intent.data?.schemeSpecificPart?.let { number ->
                val pre = intent.getStringExtra("sms_body") ?: ""
                Log.d("Chat", "SENDTO intent: $number, prebody: \"$pre\"")
                val normalized = normalizePhone(number) ?: number
                address = normalized
                binding.ivMenu.gone()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun initView() {
        address =getCheckAddress( intent.getStringExtra("address") ?: "")
        getLogDebug("LOI_CHAT_ALL","address $address")
        handleSendToIntent(intent)
//        val local = getCheckAddress(address)
        colorAvatar = intent.getIntExtra("color", Color.parseColor("#42A5F5"))
        val name = lookupContactName(this, address )
        displayName = if (name.isNotBlank()) name else address


        binding.tvAddress.text = displayName
        binding.tvPhone.text =  address
        Log.d(getTagDebug("DOAN_2"), "Address: $address, Display Name: $displayName,name${name}")
        adapter = ChatAdapter(smsList, colorAvatar)
        binding.rcvMessages.layoutManager = LinearLayoutManager(this)
        binding.rcvMessages.adapter = adapter
        loadMessages()

    }

    override fun viewListener() {
        binding.root.setOnClickListener {
            hideKeyboard()
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.btnSend.setOnClickListener {
            val text = binding.edtMessage.text.toString()
            if (text.isNotBlank()) {
                sendSmsWithStatus(address, text)
                binding.edtMessage.text.clear()
            }else{
                showToast("Vui lòng nhập tin nhắn")
            }
        }
        binding.ivMenu.tap {
            val popup = ChatMenuPopup(this,
                onSpam = {
                    blockViewModel.spamSms(address)
                    finish()
                },
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
                        finish()
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
        getLogDebug("NewChat", "Sending to: ${getCheckAddress(address)}")
        val loaded = loadSmsByAddress(this, getCheckAddress(address))

        Log.d(getTagDebug("DOAN_2"), "Loaded ${loaded.size} messages for address: ${getCheckAddress(address)}")
        loaded.filter { !it.read && !it.isSentByMe }.forEach {
            markSmsAsRead(this, it.address, it.body)
        }
        smsList.addAll(loaded)
        adapter.notifyDataSetChanged()
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
        val smsManager=getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(address, null, body, sentPendingIntent, null)

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


}

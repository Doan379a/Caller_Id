package com.example.caller_id.ui.main.fragment.message.chat

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseActivity2
import com.example.caller_id.databinding.ActivityNewChatBinding
import com.example.caller_id.model.ContactModel
import com.example.caller_id.model.SmsMessage
import com.example.caller_id.model.SmsSendStatus
import com.example.caller_id.service.RealTimeSmsReceiver
import com.example.caller_id.utils.SmsUtils.getCheckAddress
import com.example.caller_id.utils.SmsUtils.loadContacts
import com.example.caller_id.utils.SmsUtils.loadConversationAddresses
import com.example.caller_id.utils.SmsUtils.loadSmsByAddress
import com.example.caller_id.utils.SmsUtils.lookupContactName
import com.example.caller_id.utils.SmsUtils.markSmsAsRead
import com.example.caller_id.utils.SmsUtils.toNational
import com.example.caller_id.widget.getLogDebug
import com.example.caller_id.widget.getTagDebug
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.showSnackBar
import com.example.caller_id.widget.showToast
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.visible
import com.example.caller_id.widget.visibleOrGone
import kotlinx.coroutines.launch

class NewChatActivity : BaseActivity2<ActivityNewChatBinding>() {
    private val smsList: MutableList<SmsMessage> = mutableListOf()
    private val smsAdapter: ChatAdapter by lazy {
        ChatAdapter(
            smsList,
            Color.parseColor("#42A5F5")
        )
    }
    private var contactModel = mutableListOf<ContactModel>()
    private var address: String = ""
    private lateinit var adapterContact: SearchContactAdapter
    override fun setViewBinding(): ActivityNewChatBinding {
        return ActivityNewChatBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.rcvMessages.layoutManager = LinearLayoutManager(this)
        binding.rcvMessages.adapter = smsAdapter
        loadMessages()
        lifecycleScope.launch {
            val listContacts = loadContacts(this@NewChatActivity)
            val listSmsAddress = loadConversationAddresses(this@NewChatActivity)
            val merged = mutableListOf<ContactModel>()

            listSmsAddress.forEach { raw ->
                val normalized = getCheckAddress(raw) // chuẩn hóa +84/0
                val name = lookupContactName(this@NewChatActivity, normalized)
                val displayName = name.takeIf { it.isNotBlank() } ?: normalized
                merged += ContactModel(name = displayName, number = normalized)
            }

            listContacts.forEach { c ->
                val normalized = getCheckAddress(c.number)
                if (merged.none { it.number == normalized }) {
                    val displayName = lookupContactName(this@NewChatActivity, normalized)
                        .takeIf { it.isNotBlank() } ?: normalized
                    merged += ContactModel(name = displayName, number = normalized)
                }
            }

            contactModel = merged
            adapterContact = SearchContactAdapter(merged.toMutableList()) { data ->
                Log.d("DOAN_2", "contact data: $data")
                val name = lookupContactName(this@NewChatActivity, data.number)
                val displayName = name.takeIf { it.isNotBlank() } ?: data.number
                address = data.number
                binding.edtSearch.setText(displayName)
                loadMessages()
                binding.edtSearch.clearFocus()
                binding.edtMessage.requestFocus()
                binding.includeSearchContact.searchContact.gone()
                binding.rcvMessages.visible()
            }
            Log.d(getTagDebug("DOAN_2"), "List size: ${listContacts?.size}")
            binding.includeSearchContact.rcvSearchContact.layoutManager =
                LinearLayoutManager(this@NewChatActivity)
            binding.includeSearchContact.rcvSearchContact.adapter = adapterContact
            search(contactModel.toMutableList())
        }

    }

    private fun search(list: MutableList<ContactModel>) = binding.apply {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().normalize().lowercase()
                Log.d(getTagDebug("DOAN_2"), "Query: $query")
                val listQuery = list.filter { c ->
                    c.name.normalize().lowercase().contains(query) || c.number.normalize()
                        .removePrefix("+84").contains(query)
                }
                adapterContact.submitList(listQuery.toMutableList())
                val show = query.isNotEmpty() && listQuery.isNotEmpty()
                includeSearchContact.root.visibleOrGone(show)
                rcvMessages.gone()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun viewListener() {
        binding.ivBack.tap {
            finish()
        }
        binding.btnSend.setOnClickListener {
            val text = binding.edtMessage.text.toString()
            val textContact = binding.edtSearch.text.toString()
            if (textContact.isNotEmpty()) {
                if (text.isNotBlank()) {
                    sendSmsWithStatus()
                    binding.edtMessage.text.clear()
                } else {
                    showToast("bạn chưa nhập gì ")
                }
            } else {
                showToast("bạn chưa chọn contact")
            }
        }
    }

    private fun loadMessages() {
        smsList.clear()
        val loaded = loadSmsByAddress(this, getCheckAddress(address))

        Log.d(
            getTagDebug("DOAN_2"),
            "Loaded ${loaded.size} messages for address: ${getCheckAddress(address)}"
        )
        loaded.filter { !it.read && !it.isSentByMe }.forEach {
            markSmsAsRead(this, it.address, it.body)
        }
        smsList.addAll(loaded)
        smsAdapter.notifyDataSetChanged()
        binding.rcvMessages.scrollToPosition(smsList.size - 1)
    }

    override fun dataObservable() {
    }

    private fun sendSmsWithStatus() {
        val dest = address.trim()
        val body = binding.edtMessage.text.toString()
        getLogDebug("NewChat", "dest: $dest, body: $body")
        if (dest.isBlank() || body.isBlank()) {
            showSnackBar("Vui lòng nhập số và nội dung")
            return
        }
        val sms = SmsMessage(
            address = dest,
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
        val smsManager = getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(dest, null, body, sentPendingIntent, null)

        smsList.add(sms)
        smsAdapter.notifyItemInserted(smsList.size - 1)
        binding.rcvMessages.scrollToPosition(smsList.size - 1)
    }

    // Broadcast nhận khi gửi xong
    private val smsSentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val result = resultCode
            val lastIndex = smsList.indexOfLast { it.status == SmsSendStatus.SENDING }

            if (lastIndex != -1) {
                val sms = smsList[lastIndex]
                sms.status =
                    if (result == Activity.RESULT_OK) SmsSendStatus.SENT else SmsSendStatus.FAILED
                smsAdapter.notifyItemChanged(lastIndex)

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
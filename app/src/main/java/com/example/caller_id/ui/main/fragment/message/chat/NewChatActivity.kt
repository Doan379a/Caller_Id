package com.example.caller_id.ui.main.fragment.message.chat

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseActivity2
import com.example.caller_id.databinding.ActivityNewChatBinding
import com.example.caller_id.model.CallLogItem
import com.example.caller_id.model.ContactModel
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.model.SmsMessage
import com.example.caller_id.model.SmsSendStatus
import com.example.caller_id.service.RealTimeSmsReceiver
import com.example.caller_id.ui.main.fragment.calls.CallLogAdapter
import com.example.caller_id.ui.main.fragment.calls.fragmentcall.RecentsFragment
import com.example.caller_id.ui.main.fragment.message.SmsAdapter
import com.example.caller_id.utils.SmsUtils.loadContacts
import com.example.caller_id.utils.SmsUtils.loadConversationAddresses
import com.example.caller_id.utils.SmsUtils.loadSmsByAddress
import com.example.caller_id.utils.SmsUtils.markSmsAsRead
import com.example.caller_id.utils.SmsUtils.toNational
import com.example.caller_id.utils.SystemUtil
import com.example.caller_id.widget.getTagDebug
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.showSnackBar
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.visible
import com.example.caller_id.widget.visibleOrGone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewChatActivity : BaseActivity2<ActivityNewChatBinding>() {
    private val smsList: MutableList<SmsMessage> = mutableListOf()
    private val smsAdapter: ChatAdapter by lazy { ChatAdapter(smsList) }
    private var contactModel = mutableListOf<ContactModel>()
    private var address: String = ""
    private lateinit var adapterContact: SearchContact
    override fun setViewBinding(): ActivityNewChatBinding {
        return ActivityNewChatBinding.inflate(layoutInflater)
    }

    override fun initView() {

        binding.rcvMessages.layoutManager = LinearLayoutManager(this)
        binding.rcvMessages.adapter = smsAdapter
        loadMessages()
        lifecycleScope.launch {
            val list = loadContacts(this@NewChatActivity)
            val test = loadConversationAddresses(this@NewChatActivity)
            Log.d("DOAN_7", "test: $test")
            test.forEach {
                contactModel += ContactModel(name = it, number = it)
            }
            contactModel.addAll(list)
            adapterContact = SearchContact(contactModel) { data ->
                Log.d("DOAN_2", "contact data: $data")
                address = data.number
                binding.edtSearch.setText(address)
                loadMessages()
                binding.includeSearchContact.searchContact.gone()
                binding.rcvMessages.visible()
            }
            Log.d(getTagDebug("DOAN_2"), "List size: ${list?.size}")
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
                binding.includeSearchContact.root.visibleOrGone(show)
                binding.rcvMessages.gone()
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
            if (text.isNotBlank()) {
                sendSmsWithStatus()
            }
        }
    }

    private fun loadMessages() {
        smsList.clear()
        var listLoaded = mutableListOf<SmsMessage>()
        fun fetchFor(arg: String) {
            listLoaded = loadSmsByAddress(this, arg).toMutableList()
            Log.d(getTagDebug("DOAN_2"), "Loaded ${listLoaded.size} messages for address: $arg")
        }

        Log.d(getTagDebug("DOAN_2"), "Loaded ${listLoaded.size} messages for address: $address")
        listLoaded.filter { !it.read && !it.isSentByMe }.forEach {
            markSmsAsRead(this, it.address, it.body)
        }
        fetchFor(address)
        if (smsList.isNotEmpty()) {
            smsList.addAll(listLoaded)
        }

        val alt = toNational(address)
        alt?.let { fetchFor(it) }
        smsList.addAll(listLoaded)
        smsAdapter.notifyDataSetChanged()
        binding.rcvMessages.scrollToPosition(smsList.size - 1)
    }

    override fun dataObservable() {
    }
    private fun sendSmsWithStatus() {
        val dest = address.normalize().trim()
        val body = binding.edtMessage.text.toString()

        if (dest.isBlank() || body.isBlank()) {
            showSnackBar("Vui lòng nhập số và nội dung")
            return
        }

        // Nếu là short code nhỏ (3–6 chữ số), dùng SMS Intent để đảm bảo gửi
        if (dest.length < 7 && dest.all { it.isDigit() }) {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$dest"))
            intent.putExtra("sms_body", body)
            startActivity(intent)
            return
        }

        val sms = SmsMessage(dest, body, System.currentTimeMillis(), true, true, SmsSendStatus.SENDING)
        smsList += sms
        smsAdapter.notifyItemInserted(smsList.lastIndex)
        binding.rcvMessages.scrollToPosition(smsList.lastIndex)
        binding.edtMessage.text?.clear()

        val sentPI = PendingIntent.getBroadcast(this, 0, Intent("SMS_SENT"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        SmsManager.getDefault().sendTextMessage(dest, null, body, sentPI, null)
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
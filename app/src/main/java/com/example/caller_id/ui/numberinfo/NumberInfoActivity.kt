package com.example.caller_id.ui.numberinfo

import android.content.pm.ActivityInfo
import android.provider.CallLog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityNumberInfoBinding
import com.example.caller_id.model.CallLogItem
import com.example.caller_id.utils.SystemUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NumberInfoActivity: BaseActivity<ActivityNumberInfoBinding>() {
    private var number = ""
    private lateinit var adapter: NumberInfoAdapter
    override fun setViewBinding(): ActivityNumberInfoBinding {
        return ActivityNumberInfoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        number = intent.getStringExtra("number") ?: ""
        binding.txtName.text = number
        adapter = NumberInfoAdapter(this) { callLogItem ->

        }
        binding.rcvRecent.layoutManager = LinearLayoutManager(this)
        binding.rcvRecent.adapter= adapter
        adapter.updateList(getCallLogsForNumber(number))
    }

    override fun viewListener() {
    }

    private fun getCallLogsForNumber(number: String): List<CallLogItem> {
        val callLogs = mutableListOf<CallLogItem>()

        val projection = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION
        )

        val selection = "${CallLog.Calls.NUMBER} LIKE ?"
        val selectionArgs = arrayOf("%$number%")

        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CallLog.Calls.DATE} DESC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                val rawDate = it.getLong(dateIndex)

                val local = SystemUtil.getPreLanguage(this) ?: "en"
                val sdf = SimpleDateFormat("dd MMM", Locale(local))
                val dateFormatted = sdf.format(Date(rawDate))

                val type = when (it.getInt(typeIndex)) {
                    CallLog.Calls.INCOMING_TYPE -> "Incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                    CallLog.Calls.MISSED_TYPE -> "Missed"
                    else -> "Other"
                }

                callLogs.add(
                    CallLogItem(name, number, dateFormatted, type, rawDate)
                )
            }
        }

        return callLogs
    }

    override fun dataObservable() {
    }
}
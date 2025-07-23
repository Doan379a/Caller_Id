package com.example.caller_id.ui.main.fragment.calls.fragmentcall

import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentRecentsBinding
import com.example.caller_id.model.CallLogItem
import com.example.caller_id.ui.main.fragment.calls.CallLogAdapter
import com.example.caller_id.ui.main.fragment.message.chat.ChatAllActivity
import com.example.caller_id.ui.numberinfo.NumberInfoActivity
import com.example.caller_id.utils.SmsUtils.normalizePhone
import com.example.caller_id.utils.SystemUtil
import com.example.caller_id.widget.showSnackBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentsFragment : BaseFragment<FragmentRecentsBinding>() {
    private lateinit var adapter: CallLogAdapter
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRecentsBinding {
        return FragmentRecentsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val list = getCallLogs()
        adapter = CallLogAdapter(requireContext()) { data ->
                val intent = Intent(requireActivity(),NumberInfoActivity::class.java).apply {
                    putExtra("number", data.number)
                }
            startActivity(intent)
        }
        binding.rcv.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcv.adapter = adapter
        adapter.updateList(list)
        adapter.onClickSms = {sms->
            val normalized = normalizePhone(sms) ?: sms
            val smsIntent = Intent(requireActivity(), ChatAllActivity::class.java).apply {
                putExtra("address", normalized)
            }
            startActivity(smsIntent)

        }
    }

    override fun viewListener() {

    }

    fun getCallLogs(): List<CallLogItem> {
        val callLogList = mutableListOf<CallLogItem>()
        val resolver = requireActivity().contentResolver
        val cursor = resolver.query(
            CallLog.Calls.CONTENT_URI,
            null, null, null,
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

                val local = SystemUtil.getPreLanguage(requireActivity()) ?: "en"
                val sdf = SimpleDateFormat("dd MMM", Locale(local))
                val dateFormatted = sdf.format(Date(rawDate))

                val type = when (it.getInt(typeIndex)) {
                    CallLog.Calls.INCOMING_TYPE -> "Incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                    CallLog.Calls.MISSED_TYPE -> "Missed"
                    else -> "Other"
                }

                callLogList.add(
                    CallLogItem(name, number, dateFormatted, type, rawDate)
                )
            }
        }

        val groupedList = mutableListOf<CallLogItem>()
        for (item in callLogList) {
            val last = groupedList.lastOrNull()
            if (last != null && last.number == item.number && last.type == item.type) {
                groupedList[groupedList.size - 1] = last.copy(count = last.count + 1)
            } else {
                groupedList.add(item)
            }
        }

        return groupedList
    }


    override fun dataObservable() {
    }

}
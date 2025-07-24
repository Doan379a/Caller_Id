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
import com.example.caller_id.utils.SmsUtils.getCallLogs
import com.example.caller_id.utils.SmsUtils.getCheckAddress
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
        val list = getCallLogs(requireActivity())
        adapter = CallLogAdapter(requireContext()) { data ->
                val intent = Intent(requireActivity(),NumberInfoActivity::class.java).apply {
                    putExtra("number", data.number)
                    putExtra("name", data.name)
                }
            startActivity(intent)
        }
        binding.rcv.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcv.adapter = adapter
        adapter.updateList(list)
        adapter.onClickSms = { sms ->
            val normalized = getCheckAddress(sms)
            val smsIntent = Intent(requireActivity(), ChatAllActivity::class.java).apply {
                putExtra("address", normalized)
            }
            startActivity(smsIntent)

        }
    }

    override fun viewListener() {

    }


    override fun onResume() {
        super.onResume()
        val list = getCallLogs()
        adapter.updateList(list)
    }
    override fun dataObservable() {
    }

}
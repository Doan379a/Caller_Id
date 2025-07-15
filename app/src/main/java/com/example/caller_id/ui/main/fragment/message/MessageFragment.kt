package com.example.caller_id.ui.main.fragment.message

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.R
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentMessageBinding
import com.example.caller_id.service.RealTimeSmsReceiver
import com.example.caller_id.service.SmsReceiver
import com.example.caller_id.ui.main.MainActivity
import com.example.caller_id.ui.main.fragment.message.chat.ChatActivity
import com.example.caller_id.utils.SmsUtils.getGroupedSmsInbox
import com.example.caller_id.widget.showSnackBar

class MessageFragment : BaseFragment<FragmentMessageBinding>() {
    private lateinit var adapter: SmsAdapter

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMessageBinding {
        return FragmentMessageBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val list = getGroupedSmsInbox(requireActivity())
        adapter = SmsAdapter(list) { displayName,address,color ->
            val intent = Intent(requireActivity(), ChatActivity::class.java).apply {
                putExtra("displayName", displayName)
                putExtra("address", address)
                putExtra("color", color)
            }
            startActivity(intent)

        }
        binding.rvSms.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvSms.adapter = adapter
    }

    override fun viewListener() {

    }
    private fun refreshSmsList() {
        val list = getGroupedSmsInbox(requireActivity())
        adapter.updateData(list)
        Log.d("kkkk", "Refresh: total=${list.size}, first=${list.firstOrNull()?.latestMessage}")

    }
    override fun onResume() {
        super.onResume()
        ContextCompat.registerReceiver(
            requireActivity(),
            smsRefreshReceiver,
            IntentFilter(RealTimeSmsReceiver.ACTION_REFRESH),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        Log.d("kkkk", "Refresh:loooo")
        refreshSmsList()
        (requireActivity() as MainActivity).loadUnreadSmsCount()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(smsRefreshReceiver)
    }



    override fun dataObservable() {
    }
    private val smsRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, i: Intent?) {
            if (i?.action == RealTimeSmsReceiver.ACTION_REFRESH) {
                requireActivity().showSnackBar("tin mới ")
                refreshSmsList()
                (requireActivity() as MainActivity).loadUnreadSmsCount()
            }
        }
    }

}
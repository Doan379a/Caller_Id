package com.example.caller_id.ui.main.fragment.message

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.map
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.R
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentMessageBinding
import com.example.caller_id.dialog.MessagePopup
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.service.RealTimeSmsReceiver
import com.example.caller_id.service.SmsReceiver
import com.example.caller_id.ui.main.MainActivity
import com.example.caller_id.ui.main.fragment.message.chat.ChatActivity
import com.example.caller_id.utils.SmsUtils.getGroupedSmsInbox
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.showSnackBar
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.visible
import com.example.caller_id.widget.visibleOrGone
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale.filter

@AndroidEntryPoint
class MessageFragment : BaseFragment<FragmentMessageBinding>() {
    private lateinit var adapter: SmsAdapter
    private lateinit var blockAdapter: SmsAdapter
    private val vm: BlockViewModel by activityViewModels()
    private var intSelector: Int = 0
    private  lateinit var popup: MessagePopup
    private var listAll:MutableList<SmsConversation> = mutableListOf()
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMessageBinding {
        return FragmentMessageBinding.inflate(layoutInflater)
    }

    override fun initView() {
        rcvSetUp()
        search()
        popupSetUp()

    }

    override fun viewListener() {
        binding.imgMenu.tap {
            Log.d("MSG", "Before showing popup, 3333intSelector=$intSelector")
            popup.setType(intSelector)
            popup.showAtView(binding.imgMenu)
        }
    }

    private fun rcvSetUp() = binding.apply {
        adapter = SmsAdapter(mutableListOf()) { displayName, address, color ->
            val intent = Intent(requireActivity(), ChatActivity::class.java).apply {
                putExtra("displayName", displayName)
                putExtra("address", address)
                putExtra("color", color)
            }
            startActivity(intent)

        }
        rvSms.layoutManager = LinearLayoutManager(requireActivity())
        rvSms.adapter = adapter

        blockAdapter = SmsAdapter(mutableListOf()) { displayName, address, color ->
            val intent = Intent(requireActivity(), ChatActivity::class.java).apply {
                putExtra("displayName", displayName)
                putExtra("address", address)
                putExtra("color", color)
            }
            startActivity(intent)
        }
        rvBlock.adapter = blockAdapter
        rvBlock.layoutManager = LinearLayoutManager(requireActivity())
        vm.inboxFiltered.observe(viewLifecycleOwner) { smsList ->
            Log.d("Doan_1", "Block list size: ${smsList.size}")
            listAll= smsList.toMutableList()
            adapter.updateData(smsList.toMutableList())
        }
        vm.blockFiltered.observe(viewLifecycleOwner) { list ->
            if (intSelector == 2) {
                listAll= list.toMutableList()
                Log.d("Doan_1", "Block list size222: ${list.size}")
                blockAdapter.updateData(list.toMutableList())
                binding.rvBlock.visibleOrGone(list.isNotEmpty())
                binding.ctlBlock.visibleOrGone(list.isEmpty())
            }
        }
    }

    private fun search() = binding.apply {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().normalize().lowercase().trim()
                val filtered = listAll.filter { app ->
                    app.address.normalize().lowercase().contains(query)
                }
                adapter.updateData(filtered.toMutableList())
                if(intSelector==0) {
                    if (filtered.isEmpty()) {
                        tvNodata.visible()
                        rvSms.gone()
                    } else {
                        tvNodata.gone()
                        rvSms.visible()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun popupSetUp()=binding.apply {
        popup = MessagePopup(requireActivity(), intSelector,
            onAllMessages = {
                mesSpamBlockLayout(0)
                requireActivity().showSnackBar("Chức năng onAllMessages đang code")
            },
            onSpam = {
                requireActivity().showSnackBar("Chức năng onSpam")
                mesSpamBlockLayout(1)
            }, onBlock = {
                requireActivity().showSnackBar("Chức năng onBlock đang code")
                mesSpamBlockLayout(2)
            }
        )
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
        vm.refreshInbox()
        vm.refreshBlocked()
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
                vm.refreshInbox()
                vm.refreshBlocked()
                (requireActivity() as MainActivity).loadUnreadSmsCount()
            }
        }
    }

    private fun mesSpamBlockLayout(int: Int) = binding.apply {
        intSelector = int
        Log.d("MSG", "Before showing popup, intSelector=$intSelector")
        when (int) {
            0 -> {
                tvTitle.text = getString(R.string.messages)
                rvSms.visible()
                ctlBlock.gone()
                ctlSpam.gone()
            }

            1 -> {
                tvTitle.text = getString(R.string.spam)
                binding.rvSms.gone()
                binding.ctlBlock.gone()
                binding.ctlSpam.visible()
            }

            2 -> {
                tvTitle.text = getString(R.string.block)
                binding.rvSms.gone()
                binding.ctlBlock.visible()
                binding.ctlSpam.gone()
                vm.refreshBlocked()
            }
        }
    }
}
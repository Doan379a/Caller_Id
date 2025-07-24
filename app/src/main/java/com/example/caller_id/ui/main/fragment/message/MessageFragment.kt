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
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentMessageBinding
import com.example.caller_id.dialog.FilterCallHomePopup
import com.example.caller_id.dialog.MessagePopup
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.service.RealTimeSmsReceiver
import com.example.caller_id.service.SmsReceiver
import com.example.caller_id.ui.main.MainActivity
import com.example.caller_id.ui.main.fragment.message.chat.NewChatActivity
import com.example.caller_id.widget.getTagDebug
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.showSnackBar
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.visible
import com.example.caller_id.widget.visibleOrGone
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale.filter

@AndroidEntryPoint
class MessageFragment : BaseFragment<FragmentMessageBinding>() {


    private val vm: BlockViewModel by activityViewModels()
    private var intSelector: Int = 0
    private lateinit var popup: MessagePopup
    private var listAll: MutableList<SmsConversation> = mutableListOf()

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMessageBinding {
        return FragmentMessageBinding.inflate(layoutInflater)
    }

    override fun initView() {

        binding.viewPager2.isUserInputEnabled = false
        binding.viewPager2.adapter = MessageAdapterViewPager(requireActivity())
        binding.viewPager2.currentItem = 0
        binding.viewPager2.offscreenPageLimit = 3
        viewModelSetUp()
        search()
        popupSetUp()

    }

    override fun viewListener() {
        binding.imgPopup.tap {
            val popup= FilterCallHomePopup(requireActivity())
            popup.showAtView(binding.imgPopup)
        }
        binding.imgNewMessage.tap {
            val intent = Intent(requireActivity(), NewChatActivity::class.java)
            startActivity(intent)
        }
        binding.imgMenu.tap {
            Log.d("MSG", "Before showing popup, 3333intSelector=$intSelector")
            popup.setType(binding.viewPager2.currentItem)
            popup.showAtView(binding.imgMenu)
        }
    }

    private fun viewModelSetUp() = binding.apply {
//        vm.inboxFiltered.observe(viewLifecycleOwner) { smsList ->
//            if (viewPager2.currentItem == 0) {
//                listAll = smsList.toMutableList()
//            }
//        }
//        vm.blockFiltered.observe(viewLifecycleOwner) { list ->
//            if (viewPager2.currentItem == 2) {
//                listAll = list.toMutableList()
//            }
//        }
    }

    private fun search() = binding.apply {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().normalize().lowercase().trim()
                val filtered = listAll.filter { app ->
                    app.address.normalize().lowercase().contains(query)
                }
                vm.searchMessage(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun popupSetUp() = binding.apply {
        popup = MessagePopup(requireActivity(), viewPager2.currentItem,
            onAllMessages = {
                viewPager2.currentItem = 0
//                vm.refreshInbox()
                requireActivity().showSnackBar("Chức năng onAllMessages đang code")
            },
            onSpam = {
                requireActivity().showSnackBar("Chức năng onSpam")
                viewPager2.currentItem = 1

            }, onBlock = {
                requireActivity().showSnackBar("Chức năng onBlock đang code")
                viewPager2.currentItem = 2
//                vm.refreshBlocked()
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
//        if (isDefaultSmsApp()){
//            Log.d(getTagDebug("QUYEN"), "đã cấp quyền")
//            requireActivity().showSnackBar("Ứng dụng này đã được cấp quyền SMS mặc định. Bạn có thể sử dụng tính năng này.")
//        }else{
//            Log.d(getTagDebug("QUYEN"), "Chưa cấp quyền")
//            requireActivity().showSnackBar("Ứng dụng này không phải là ứng dụng SMS mặc định. Vui lòng cấp quyền để sử dụng tính năng này.")
//            requestDefaultSmsRoleIfNeeded()
//        }

        Log.d("kkkk", "Refresh:loooo")
        vm.refreshInbox()
//        vm.refreshBlocked()
//        vm.refreshSpam()
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
//                vm.refreshBlocked()
//                vm.refreshSpam()
                (requireActivity() as MainActivity).loadUnreadSmsCount()
            }
        }
    }

}
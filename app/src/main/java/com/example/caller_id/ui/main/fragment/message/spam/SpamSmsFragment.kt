package com.example.caller_id.ui.main.fragment.message.spam

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentSpamSmsBinding
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.ui.main.fragment.message.SmsAdapter
import com.example.caller_id.ui.main.fragment.message.chat.ChatBlockActivity
import com.example.caller_id.ui.main.fragment.message.chat.ChatSpamActivity
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.visibleOrGone

class SpamSmsFragment:BaseFragment<FragmentSpamSmsBinding>() {
    private lateinit var spamAdapter: SmsAdapter
    private var listSpam: MutableList<SmsConversation> = mutableListOf()
    private val vm: BlockViewModel by activityViewModels()
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSpamSmsBinding {
        return FragmentSpamSmsBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        spamAdapter = SmsAdapter(mutableListOf()) { displayName, address, color ->
            val intent = Intent(requireActivity(), ChatSpamActivity::class.java).apply {
                putExtra("displayName", displayName)
                putExtra("address", address)
                putExtra("color", color)
            }
            startActivity(intent)
        }
        binding.rvSpam.adapter = spamAdapter
        binding.rvSpam.layoutManager = LinearLayoutManager(requireActivity())
    }

    override fun viewListener() {
    }

    override fun dataObservable() {
        vm.listSearchMessage.observe(viewLifecycleOwner) { query ->
            val filtered = listSpam.filter { app ->
                app.address.normalize().lowercase().contains(query)
            }
            spamAdapter.updateData(filtered.toMutableList())
            binding.rvSpam.visibleOrGone(filtered.isNotEmpty())
            binding.tvNodata.visibleOrGone(filtered.isEmpty())
        }
        vm.spamFiltered.observe(viewLifecycleOwner) { list ->
            listSpam = list.toMutableList()
            spamAdapter.updateData(list.toMutableList())
            binding.rvSpam.visibleOrGone(list.isNotEmpty())
            binding.ctlSpam.visibleOrGone(list.isEmpty())
        }
    }
}
package com.example.caller_id.ui.main.fragment.message.block

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentBlockSmsBinding
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.ui.main.fragment.message.SmsAdapter
import com.example.caller_id.ui.main.fragment.message.chat.ChatAllActivity
import com.example.caller_id.ui.main.fragment.message.chat.ChatBlockActivity
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.visibleOrGone

class BlockSmsFragment : BaseFragment<FragmentBlockSmsBinding>() {
    private lateinit var blockAdapter: SmsAdapter
    private var listBlock: MutableList<SmsConversation> = mutableListOf()
    private val vm: BlockViewModel by activityViewModels()
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBlockSmsBinding {
        return FragmentBlockSmsBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        blockAdapter = SmsAdapter(mutableListOf()) { displayName, address, color ->
            val intent = Intent(requireActivity(), ChatBlockActivity::class.java).apply {
                putExtra("displayName", displayName)
                putExtra("address", address)
                putExtra("color", color)
            }
            startActivity(intent)
        }
        binding.rvBlock.adapter = blockAdapter
        binding.rvBlock.layoutManager = LinearLayoutManager(requireActivity())
    }

    override fun viewListener() {
    }

    override fun dataObservable() {
        vm.listSearchMessage.observe(viewLifecycleOwner) { query ->
            val filtered = listBlock.filter { app ->
                app.address.normalize().lowercase().contains(query)
            }
            Log.d("Doan_1", "Block list size: ${listBlock.size}")
            blockAdapter.updateData(filtered.toMutableList())
            binding.rvBlock.visibleOrGone(filtered.isNotEmpty())
            binding.tvNodata.visibleOrGone(filtered.isEmpty())
        }
        vm.blockFiltered.observe(viewLifecycleOwner) { list ->
            Log.d("Doan_1", "Block list size222: ${list.size}")
            listBlock = list.toMutableList()
            blockAdapter.updateData(list.toMutableList())
            binding.rvBlock.visibleOrGone(list.isNotEmpty())
            binding.ctlBlock.visibleOrGone(list.isEmpty())
        }
    }
}
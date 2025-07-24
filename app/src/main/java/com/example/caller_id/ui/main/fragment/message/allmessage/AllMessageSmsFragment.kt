package com.example.caller_id.ui.main.fragment.message.allmessage

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentAllSmsMessageBinding
import com.example.caller_id.model.SmsConversation
import com.example.caller_id.ui.main.fragment.message.SmsAdapter
import com.example.caller_id.ui.main.fragment.message.chat.ChatAllActivity
import com.example.caller_id.utils.SmsUtils.lookupContactName
import com.example.caller_id.utils.SmsUtils.toNational
import com.example.caller_id.widget.getLogDebug
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.visibleOrGone
import dagger.hilt.android.AndroidEntryPoint
import java.text.Normalizer.normalize

@AndroidEntryPoint
class AllMessageSmsFragment : BaseFragment<FragmentAllSmsMessageBinding>() {
    private lateinit var adapter: SmsAdapter
    private val vm: BlockViewModel by activityViewModels()
    private var listMessage: MutableList<SmsConversation> = mutableListOf()

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAllSmsMessageBinding {
        return FragmentAllSmsMessageBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        rcvSetUp()
    }

    private fun rcvSetUp() = binding.apply {
        adapter = SmsAdapter(mutableListOf()) { displayName, address, color ->
            val intent = Intent(requireActivity(), ChatAllActivity::class.java).apply {
//                val name = lookupContactName(requireActivity(), normalized)
                Log.d("Doan_1", "Contact name for $address: $displayName,")
                putExtra("displayName", displayName)
                putExtra("address", address)
                putExtra("color", color)
            }
            startActivity(intent)

        }
        rvSms.layoutManager = LinearLayoutManager(requireActivity())
        rvSms.adapter = adapter
    }

    override fun viewListener() {

    }

    override fun dataObservable() {
        vm.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibleOrGone(isLoading)
            binding.rvSms.visibleOrGone(!isLoading)
        }

        vm.listSearchMessage.observe(viewLifecycleOwner) { query ->
            val filtered = listMessage.filter { app ->
                app.address.normalize().lowercase().contains(query)
            }
            Log.d("Doan_1", "Filtered list size: ${filtered.size}")
            adapter.updateData(filtered.toMutableList())
            binding.rvSms.visibleOrGone(filtered.isNotEmpty())
            binding.tvNodata.visibleOrGone(filtered.isEmpty())
        }
        vm.inboxFiltered.observe(viewLifecycleOwner) { smsList ->
            getLogDebug("ALL_SMS","list:$smsList")
            listMessage = smsList.toMutableList()
            adapter.updateData(smsList.toMutableList())
        }
    }
}
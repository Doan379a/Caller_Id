package com.example.caller_id.ui.main.fragment.contacts.pager

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentContactBinding
import com.example.caller_id.model.ContactModel
import com.example.caller_id.ui.main.fragment.message.chat.ChatAllActivity
import com.example.caller_id.ui.numberinfo.NumberInfoActivity
import com.example.caller_id.utils.SmsUtils.getCheckAddress
import com.example.caller_id.widget.getLogDebug
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.showSnackBar
import com.example.caller_id.widget.visibleOrGone
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactFragment : BaseFragment<FragmentContactBinding>() {
    private val vm: BlockViewModel by activityViewModels()
    private val adapter: ContactAdapter by lazy { ContactAdapter(mutableListOf()) }
    private var listContact: MutableList<ContactModel> = mutableListOf()
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentContactBinding {
        return FragmentContactBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.rcv.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcv.adapter = adapter

    }

    override fun viewListener() {
        adapter.onClickItem = { data ->
            val intent = Intent(requireActivity(), NumberInfoActivity::class.java).apply {
                putExtra("number", data.number)
            }
            startActivity(intent)
        }
        adapter.onClickCall = { phone ->
            requireActivity().showSnackBar("ssss$phone")
            getLogDebug("CONTACT", "kkk$phone")

        }
        adapter.onClickSms = { phone ->
            requireActivity().showSnackBar("ssss$phone")
            getLogDebug("CONTACT", "kkk$phone")
            val normalized = getCheckAddress(phone)
            val smsIntent = Intent(requireActivity(), ChatAllActivity::class.java).apply {
                putExtra("address", normalized)
            }
            startActivity(smsIntent)
        }
    }

    override fun dataObservable() {
        vm.listSearchContact.observe(viewLifecycleOwner) { query ->
            val listQuery = listContact.filter { c ->
                c.name.normalize().lowercase().contains(query) || c.number.normalize()
                    .removePrefix("+84").contains(query)
            }
            adapter.updateList(listQuery.toMutableList())
            binding.rcv.visibleOrGone(listQuery.isNotEmpty())
            binding.tvNodata.visibleOrGone(listQuery.isEmpty())
        }
        vm.contacts.observe(viewLifecycleOwner) { list ->
            listContact = list.toMutableList()
            adapter.updateList(list.toMutableList())
        }
    }
}
package com.example.caller_id.ui.main.fragment.contacts.pager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentContactFavoritesBinding
import com.example.caller_id.model.ContactModel
import com.example.caller_id.widget.getLogDebug
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.showSnackBar

class ContactFavoritesFragment : BaseFragment<FragmentContactFavoritesBinding>() {
    private val vm: BlockViewModel by activityViewModels()
    private var listContact: MutableList<ContactModel> = mutableListOf()

    private val adapter: ContactAdapter by lazy { ContactAdapter(mutableListOf(), true) }
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentContactFavoritesBinding {
        return FragmentContactFavoritesBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.rcvContactFavorites.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcvContactFavorites.adapter = adapter
    }

    override fun viewListener() {
        adapter.onClickNext = { phone ->
            requireActivity().showSnackBar("ssss$phone")
            getLogDebug("CONTACT", "kkk$phone")
        }
    }

    override fun dataObservable() {
        vm.listSearchContact.observe(viewLifecycleOwner) { query ->
            val listQuery = listContact.filter { c ->
                c.name.normalize().lowercase().contains(query) || c.number.normalize()
                    .removePrefix("+84").contains(query)
            }
            adapter.updateList(listQuery.toMutableList())

        }
        vm.contacts.observe(viewLifecycleOwner) { list ->
            listContact = list.toMutableList()
            adapter.updateList(list.toMutableList())
        }
    }
}
package com.example.caller_id.ui.main.fragment.contacts.pager

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentContactFavoritesBinding
import com.example.caller_id.model.ContactModel
import com.example.caller_id.ui.numberinfo.NumberInfoActivity
import com.example.caller_id.widget.getLogDebug
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactFavoritesFragment : BaseFragment<FragmentContactFavoritesBinding>() {
    private val vm: BlockViewModel by activityViewModels()
    private lateinit var adapter: FavoriteAdapter
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentContactFavoritesBinding {
        return FragmentContactFavoritesBinding.inflate(layoutInflater)
    }

    override fun initView() {
        adapter = FavoriteAdapter(requireActivity()) { data ->
            val intent = Intent(requireActivity(), NumberInfoActivity::class.java).apply {
                putExtra("number", data.number)
                putExtra("name", data.name)
            }
            startActivity(intent)
        }
        binding.rcvContactFavorites.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcvContactFavorites.adapter = adapter
        vm.callBlockedList.observe(viewLifecycleOwner) { list ->
            val favoriteList = list.filter { it.type == "favorites" }
            adapter.updateList(favoriteList)
        }
    }

    override fun viewListener() {

    }

    override fun dataObservable() {

    }
}
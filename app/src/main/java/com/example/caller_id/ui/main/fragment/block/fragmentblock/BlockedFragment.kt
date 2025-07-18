package com.example.caller_id.ui.main.fragment.block.fragmentblock

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.R
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentBlockBinding
import com.example.caller_id.databinding.FragmentBlockedBinding
import com.example.caller_id.databinding.FragmentFavoritesBinding
import com.example.caller_id.ui.main.fragment.block.adapterblock.BlockedAdapter
import com.example.caller_id.widget.invisible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedFragment : BaseFragment<FragmentBlockedBinding>() {
    private lateinit var adapter: BlockedAdapter
    private val vm: BlockViewModel by activityViewModels()

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBlockedBinding {
        return FragmentBlockedBinding.inflate(layoutInflater)
    }

    override fun initView() {

        adapter = BlockedAdapter(requireActivity()){   data->
            AlertDialog.Builder(requireContext())
                .setMessage("${getString(R.string.are_you_sure_you_want_to_unblock)} ${data.number}?")
                .setPositiveButton(getString(R.string.unblock)) { _, _ ->
                    vm.deleteCallById(data.id)
                    Toast.makeText(requireContext(), "${getString(R.string.unblocked)} ${data.number}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
        binding.rcvBlock.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcvBlock.adapter = adapter

        vm.callBlockedList.observe(viewLifecycleOwner) { list ->
            Log.d("callBlockedList", list.toString())
            adapter.updateList(list)
        }

    }

    override fun viewListener() {
    }

    override fun dataObservable() {
        vm.listSearchBlock.observe(viewLifecycleOwner) { query ->
            adapter.filtered(query)
            Log.d("listSearchBlock", "kjkjk")
        }
    }

}
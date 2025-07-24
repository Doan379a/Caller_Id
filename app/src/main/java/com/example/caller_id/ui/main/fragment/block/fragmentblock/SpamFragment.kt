package com.example.caller_id.ui.main.fragment.block.fragmentblock

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.R
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentBlockedBinding
import com.example.caller_id.databinding.FragmentFavoritesBinding
import com.example.caller_id.ui.main.fragment.block.adapterblock.SpamAdapter
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpamFragment : BaseFragment<FragmentBlockedBinding>() {
    private lateinit var adapter: SpamAdapter
    private val vm: BlockViewModel by viewModels()
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBlockedBinding {
        return FragmentBlockedBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.rlSpam.visible()
        binding.rlBlock.gone()
        adapter = SpamAdapter(requireActivity()){data->
            AlertDialog.Builder(requireContext())
                .setMessage("${getString(R.string.are_you_sure_you_want_to_unblock_spam)} ${data.number}?")
                .setPositiveButton(getString(R.string.unblock)) { _, _ ->
                    vm.deleteCallById(data.id)
                    Toast.makeText(requireContext(), "${getString(R.string.unblocked_spam)} ${data.number}", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
        binding.rcvBlock.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcvBlock.adapter = adapter
        vm.callSpamList.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)
        }
    }

    override fun viewListener() {
    }

    override fun dataObservable() {
    }

}
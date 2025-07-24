package com.example.caller_id.ui.main.fragment.calls.fragmentcall

import android.app.AlertDialog
import android.content.Intent
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
import com.example.caller_id.databinding.FragmentFavoritesBinding
import com.example.caller_id.ui.main.fragment.calls.FavoritesAdapter
import com.example.caller_id.ui.main.fragment.message.chat.ChatAllActivity
import com.example.caller_id.ui.numberinfo.NumberInfoActivity
import com.example.caller_id.utils.SmsUtils.normalizePhone
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : BaseFragment<FragmentFavoritesBinding>() {
    private lateinit var adapter: FavoritesAdapter
    private val viewModel: BlockViewModel by viewModels()
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFavoritesBinding {
        return FragmentFavoritesBinding.inflate(layoutInflater)
    }

    override fun initView() {
        adapter = FavoritesAdapter(requireContext()) { data ->
            val intent = Intent(requireActivity(), NumberInfoActivity::class.java).apply {
                putExtra("number", data.number)
                putExtra("name", data.name)
            }
            startActivity(intent)
        }
        binding.rcv.layoutManager = LinearLayoutManager(requireContext())
        binding.rcv.adapter = adapter
        viewModel.callBlockedList.observe(viewLifecycleOwner) { list ->
            val favoriteList = list.filter { it.type == "favorites" }
            adapter.updateList(favoriteList)
        }
        adapter.onClickSms = { sms ->
            val normalized = normalizePhone(sms) ?: sms
            val smsIntent = Intent(requireActivity(), ChatAllActivity::class.java).apply {
                putExtra("address", normalized)
            }
            startActivity(smsIntent)

        }

    }

    override fun viewListener() {
    }

    override fun dataObservable() {
    }

}
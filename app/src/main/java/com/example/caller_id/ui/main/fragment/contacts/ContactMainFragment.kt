package com.example.caller_id.ui.main.fragment.contacts

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.caller_id.R
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentContactMainBinding
import com.example.caller_id.dialog.FilterCallHomePopup
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.tap
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactMainFragment : BaseFragment<FragmentContactMainBinding>() {
    private val vm: BlockViewModel by activityViewModels()

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentContactMainBinding {
        return FragmentContactMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.viewPager2.isUserInputEnabled = false
        binding.viewPager2.adapter = ContactMainAdapterViewPager(requireActivity())
        binding.viewPager2.offscreenPageLimit = 2
        search()
    }

    override fun viewListener() {
        binding.imgPopup.tap {
            val popup= FilterCallHomePopup(requireActivity())
            popup.showAtView(binding.imgPopup)
        }
        binding.tvContact.setOnClickListener {
            binding.viewPager2.currentItem = 0
            setUpColorTab(binding.viewPager2.currentItem)
        }
        binding.tvFavorites.setOnClickListener {
            binding.viewPager2.currentItem = 1
            setUpColorTab(binding.viewPager2.currentItem)
        }
    }

    private fun search() = binding.apply {
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().normalize().lowercase().trim()
                vm.searchContact(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
    override fun dataObservable() {

    }

    private fun setUpColorTab(selectedTab: Int) = binding.apply {
        binding.tvContact.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_transparent
            )
        )
        binding.tvFavorites.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_transparent
            )
        )
        when (selectedTab) {
            0 -> {
                binding.tvContact.setBackgroundResource(R.drawable.bg_boder_white)
            }

            1 -> {
                binding.tvFavorites.setBackgroundResource(R.drawable.bg_boder_white)
            }

        }
    }
}
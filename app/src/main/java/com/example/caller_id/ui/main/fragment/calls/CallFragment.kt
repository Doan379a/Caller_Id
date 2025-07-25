package com.example.caller_id.ui.main.fragment.calls

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.caller_id.R
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentCallBinding
import com.example.caller_id.dialog.FilterCallHomePopup
import com.example.caller_id.ui.main.MainAdapter
import com.example.caller_id.ui.main.user.UserInfoActivity
import com.example.caller_id.widget.tap

class CallFragment:BaseFragment<FragmentCallBinding>() {

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCallBinding {
        return FragmentCallBinding.inflate(layoutInflater)
    }
    private var myPageChangeCallback: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("KKK", "onPageSelected: $position")
                setUpColorTab(position)
            }
        }

    override fun initView() {
        binding.viewPager2.isUserInputEnabled = false

        binding.viewPager2.adapter = CallAdapter(requireActivity())
        binding.viewPager2.registerOnPageChangeCallback(myPageChangeCallback)
    }

    override fun viewListener() {
        binding.imgUser.tap {
            startActivity(Intent(requireActivity(),UserInfoActivity::class.java))
        }
        binding.imgPopup.tap {
            val popup= FilterCallHomePopup(requireActivity())
            popup.showAtView(binding.imgPopup)
        }
        binding.ivRecent.setOnClickListener {
            setUpColorTab(0)
            binding.viewPager2.currentItem = 0
        }
        binding.ivFavorites.setOnClickListener {
            setUpColorTab(1)
            binding.viewPager2.currentItem = 1
        }
        binding.ivDisturd.setOnClickListener {
            setUpColorTab(2)
            binding.viewPager2.currentItem = 2
        }
        // Set up view listeners here
    }

    private fun setUpColorTab(selectedTab: Int) = binding.apply {
        binding.ivRecent.setBackgroundColor(ContextCompat.getColor(requireActivity(),R.color.color_transparent))
        binding.ivFavorites.setBackgroundColor(ContextCompat.getColor(requireActivity(),R.color.color_transparent))
        binding.ivDisturd.setBackgroundColor(ContextCompat.getColor(requireActivity(),R.color.color_transparent))
        binding.ivRecent.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_707070))
        binding.ivFavorites.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_707070))
        binding.ivDisturd.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_707070))

        when (selectedTab) {
            0 -> {
                binding.ivRecent.setBackgroundResource(R.drawable.bg_boder_white)
                binding.ivRecent.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_3368EE))
            }

            1 -> {
                binding.ivFavorites.setBackgroundResource(R.drawable.bg_boder_white)
                binding.ivFavorites.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_3368EE))
            }

            2 -> {
                binding.ivDisturd.setBackgroundResource(R.drawable.bg_boder_white)
                binding.ivDisturd.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_3368EE))
            }
        }
    }
    override fun dataObservable() {
    }
}
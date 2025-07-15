package com.example.caller_id.ui.main.fragment.block

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.caller_id.R
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentBlockBinding
import com.example.caller_id.ui.main.fragment.calls.CallAdapter

class BlockFragment:BaseFragment<FragmentBlockBinding>() {

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBlockBinding {
        return FragmentBlockBinding.inflate(layoutInflater)
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

        binding.viewPager2.adapter = BlockAdapter(requireActivity())
        binding.viewPager2.registerOnPageChangeCallback(myPageChangeCallback)
    }

    override fun viewListener() {
        binding.ivSetting.setOnClickListener {
            setUpColorTab(0)
            binding.viewPager2.currentItem = 0
        }
        binding.ivSpam.setOnClickListener {
            setUpColorTab(1)
            binding.viewPager2.currentItem = 1
        }
        binding.ivBlocked.setOnClickListener {
            setUpColorTab(2)
            binding.viewPager2.currentItem = 2
        }
    }

    private fun setUpColorTab(selectedTab: Int) = binding.apply {
        binding.ivSetting.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_transparent))
        binding.ivSpam.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_transparent))
        binding.ivBlocked.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_transparent))
        when (selectedTab) {
            0 -> {
                binding.ivSetting.setBackgroundResource(R.drawable.bg_boder_white)
            }

            1 -> {
                binding.ivSpam.setBackgroundResource(R.drawable.bg_boder_white)
            }

            2 -> {
                binding.ivBlocked.setBackgroundResource(R.drawable.bg_boder_white)
            }
        }
    }
    override fun dataObservable() {
    }
}
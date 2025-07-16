package com.example.caller_id.ui.main.fragment.calls

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.caller_id.R
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentCallBinding
import com.example.caller_id.ui.main.MainAdapter

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
        when (selectedTab) {
            0 -> {
                binding.ivRecent.setBackgroundResource(R.drawable.bg_boder_white)
            }

            1 -> {
                binding.ivFavorites.setBackgroundResource(R.drawable.bg_boder_white)
            }

            2 -> {
                binding.ivDisturd.setBackgroundResource(R.drawable.bg_boder_white)
            }
        }
    }
    override fun dataObservable() {
    }
}
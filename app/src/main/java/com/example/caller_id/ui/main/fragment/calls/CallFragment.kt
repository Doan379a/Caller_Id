package com.example.caller_id.ui.main.fragment.calls

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentCallBinding

class CallFragment:BaseFragment<FragmentCallBinding>() {

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCallBinding {
        return FragmentCallBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // Initialize views here
    }

    override fun viewListener() {
        // Set up view listeners here
    }

    override fun dataObservable() {
        // Observe data changes here
    }
}
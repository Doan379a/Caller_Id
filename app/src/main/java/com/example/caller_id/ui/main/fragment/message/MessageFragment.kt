package com.example.caller_id.ui.main.fragment.message

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentMessageBinding

class MessageFragment: BaseFragment<FragmentMessageBinding>() {
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMessageBinding {
        return FragmentMessageBinding.inflate(layoutInflater)
    }

    override fun initView() {
    }

    override fun viewListener() {
    }

    override fun dataObservable() {
    }
}
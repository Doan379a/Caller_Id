package com.example.caller_id.ui.main.fragment.message.spam

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentSpamSmsBinding

class SpamSmsFragment:BaseFragment<FragmentSpamSmsBinding>() {
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSpamSmsBinding {
        return FragmentSpamSmsBinding.inflate(inflater, container, false)
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
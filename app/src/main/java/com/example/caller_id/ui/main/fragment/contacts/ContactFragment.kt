package com.example.caller_id.ui.main.fragment.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentContactBinding

class ContactFragment:BaseFragment<FragmentContactBinding>() {

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentContactBinding {
        return FragmentContactBinding.inflate(layoutInflater)
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
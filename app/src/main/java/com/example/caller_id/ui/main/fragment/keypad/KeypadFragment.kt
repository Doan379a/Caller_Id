package com.example.caller_id.ui.main.fragment.keypad

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentKeypadBinding

class KeypadFragment: BaseFragment<FragmentKeypadBinding>() {

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentKeypadBinding {
        return FragmentKeypadBinding.inflate(layoutInflater)
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
package com.example.caller_id.ui.main.fragment.block

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentBlockBinding

class BlockFragment:BaseFragment<FragmentBlockBinding>() {



    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBlockBinding {
        return FragmentBlockBinding.inflate(layoutInflater)
    }

    override fun initView() {

    }

    override fun viewListener() {
    }

    override fun dataObservable() {
    }
}
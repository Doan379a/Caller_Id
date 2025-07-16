package com.example.caller_id.ui.main.fragment.block.fragmentblock

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.caller_id.R
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentBlockBinding
import com.example.caller_id.databinding.FragmentFavoritesBinding

class BlockedFragment : BaseFragment<FragmentFavoritesBinding>() {
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFavoritesBinding {
        return FragmentFavoritesBinding.inflate(layoutInflater)
    }

    override fun initView() {
    }

    override fun viewListener() {
    }

    override fun dataObservable() {
    }

}
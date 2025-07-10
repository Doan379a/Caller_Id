package com.example.caller_id.ui.main

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.caller_id.ui.main.fragment.block.BlockFragment
import com.example.caller_id.ui.main.fragment.calls.CallFragment
import com.example.caller_id.ui.main.fragment.contacts.ContactFragment
import com.example.caller_id.ui.main.fragment.message.MessageFragment

class MainAdapter (fragmentManager: FragmentActivity) : FragmentStateAdapter(fragmentManager)  {
    override fun getItemCount(): Int =4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CallFragment()
            1 -> MessageFragment()
            2 ->  ContactFragment()
            3 -> BlockFragment()
            else -> CallFragment()
        }
    }
}

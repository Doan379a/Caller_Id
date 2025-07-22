package com.example.caller_id.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.caller_id.ui.main.fragment.block.BlockFragment
import com.example.caller_id.ui.main.fragment.calls.CallFragment
import com.example.caller_id.ui.main.fragment.contacts.ContactMainFragment
import com.example.caller_id.ui.main.fragment.contacts.pager.ContactFragment
import com.example.caller_id.ui.main.fragment.message.MessageFragment

class MainAdapter (fragmentManager: FragmentActivity) : FragmentStateAdapter(fragmentManager)  {
    override fun getItemCount(): Int =4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CallFragment()
            1 -> MessageFragment()
            2 ->  ContactMainFragment()
            3 -> BlockFragment()
            else -> CallFragment()
        }
    }
}

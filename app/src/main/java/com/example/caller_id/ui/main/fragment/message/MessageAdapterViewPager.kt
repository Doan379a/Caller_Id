package com.example.caller_id.ui.main.fragment.message

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.caller_id.ui.main.fragment.message.allmessage.AllMessageSmsFragment
import com.example.caller_id.ui.main.fragment.message.block.BlockSmsFragment
import com.example.caller_id.ui.main.fragment.message.spam.SpamSmsFragment

class MessageAdapterViewPager  (fragmentManager: FragmentActivity) : FragmentStateAdapter(fragmentManager)  {
    override fun getItemCount(): Int =3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllMessageSmsFragment()
            1 -> SpamSmsFragment()
            2 ->  BlockSmsFragment()
            else -> AllMessageSmsFragment()
        }
    }
}

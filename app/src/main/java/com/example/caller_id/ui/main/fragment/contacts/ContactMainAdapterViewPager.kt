package com.example.caller_id.ui.main.fragment.contacts

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.caller_id.ui.main.fragment.contacts.pager.ContactFavoritesFragment
import com.example.caller_id.ui.main.fragment.contacts.pager.ContactFragment
import com.example.caller_id.ui.main.fragment.message.allmessage.AllMessageSmsFragment
import com.example.caller_id.ui.main.fragment.message.block.BlockSmsFragment
import com.example.caller_id.ui.main.fragment.message.spam.SpamSmsFragment

class ContactMainAdapterViewPager(fragmentManager: FragmentActivity) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ContactFragment()
            1 -> ContactFavoritesFragment()
            else -> ContactFragment()
        }
    }
}

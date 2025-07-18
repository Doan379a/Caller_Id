package com.example.caller_id.ui.main.fragment.block.adapterblock

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.caller_id.ui.main.fragment.block.fragmentblock.BlockedFragment
import com.example.caller_id.ui.main.fragment.block.fragmentblock.SettingFragment
import com.example.caller_id.ui.main.fragment.block.fragmentblock.SpamFragment

class BlockAdapter (fragmentManager: FragmentActivity) : FragmentStateAdapter(fragmentManager)  {
    override fun getItemCount(): Int =3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SettingFragment()
            1 -> SpamFragment()
            2 ->  BlockedFragment()
            else -> SettingFragment()
        }
    }
}

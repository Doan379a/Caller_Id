package com.example.caller_id.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.caller_id.R
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityMainBinding
import com.example.caller_id.library.magicindicator.buildins.commonnavigator.CommonNavigator

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val commonNavigator by lazy { CommonNavigator(this) }
    private var unreadMessageCount = 0
    override fun setViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    private var myPageChangeCallback: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("KKK", "onPageSelected: $position")
                setUpColorTab(position)
            }
        }


    override fun initView() {
        checkSmsPermission()
        binding.viewPager2.adapter = MainAdapter(this)
        binding.viewPager2.registerOnPageChangeCallback(myPageChangeCallback)
    }

    override fun viewListener() {
        binding.imgCall.setOnClickListener {
            setUpColorTab(0)
            binding.viewPager2.currentItem = 0
        }
        binding.imgMessage.setOnClickListener {
            setUpColorTab(1)
            binding.viewPager2.currentItem = 1
        }
        binding.imgContacts.setOnClickListener {
            setUpColorTab(2)
            binding.viewPager2.currentItem = 2
        }
        binding.imgBlock.setOnClickListener {
            setUpColorTab(3)
            binding.viewPager2.currentItem = 3
        }
    }

    override fun dataObservable() {
    }

    private fun setUpColorTab(selectedTab: Int) = binding.apply {
        binding.imgCall.setImageResource(R.drawable.ic_call)
        binding.imgMessage.setImageResource(R.drawable.ic_message)
        binding.imgContacts.setImageResource(R.drawable.ic_contacts)
        binding.imgBlock.setImageResource(R.drawable.ic_block)
        when (selectedTab) {
            0 -> {
                binding.imgCall.setImageResource(R.drawable.ic_call_select)
            }

            1 -> {
                binding.imgMessage.setImageResource(R.drawable.ic_message_select)
            }

            2 -> {
                binding.imgContacts.setImageResource(R.drawable.ic_contacts_select)
            }

            3 -> {
                binding.imgBlock.setImageResource(R.drawable.ic_block_select)
            }
        }
    }

    private fun checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS), 1001)
        } else {
            loadUnreadSmsCount()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadUnreadSmsCount()
        }
    }

    private fun loadUnreadSmsCount() {
        var count = 0
        try {
            val uri = Uri.parse("content://sms/inbox")
            val projection = arrayOf("_id", "read")
            val selection = "read = 0"
            val cursor = contentResolver.query(uri, projection, selection, null, null)

            if (cursor != null) {
                count = cursor.count
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        unreadMessageCount = count
        commonNavigator.adapter?.notifyDataSetChanged() // Cập nhật tab
    }

}
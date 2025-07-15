package com.example.caller_id.ui.main

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.caller_id.R
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityMainBinding
import com.example.caller_id.library.magicindicator.buildins.commonnavigator.CommonNavigator
import com.example.caller_id.widget.showSnackBar


class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val commonNavigator by lazy { CommonNavigator(this) }
    private var unreadMessageCount = 0
    private var REQUEST_CODE_SET_DEFAULT_DIALER = 123
    private val REQUEST_CODE_CALL_SCREENING = 1002

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
        checkPermissionPhone()
        binding.viewPager2.isUserInputEnabled = false

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
    private fun checkPermissionPhone(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                    startActivityForResult(intent, REQUEST_CODE_CALL_SCREENING)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
            val isDefaultDialer = packageName == telecomManager.defaultDialerPackage
            if (!isDefaultDialer) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = getSystemService(RoleManager::class.java)
                    if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                        if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                            startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
                        }
                    }
                } else {
                    val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                    intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
                    startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
                }

            }
        } else {
            showSnackBar("Phiên bản Android không hỗ trợ thay đổi trình quay số mặc định")
        }
    }

}
package com.example.caller_id.ui.main.user

import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityUserInfoBinding

class UserInfoActivity:BaseActivity<ActivityUserInfoBinding>() {
    override fun setViewBinding(): ActivityUserInfoBinding {
        return ActivityUserInfoBinding.inflate(layoutInflater)
    }

    override fun initView() {
    }

    override fun viewListener() {
    }

    override fun dataObservable() {
    }
}
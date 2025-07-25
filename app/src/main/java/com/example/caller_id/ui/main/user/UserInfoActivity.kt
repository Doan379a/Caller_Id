package com.example.caller_id.ui.main.user

import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityUserInfoBinding
import com.example.caller_id.widget.showSnackBar
import com.example.caller_id.widget.tap

class UserInfoActivity:BaseActivity<ActivityUserInfoBinding>() {
    override fun setViewBinding(): ActivityUserInfoBinding {
        return ActivityUserInfoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.tvSave.tap {
            showSnackBar("Save")
        }
        binding.imgBack.tap {
            finish()
        }
    }

    override fun viewListener() {
    }

    override fun dataObservable() {
    }
}
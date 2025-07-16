package com.example.caller_id.dialog

import android.app.Activity
import android.view.LayoutInflater
import com.example.caller_id.base.BaseDialog
import com.example.caller_id.databinding.DialogPermissionBinding
import com.example.caller_id.widget.tap

class BlockDialog(
    activity1: Activity,
    private var action: () -> Unit
) : BaseDialog<DialogPermissionBinding>(activity1, true) {
    override fun getContentView(): DialogPermissionBinding {
        return DialogPermissionBinding.inflate(LayoutInflater.from(activity))
    }

    override fun initView() {

    }

    override fun bindView() {
        binding.apply {
            txtGo.tap {
                action.invoke()
                dismiss()
            }
        }
    }


}
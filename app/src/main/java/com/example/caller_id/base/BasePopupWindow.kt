package com.example.caller_id.base

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.viewbinding.ViewBinding
import com.example.caller_id.utils.SystemUtil

abstract class BasePopupWindow<VB : ViewBinding>(
    context: Context
) : PopupWindow(context) {

    protected lateinit var binding: VB

    abstract fun getViewBinding(inflater: LayoutInflater): VB
    abstract fun onBind(binding: VB)

    init {
        val inflater = LayoutInflater.from(context)
        binding = getViewBinding(inflater)
        contentView = binding.root
        SystemUtil.setLocale(context)
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        elevation = 10f
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Cho phép bo góc

        onBind(binding)
    }

    fun showAtView(anchor: View, xOff: Int = 0, yOff: Int = 0) {
        showAsDropDown(anchor, xOff, yOff)
    }
}

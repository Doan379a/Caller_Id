package com.example.caller_id.dialog

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import com.example.caller_id.R
import com.example.caller_id.base.BasePopupWindow
import com.example.caller_id.databinding.PopupMessageBinding

class MessagePopup(
    context: Context,
    private var intSelector: Int,
    private val onSpam: () -> Unit,
    private val onBlock: () -> Unit,
    private val onAllMessages: () -> Unit
) : BasePopupWindow<PopupMessageBinding>(context) {

    override fun getViewBinding(inflater: LayoutInflater): PopupMessageBinding {
        return PopupMessageBinding.inflate(inflater)
    }

    override fun onBind(binding: PopupMessageBinding) {
        Log.d("DOAN_3", "popup=$intSelector")

       // setSelector(intSelector)
        binding.itemAllMessages.setOnClickListener {
            dismiss()
            onAllMessages()
            setSelector(0)
        }
        binding.itemBlock.setOnClickListener {
            dismiss()
            onBlock()
            setSelector(2)
        }
        binding.itemSpam.setOnClickListener {
            dismiss()
            onSpam()
            setSelector(1)
        }
    }

    fun setType(type: Int) {
        intSelector = type
        Log.d("DOAN_3", "setType=$intSelector")
        setSelector(intSelector)
    }
    fun setSelector(selector: Int) {
        binding.itemAllMessages.setBackgroundColor(Color.TRANSPARENT)
        binding.itemBlock.setBackgroundColor(Color.TRANSPARENT)
        binding.itemSpam.setBackgroundColor(Color.TRANSPARENT)
        when (selector) {
            0 -> binding.itemAllMessages.setBackgroundResource(R.drawable.bg_select_item_popup)

            1 -> binding.itemSpam.setBackgroundResource(R.drawable.bg_select_item_popup)

            2 ->binding.itemBlock.setBackgroundResource(R.drawable.bg_select_item_popup)

        }
    }
}
package com.example.caller_id.dialog

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.example.caller_id.R
import com.example.caller_id.base.BasePopupWindow
import com.example.caller_id.databinding.PopupChatMenuBinding

class ChatMenuPopup ( context: Context,
                      private val onCreateContact: () -> Unit,
                      private val onSpam: () -> Unit,
                        private val onBlock: () -> Unit,
                        private val onDelete: () -> Unit
) : BasePopupWindow<PopupChatMenuBinding>(context) {

    override fun getViewBinding(inflater: LayoutInflater): PopupChatMenuBinding {
        return PopupChatMenuBinding.inflate(inflater)
    }

    override fun onBind(binding: PopupChatMenuBinding) {
        binding.itemCreate.setOnClickListener {
            dismiss()
            onCreateContact()
        }

        binding.itemSpam.setOnClickListener {
            dismiss()
            onSpam()
        }
        binding.itemBlock.setOnClickListener {
            dismiss()
            onBlock()
        }
        binding.itemDelete.setOnClickListener {
            dismiss()
            onDelete()
        }
    }
}
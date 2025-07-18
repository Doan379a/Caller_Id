package com.example.caller_id.dialog

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.example.caller_id.R
import com.example.caller_id.base.BasePopupWindow
import com.example.caller_id.databinding.PopupChatMenuBinding
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.visible
import com.example.caller_id.widget.visibleOrGone

class ChatMenuPopup(
    context: Context,
    private val onSpam: () -> Unit,
    private val onBlock: () -> Unit,
    private val onDelete: () -> Unit,

    ) : BasePopupWindow<PopupChatMenuBinding>(context) {
    var onRemoveSpam: (() -> Unit)? = null
    var onUnBlock: (() -> Unit)? = null
    override fun getViewBinding(inflater: LayoutInflater): PopupChatMenuBinding {
        return PopupChatMenuBinding.inflate(inflater)
    }

    fun setUnBlock(unblock: Boolean) {
        binding.itemUnBlock.visibleOrGone(unblock)
        binding.itemBlock.visibleOrGone(!unblock)
        Log.d("DOAN_3", "setType=$unblock")
    }

    fun setRemoveSpam(unblock: Boolean) {
        binding.itemRemoveSpam.visibleOrGone(unblock)
        binding.itemSpam.visibleOrGone(!unblock)
        Log.d("DOAN_3", "setType=$unblock")
    }

    override fun onBind(binding: PopupChatMenuBinding) {

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
        binding.itemUnBlock.setOnClickListener {
            dismiss()
            onUnBlock?.invoke()
        }
        binding.itemRemoveSpam.setOnClickListener {
            dismiss()
            onRemoveSpam?.invoke()
        }
    }
}
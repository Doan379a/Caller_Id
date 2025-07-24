package com.example.caller_id.dialog

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import com.example.caller_id.base.BasePopupWindow
import com.example.caller_id.databinding.PopupChatMenuBinding
import com.example.caller_id.databinding.PopupFilterCallHomeBinding
import com.example.caller_id.ui.filtercall.FilterCallHomeActivity
import com.example.caller_id.utils.SmsUtils.getCallLogs
import com.example.caller_id.widget.tap

class FilterCallHomePopup(
    val context: Context,
) : BasePopupWindow<PopupFilterCallHomeBinding>(context) {
    //    var onClickCallAll: (() -> Unit)? = null
//    var onClickIncoming: (() -> Unit)? = null
//    var onClickOutgoing: (() -> Unit)? = null
//    var onClickMissed: (() -> Unit)? = null
//    var onClickClearAll: (() -> Unit)? = null
    enum class TabFilter() {
        All, INCOMING, OUTGOING, MISSED
    }

    override fun getViewBinding(inflater: LayoutInflater): PopupFilterCallHomeBinding {
        return PopupFilterCallHomeBinding.inflate(inflater)
    }

    override fun onBind(binding: PopupFilterCallHomeBinding) {
        binding.itemAll.tap {
//            onClickCallAll?.invoke()
            showActivity(TabFilter.All)
        }
        binding.itemIncomingCalls.tap {
            showActivity(TabFilter.INCOMING)
//            onClickIncoming?.invoke()
        }
        binding.itemCallOutgoing.tap {
            showActivity(TabFilter.OUTGOING)
//            onClickOutgoing?.invoke()
        }
        binding.itemCallMissed.tap {
            showActivity(TabFilter.MISSED)
//            onClickMissed?.invoke()
        }
        binding.itemClearAllCalls.tap {
//            onClickClearAll?.invoke()
        }
    }

    fun showActivity(tab: TabFilter) {
        val intent = Intent(context, FilterCallHomeActivity::class.java).apply {
            putExtra("name", tab.name)
        }
        context.startActivity(intent)
    }
}
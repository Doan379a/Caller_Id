package com.example.caller_id.ui.filtercall

import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.R
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityFilterCallHomeBinding
import com.example.caller_id.dialog.FilterCallHomePopup
import com.example.caller_id.model.CallLogItem
import com.example.caller_id.ui.numberinfo.NumberInfoActivity
import com.example.caller_id.utils.SmsUtils.getCallLogs
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.visibleOrGone

class FilterCallHomeActivity : BaseActivity<ActivityFilterCallHomeBinding>() {

    private val adapter: FilterCallHomeAdapter by lazy { FilterCallHomeAdapter(mutableListOf()) }
    private var listCall: MutableList<CallLogItem> = mutableListOf()

    override fun setViewBinding(): ActivityFilterCallHomeBinding {
        return ActivityFilterCallHomeBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val tabLayout = intent.getStringExtra("name") ?: FilterCallHomePopup.TabFilter.All.name
        val tabFilter = FilterCallHomePopup.TabFilter.valueOf(tabLayout)
        val list = getCallLogs(context = this)
        when (tabFilter) {
            FilterCallHomePopup.TabFilter.All -> {
                listCall = list.toMutableList()
                binding.tvTitle.text = getString(R.string.all)
            }

            FilterCallHomePopup.TabFilter.INCOMING -> {
                listCall = list.filter { it.type == "Incoming" }.toMutableList()
                binding.tvTitle.text = getString(R.string.incoming_calls)
            }

            FilterCallHomePopup.TabFilter.OUTGOING -> {
                listCall = list.filter { it.type == "Outgoing" }.toMutableList()
                binding.tvTitle.text = getString(R.string.outgoing_calls)

            }

            FilterCallHomePopup.TabFilter.MISSED -> {
                listCall = list.filter { it.type == "Missed" }.toMutableList()
                binding.tvTitle.text = getString(R.string.missed_calls)
            }

        }
        binding.tvNodata.visibleOrGone(listCall.isEmpty())
        binding.rcvFilterCall.visibleOrGone(listCall.isNotEmpty())
        binding.rcvFilterCall.layoutManager = LinearLayoutManager(this)
        binding.rcvFilterCall.adapter = adapter
        adapter.updateList(listCall.toMutableList())
    }

    override fun viewListener() {
        adapter.onClickItem = { data ->
            val intent = Intent(this, NumberInfoActivity::class.java).apply {
                putExtra("number", data.number)
            }
            startActivity(intent)
        }
        binding.imgClose.tap {
            finish()
        }
    }

    override fun dataObservable() {
    }
}
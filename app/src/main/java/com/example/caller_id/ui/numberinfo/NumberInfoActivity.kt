package com.example.caller_id.ui.numberinfo

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.provider.CallLog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityNumberInfoBinding
import com.example.caller_id.model.CallLogItem
import com.example.caller_id.utils.SystemUtil
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NumberInfoActivity: BaseActivity<ActivityNumberInfoBinding>() {
    private var number = ""
    private lateinit var adapter: NumberInfoAdapter
    override fun setViewBinding(): ActivityNumberInfoBinding {
        return ActivityNumberInfoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        number = intent.getStringExtra("number") ?: ""
        binding.txtName.text = number
        val logs = getCallLogsForNumber(number)

        val incomingCount = logs.count { it.type == "Incoming" }
        val outgoingCount = logs.count { it.type == "Outgoing" }
        val missedCount = logs.count { it.type == "Missed" }
        val otherCount = logs.count { it.type == "Other" }

        val entries = listOf(
            PieEntry(incomingCount.toFloat()),
            PieEntry(outgoingCount.toFloat()),
            PieEntry(missedCount.toFloat()),
            PieEntry(otherCount.toFloat())
        )

        val dataSet = PieDataSet(entries, "").apply {
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
            colors = listOf(
                Color.parseColor("#4CAF50"), // xanh lá
                Color.parseColor("#F44336"), // đỏ
                Color.parseColor("#FF9800"), // cam
                Color.parseColor("#2196F3")  // xanh dương
            )
            setDrawValues(true)
            valueTextColor = Color.WHITE
            valueTextSize = 16f
            sliceSpace = 2f
        }

        val data = PieData(dataSet)

        binding.pieChart.apply {
            this.data = data
            description.isEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)
            setUsePercentValues(false)
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTouchEnabled(false)
            invalidate()
        }

        adapter = NumberInfoAdapter(this) { callLogItem ->

        }
        binding.rcvRecent.layoutManager = LinearLayoutManager(this)
        binding.rcvRecent.adapter= adapter
        adapter.updateList(logs)
    }

    override fun viewListener() {
    }

    private fun getCallLogsForNumber(number: String): List<CallLogItem> {
        val callLogs = mutableListOf<CallLogItem>()

        val projection = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION
        )

        val selection = "${CallLog.Calls.NUMBER} LIKE ?"
        val selectionArgs = arrayOf("%$number%")

        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CallLog.Calls.DATE} DESC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                val rawDate = it.getLong(dateIndex)

                val local = SystemUtil.getPreLanguage(this) ?: "en"
                val sdf = SimpleDateFormat("dd MMM", Locale(local))
                val dateFormatted = sdf.format(Date(rawDate))

                val type = when (it.getInt(typeIndex)) {
                    CallLog.Calls.INCOMING_TYPE -> "Incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                    CallLog.Calls.MISSED_TYPE -> "Missed"
                    else -> "Other"
                }

                callLogs.add(
                    CallLogItem(name, number, dateFormatted, type, rawDate)
                )
            }
        }

        return callLogs
    }

    override fun dataObservable() {
    }
}
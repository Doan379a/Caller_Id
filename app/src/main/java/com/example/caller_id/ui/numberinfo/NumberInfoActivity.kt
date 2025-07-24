package com.example.caller_id.ui.numberinfo

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.R
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.ActivityNumberInfoBinding
import com.example.caller_id.model.CallLogItem
import com.example.caller_id.utils.SystemUtil
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.tapRotate
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class NumberInfoActivity: BaseActivity<ActivityNumberInfoBinding>() {
    private var number = ""
    private var name = ""
    private lateinit var adapter: NumberInfoAdapter
    private var isCheckBlock = false
    private var isCheckFavorite = false
    private val viewModel: BlockViewModel by viewModels()
    override fun setViewBinding(): ActivityNumberInfoBinding {
        return ActivityNumberInfoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        number = intent.getStringExtra("number") ?: ""
        name = intent.getStringExtra("name") ?: number
        binding.txtName.text = name
        binding.ivNumber.text = number
        viewModel.checkIfBlocked(number) {
            isCheckBlock = it
            binding.ivBlock.alpha = if (isCheckBlock) 0.6f else 1f
        }

        viewModel.checkIfFavoritesBlocked(number) {
            isCheckFavorite = it
            binding.ivFavorites.alpha = if (isCheckFavorite) 0.6f else 1f
        }

        val numberInternational = normalizeToE164(number)
        val countryCode = getCountryFromPhoneNumber(numberInternational)
        val countryName = Locale("", countryCode).getDisplayCountry(Locale.ROOT)

        binding.txtLocal.text = countryName ?: getString(R.string.location)


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
                    return if (value == 0f) "" else value.toInt().toString()
                }
            }
            colors = listOf(
                Color.parseColor("#2196F3"),  // xanh dương
                Color.parseColor("#4CAF50"), // xanh lá
                Color.parseColor("#F44336"), // đỏ
                Color.parseColor("#FF9800"), // cam
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
        binding.ivBack.tap {
            finish()
        }
        binding.ivMore.tap {

        }
        binding.ivBlock.tapRotate {
            if (!isCheckBlock) {
                isCheckBlock = true
                binding.ivBlock.alpha = 0.6f
                viewModel.insertCallBlock(number, name = name, "number", false)
                Toast.makeText(this, "Đã chặn $number", Toast.LENGTH_SHORT).show()
            }else{
                isCheckBlock = false
                binding.ivBlock.alpha = 1f
                viewModel.deleteCallByNumber(number, "number")
                Toast.makeText(this, "Đã bỏ chặn $number", Toast.LENGTH_SHORT).show()
            }
        }
        binding.ivFavorites.tapRotate {
            if (!isCheckFavorite) {
                isCheckFavorite = true
                binding.ivFavorites.alpha = 0.6f
                viewModel.insertCallBlock(number, name = name, "favorites",false)
                Toast.makeText(this, "Đã thêm $number vào danh sách yêu thích", Toast.LENGTH_SHORT).show()
            }else{
                isCheckFavorite = false
                binding.ivFavorites.alpha = 1f
                viewModel.deleteCallByNumber(number, "favorites")
                Toast.makeText(this, "Đã bỏ $number khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show()
            }

        }
        binding.llSpam.tap {
            viewModel.insertCallBlock(number, name = name, "",true)
            Toast.makeText(this, "Đã thêm $number vào danh sách spam", Toast.LENGTH_SHORT).show()
        }

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
    fun getCountryFromPhoneNumber(number: String): String? {
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            val phoneNumber = phoneUtil.parse(number, null)
            val regionCode = phoneUtil.getRegionCodeForNumber(phoneNumber)
            regionCode
        } catch (e: NumberParseException) {
            null
        }
    }

    fun normalizeToE164( number: String): String {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simCountry = tm.simCountryIso?.uppercase(Locale.ROOT)

        return try {
            val parsed = phoneUtil.parse(number, simCountry)
            phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: Exception) {
            number
        }
    }
}
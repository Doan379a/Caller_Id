package com.example.caller_id.ui.main.fragment.calls

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.caller_id.R
import com.example.caller_id.base.BaseAdapter
import com.example.caller_id.database.entity.BlockedCalled
import com.example.caller_id.database.entity.DoNotDisturbNumber
import com.example.caller_id.databinding.ItemBlockedBinding
import com.example.caller_id.databinding.ItemCallLogBinding
import com.example.caller_id.databinding.ItemDonotdisturbBinding
import com.example.caller_id.model.CallLogItem
import com.example.caller_id.model.DndType
import com.example.caller_id.widget.setDrawableStartWithTint
import com.example.caller_id.widget.tap
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

class DoNotDisturbAdapter(val context: Context,
                          val onClick :(callLogItem: DoNotDisturbNumber)->Unit,
) :BaseAdapter<ItemDonotdisturbBinding, DoNotDisturbNumber>() {


    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemDonotdisturbBinding {
        return ItemDonotdisturbBinding.inflate(inflater, parent, false)
    }

    override fun creatVH(binding: ItemDonotdisturbBinding): RecyclerView.ViewHolder =
        CallVH(binding)

    inner class CallVH(binding: ItemDonotdisturbBinding) : BaseVH<DoNotDisturbNumber>(binding) {
        private var countDownTimer: CountDownTimer? = null
        override fun onItemClickListener(data: DoNotDisturbNumber) {
            super.onItemClickListener(data)
        }

        override fun bind(data: DoNotDisturbNumber) {
            super.bind(data)
            countDownTimer?.cancel()
        Log.d("dataendTimeMillis", "data: ${data.endTimeMillis}")
            when (data.type) {
                DndType.TIMER -> {
                    val remainingTime = data.endTimeMillis - System.currentTimeMillis()
                    if (remainingTime > 0) {
                        countDownTimer = object : CountDownTimer(remainingTime, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                val totalSeconds = millisUntilFinished / 1000
                                val hours = (totalSeconds / 3600) % 24
                                val minutes = (totalSeconds % 3600) / 60
                                val seconds = totalSeconds % 60

                                binding.txtByselect.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                            }

                            override fun onFinish() {
                                onClick.invoke(data)
                                binding.txtByselect.text = "00:00:00"
                            }
                        }.start()
                    } else {
                        binding.txtByselect.text = "00:00:00"
                    }
                }

                DndType.COUNTER -> {
                        binding.txtByselect.text = "Còn ${data.remainingCount} cuộc gọi"
                    }

                else -> {}
            }

            binding.tvAvatar.text = data.number.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            binding.cardAvatar.setCardBackgroundColor(getColorFromAddress())
            binding.txtName.text = data.number
            binding.ivClear.tap {
                onClick.invoke(data)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<DoNotDisturbNumber>) {
        listData.clear()
        listData.addAll(newList)
        notifyDataSetChanged()
    }

    fun getColorFromAddress(): Int {
        val colors = listOf(
            Color.parseColor("#EF5350"),
            Color.parseColor("#AB47BC"),
            Color.parseColor("#42A5F5"),
            Color.parseColor("#26A69A"),
            Color.parseColor("#FFA726"),
            Color.parseColor("#66BB6A"),
            Color.parseColor("#FF7043"),
        )

        return colors.random()
    }

}


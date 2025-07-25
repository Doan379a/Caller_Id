package com.example.caller_id.bottomsheet

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.caller_id.R
import com.example.caller_id.base.BaseBottomSheetFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.BottomsheetDisturbBinding
import com.example.caller_id.model.DndType
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.tapin
import com.example.caller_id.widget.visible
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DisturbBottomSheet(val numberPhone: String) : BaseBottomSheetFragment<BottomsheetDisturbBinding>() {
    private val vm: BlockViewModel by activityViewModels()
    private var counter = 0
    private var number = ""
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomsheetDisturbBinding {
        return BottomsheetDisturbBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.edtContent.setText(numberPhone)
        binding.txtCounter.text = "0"
        binding.include.number1.setFormatter { i -> String.format("%02d", i) }
        binding.include.number2.setFormatter { i -> String.format("%02d", i) }
        val counterText = binding.txtCounter.text.toString().trim()
        counter = if (counterText.isNotEmpty()) {
            counterText.toIntOrNull() ?: 0
        } else {
            0
        }
    }

    override fun bindView() {
        binding.ivPermanently.tap {
            number = binding.edtContent.text.toString().trim()
            if (number.isNotEmpty()) {
                vm.insertDndCalled(number, DndType.MANUALLY, endTimeMillis = 0L, remainingCount = 0)
                Toast.makeText(
                    requireContext(),
                    "Đã thêm số $number vào danh sách không làm phiền",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.ivClose.tap {
            dismiss()
        }
        binding.ivCounter.tap {
            number = binding.edtContent.text.toString().trim()
            if (number.isNotEmpty()) {
                binding.llCounter.visible()
                binding.llManually.gone()
                binding.llTimer.gone()
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.ivTime.tap {
            number = binding.edtContent.text.toString().trim()
            if (number.isNotEmpty()) {
            binding.llTimer.visible()
            binding.llManually.gone()
            binding.llCounter.gone()
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.ivMinus.tapin {
            if (counter == 0) {
                Toast.makeText(
                    requireContext(),
                    "Số lượng không được nhỏ hơn 0",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                counter--
                checkCounter(counter)
                binding.txtCounter.text = counter.toString()
            }
        }
        binding.ivPlus.tapin {
            counter++
            checkCounter(counter)
            binding.txtCounter.text = counter.toString()
        }
        binding.txt3.tap {
            counter = 3
            checkCounter(3)
            binding.txtCounter.text = counter.toString()
        }
        binding.txt6.tap {
            counter = 6
            checkCounter(6)
            binding.txtCounter.text = counter.toString()
        }
        binding.txt9.tap {
            counter = 9
            checkCounter(9)
            binding.txtCounter.text = counter.toString()
        }
        binding.txt12.tap {
            counter = 12
            checkCounter(12)
            binding.txtCounter.text = counter.toString()
        }
        binding.ivDoneCounter.tap {
            if (counter == 0) {
                Toast.makeText(
                    requireContext(),
                    "Số lượng không được nhỏ hơn 0",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                vm.insertDndCalled(
                    number,
                    DndType.COUNTER,
                    endTimeMillis = 0L,
                    remainingCount = counter
                )
                Toast.makeText(
                    requireContext(),
                    "Đã thêm số $number vào danh sách không làm phiền với $counter lần",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
        }

        binding.ivRsset.tap {
            counter = 0
            checkCounter(0)
            binding.txtCounter.text = counter.toString()
        }

        binding.txt30min.tap {
            binding.include.number1.value = 0
            binding.include.number2.value = 30
            checkTime(0, 30)
        }
        binding.txt1hour.tap {
            binding.include.number1.value = 1
            binding.include.number2.value = 0
            checkTime(1, 0)
        }
        binding.txt2hours.tap {
            binding.include.number1.value = 2
            binding.include.number2.value = 0
            checkTime(2, 0)
        }
        binding.txt8hours.tap {
            binding.include.number1.value = 8
            binding.include.number2.value = 0
            checkTime(8, 0)
        }

        binding.include.number1.setOnValueChangedListener { picker, oldVal, newVal ->
            checkTime(newVal, 0)
        }
        binding.include.number2.setOnValueChangedListener { picker, oldVal, newVal ->
            checkTime(0, newVal)
        }
        binding.ivDoneTimer.setOnClickListener {
            val hour = binding.include.number1.value
            val minute = binding.include.number2.value

            if (hour == 0 && minute == 0) {
                Toast.makeText(
                    requireContext(),
                    "Số lượng không được nhỏ hơn 0",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val endTimeMillis = System.currentTimeMillis() +
                        TimeUnit.HOURS.toMillis(hour.toLong()) +
                        TimeUnit.MINUTES.toMillis(minute.toLong())

                Log.d("endTimeMillis", "endTimeMillis: $endTimeMillis")
                vm.insertDndCalled(
                    number,
                    DndType.TIMER,
                    endTimeMillis = endTimeMillis,
                    remainingCount = 0)
                Toast.makeText(
                    requireContext(),
                    "Đã thêm số $number vào danh sách không làm phiền với $hour giờ $minute lần",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
        }

    }

    fun checkCounter(input: Int) {
        binding.txt3.setBackgroundResource(R.drawable.bg_conner_e4ebff)
        binding.txt6.setBackgroundResource(R.drawable.bg_conner_e4ebff)
        binding.txt9.setBackgroundResource(R.drawable.bg_conner_e4ebff)
        binding.txt12.setBackgroundResource(R.drawable.bg_conner_e4ebff)
        when (input) {
            3 -> binding.txt3.setBackgroundResource(R.drawable.bg_conner_stroke_e4ebff)
            6 -> binding.txt6.setBackgroundResource(R.drawable.bg_conner_stroke_e4ebff)
            9 -> binding.txt9.setBackgroundResource(R.drawable.bg_conner_stroke_e4ebff)
            12 -> binding.txt12.setBackgroundResource(R.drawable.bg_conner_stroke_e4ebff)
        }
    }

    fun checkTime(input: Int, input2: Int) {
        binding.txt30min.setBackgroundResource(R.drawable.bg_conner_e4ebff)
        binding.txt1hour.setBackgroundResource(R.drawable.bg_conner_e4ebff)
        binding.txt2hours.setBackgroundResource(R.drawable.bg_conner_e4ebff)
        binding.txt8hours.setBackgroundResource(R.drawable.bg_conner_e4ebff)
        when (input) {
            1 -> binding.txt1hour.setBackgroundResource(R.drawable.bg_conner_stroke_e4ebff)
            2 -> binding.txt2hours.setBackgroundResource(R.drawable.bg_conner_stroke_e4ebff)
            8 -> binding.txt8hours.setBackgroundResource(R.drawable.bg_conner_stroke_e4ebff)
        }
        when (input2) {
            30 -> binding.txt30min.setBackgroundResource(R.drawable.bg_conner_stroke_e4ebff)
        }
    }
}
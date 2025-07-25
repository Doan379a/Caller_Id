package com.example.caller_id.bottomsheet

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.R
import com.example.caller_id.base.BaseBottomSheetFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.BottomsheetContactBinding
import com.example.caller_id.databinding.BottomsheetDisturbBinding
import com.example.caller_id.model.DndType
import com.example.caller_id.utils.SmsUtils.getCallLogs
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.tapin
import com.example.caller_id.widget.visible
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FromContactBottomSheet(val type: Int) : BaseBottomSheetFragment<BottomsheetContactBinding>() {
    private val vm: BlockViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter
    private lateinit var adapterRecents: RecentsAdapter
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomsheetContactBinding {
        return BottomsheetContactBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val list = getCallLogs(requireActivity())
        adapterRecents = RecentsAdapter(requireActivity()){ data->
                 dismiss()
                DisturbBottomSheet(data.number).show(parentFragmentManager, "ExampleBottomSheet")

        }
        adapter = ContactAdapter(requireActivity()) {data->
            dismiss()
            DisturbBottomSheet(data.number).show(parentFragmentManager, "ExampleBottomSheet")
        }
        binding.rcv.layoutManager = LinearLayoutManager(requireActivity())

        if (type == 0){
            binding.rcv.adapter = adapter
            vm.contacts.observe(viewLifecycleOwner){data->
                adapter.updateList(data)
            }
        }else{
            binding.rcv.adapter = adapterRecents
            adapterRecents.updateList(list)
        }


    }

    override fun bindView() {
        binding.ivClose.tap {
            dismiss()
        }
    }

}
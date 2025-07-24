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
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.tapin
import com.example.caller_id.widget.visible
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FromContactBottomSheet(val type: Int) : BaseBottomSheetFragment<BottomsheetContactBinding>() {
    private val vm: BlockViewModel by activityViewModels()
    private var counter = 0
    private var number = ""
    private lateinit var adapter: ContactAdapter
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomsheetContactBinding {
        return BottomsheetContactBinding.inflate(layoutInflater)
    }

    override fun initView() {
        adapter = ContactAdapter(requireActivity()) {

        }
        binding.rcv.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcv.adapter = adapter
        if (type == 0){
            vm.contacts.observe(viewLifecycleOwner){data->
                adapter.updateList(data)
            }
        }else{

        }


    }

    override fun bindView() {

    }

}
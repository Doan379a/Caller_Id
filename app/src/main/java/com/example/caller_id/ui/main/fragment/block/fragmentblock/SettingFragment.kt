package com.example.caller_id.ui.main.fragment.block.fragmentblock

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentSettingBinding
import com.example.caller_id.dialog.BlockDialog
import com.example.caller_id.sharePreferent.SharePrefUtils
import com.example.caller_id.widget.tap
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding>() {
    private val vm: BlockViewModel by viewModels()
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val blockTopSpammer = SharePrefUtils.getSetting(context, "CBSPAMMER")
        val blockPrivate = SharePrefUtils.getSetting(context, "CBHIDDENNUMBER")
        val blockInternational = SharePrefUtils.getSetting(context, "CBINTERNATIONALCALLS")
        val blockUnknownContacts = SharePrefUtils.getSetting(context, "CBNOTINCONTACTS")

        binding.cbSpammer.isChecked = blockTopSpammer
        binding.cbHiddenNumber.isChecked = blockPrivate
        binding.cbInternationalCalls.isChecked = blockInternational
        binding.cbNotinContacts.isChecked = blockUnknownContacts


    }

    override fun viewListener() {
        binding.cbSpammer.setOnCheckedChangeListener { _, isChecked ->
            SharePrefUtils.saveSetting(requireActivity(), "CBSPAMMER", isChecked)
        }

        binding.cbHiddenNumber.setOnCheckedChangeListener { _, isChecked ->
            SharePrefUtils.saveSetting(requireActivity(), "CBHIDDENNUMBER", isChecked)
        }

        binding.cbInternationalCalls.setOnCheckedChangeListener { _, isChecked ->
            SharePrefUtils.saveSetting(requireActivity(), "CBINTERNATIONALCALLS", isChecked)
        }

        binding.cbNotinContacts.setOnCheckedChangeListener { _, isChecked ->
            SharePrefUtils.saveSetting(requireActivity(), "CBNOTINCONTACTS", isChecked)
        }

        binding.ivPhoneNumber.tap {
            BlockDialog(requireActivity(), 0) { number, type ->
                vm.insertCallBlock(number, name = "", type,false)
                Toast.makeText(requireContext(), "Đã chặn $number", Toast.LENGTH_SHORT).show()
            }.show()
        }
        binding.ivMessageSenderName.tap {
            BlockDialog(activity1 = requireActivity(), type = 1, actionBlock = { number, type ->
                vm.insertCallBlock(number, name = "",type,false)
                Toast.makeText(requireContext(), "Đã chặn $number", Toast.LENGTH_SHORT).show()
            }).show()
        }
        binding.ivCountryCode.tap {
            BlockDialog(activity1 = requireActivity(), type = 2, actionBlock = { number, type ->
                vm.insertCallBlock(number, name = "", type,false)
                Toast.makeText(requireContext(), "Đã chặn mã nước $number", Toast.LENGTH_SHORT).show()
            }).show()
        }
        binding.ivNumberSeries.tap {
            BlockDialog(activity1 = requireActivity(), type = 3, actionBlock = { number, type ->
                vm.insertCallBlock(number,  name = "", type,false)
                Toast.makeText(requireContext(), "Đã chặn chuỗi $number", Toast.LENGTH_SHORT).show()
            }).show()
        }
    }



    override fun dataObservable() {
    }

}
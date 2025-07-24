package com.example.caller_id.ui.main.fragment.calls.fragmentcall

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.R
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.bottomsheet.DisturbBottomSheet
import com.example.caller_id.database.entity.DoNotDisturbNumber
import com.example.caller_id.database.viewmodel.BlockViewModel
import com.example.caller_id.databinding.FragmentBlockedBinding
import com.example.caller_id.databinding.FragmentDisturbBinding
import com.example.caller_id.model.DndType
import com.example.caller_id.service.DndListenerManager
import com.example.caller_id.service.DndUpdateListener
import com.example.caller_id.ui.main.fragment.calls.DoNotDisturbAdapter
import com.example.caller_id.widget.tap

class DisturbFragment : BaseFragment<FragmentDisturbBinding>() {
    private val vm: BlockViewModel by activityViewModels()
    private lateinit var adapter: DoNotDisturbAdapter
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDisturbBinding {
        return FragmentDisturbBinding.inflate(layoutInflater)
    }

    override fun initView() {
        deleteExpired()
        adapter = DoNotDisturbAdapter(requireContext(), { data ->
            vm.deleteDndCalled(data.id)
            Toast.makeText(requireActivity(), "Đã xóa số ${data.number}", Toast.LENGTH_SHORT).show()
        })


        binding.rcvDnd.layoutManager = LinearLayoutManager(requireActivity())
        binding.rcvDnd.adapter = adapter
        vm.dndCalledList.observe(viewLifecycleOwner) {
            adapter.updateList(it)
        }

    }

    override fun viewListener() {
        binding.ivManually.tap {
            DisturbBottomSheet().show(parentFragmentManager, "ExampleBottomSheet")
        }
        binding.cbDisturb.setOnCheckedChangeListener { _, isChecked ->
            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!notificationManager.isNotificationPolicyAccessGranted) {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    startActivity(intent)

                    binding.cbDisturb.isChecked = false
                    return@setOnCheckedChangeListener
                }

                val filter = if (isChecked) {
                    NotificationManager.INTERRUPTION_FILTER_NONE
                } else {
                    NotificationManager.INTERRUPTION_FILTER_ALL
                }
                notificationManager.setInterruptionFilter(filter)

                if (isChecked) {
                    binding.ll2.alpha = 0.4f
                    binding.ivManually.isEnabled = false
                    binding.ivHistory.isEnabled = false
                    binding.ivContact.isEnabled  =false
                } else {
                    binding.ll2.alpha = 1f
                    binding.ivManually.isEnabled = true
                    binding.ivHistory.isEnabled = true
                    binding.ivContact.isEnabled  =true
                }


            }
        }
    }

    override fun onResume() {
        super.onResume()
        DndListenerManager.listener = object : DndUpdateListener {
            override fun onDndUpdated(data: DoNotDisturbNumber) {
                vm.decreaseCounter(data.number)
                vm.deleteIfCounterReached(data.number)
                vm.dndCalledList.observe(viewLifecycleOwner) {
                    adapter.updateList(it)
                }
            }
        }
        deleteExpired()
        updateDisturbCheckboxState()
    }

    fun deleteExpired() {
        val now = System.currentTimeMillis()
        Log.d("now", "now: $now")
        vm.deleteExpired(now)
    }

    private fun updateDisturbCheckboxState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                val currentFilter = notificationManager.currentInterruptionFilter
                binding.cbDisturb.isChecked =
                    (currentFilter == NotificationManager.INTERRUPTION_FILTER_NONE)
            } else {
                binding.cbDisturb.isChecked = false
            }
        }
    }

    override fun dataObservable() {
    }
}
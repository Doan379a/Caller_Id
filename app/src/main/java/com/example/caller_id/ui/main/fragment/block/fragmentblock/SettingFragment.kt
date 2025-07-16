package com.example.caller_id.ui.main.fragment.block.fragmentblock

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.example.caller_id.R
import com.example.caller_id.base.BaseFragment
import com.example.caller_id.databinding.FragmentFavoritesBinding
import com.example.caller_id.databinding.FragmentSettingBinding
import com.example.caller_id.model.CountryCodeItem
import com.example.caller_id.sharePreferent.SharePrefUtils
import com.example.caller_id.ui.main.fragment.block.CountrySpinnerAdapter
import com.example.caller_id.utils.SystemUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

class SettingFragment : BaseFragment<FragmentSettingBinding>() {
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

        val countryList = getAllCountryCodes()
        val adapter = CountrySpinnerAdapter(requireActivity(), countryList)
        binding.spinnerCountry.adapter = adapter

        val savedIso = SharePrefUtils.getSavedCountryIso(requireContext())
        val savedIndex = countryList.indexOfFirst { it.isoCode.equals(savedIso, ignoreCase = true) }
        if (savedIndex != -1) {
            binding.spinnerCountry.setSelection(savedIndex)
        }

        binding.spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = countryList[position]
                SharePrefUtils.saveCountrySelection(requireContext(), selected.isoCode)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun viewListener() {
            binding.cbSpammer.setOnCheckedChangeListener { _, isChecked ->
                SharePrefUtils.saveSetting(requireActivity(),"CBSPAMMER", isChecked)
            }

            binding.cbHiddenNumber.setOnCheckedChangeListener { _, isChecked ->
                SharePrefUtils.saveSetting(requireActivity(),"CBHIDDENNUMBER", isChecked)
            }

            binding.cbInternationalCalls.setOnCheckedChangeListener { _, isChecked ->
                SharePrefUtils.saveSetting(requireActivity(),"CBINTERNATIONALCALLS", isChecked)
            }

            binding.cbNotinContacts.setOnCheckedChangeListener { _, isChecked ->
                SharePrefUtils.saveSetting(requireActivity(),"CBNOTINCONTACTS", isChecked)
            }

    }
    fun getAllCountryCodes(): List<CountryCodeItem> {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val regionCodes = phoneUtil.supportedRegions

        return regionCodes.mapNotNull { region ->
            try {
                val code = phoneUtil.getCountryCodeForRegion(region)
                val local = SystemUtil.setLocale(requireActivity())
                val name = Locale("", region).getDisplayCountry(Locale(local.toString()))
                if (name.isBlank()) return@mapNotNull null
                CountryCodeItem(name, "+$code", region)
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.countryName }
    }

    override fun dataObservable() {
    }

}
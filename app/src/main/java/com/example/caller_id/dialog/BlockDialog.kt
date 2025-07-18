package com.example.caller_id.dialog

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.caller_id.R
import com.example.caller_id.base.BaseDialog
import com.example.caller_id.databinding.DialogBlockBinding
import com.example.caller_id.model.CountryCodeItem
import com.example.caller_id.sharePreferent.SharePrefUtils
import com.example.caller_id.ui.main.fragment.block.adapterblock.CountrySpinnerAdapter
import com.example.caller_id.utils.SystemUtil
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.visible
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

class BlockDialog(
    activity1: Activity,
    val type: Int,
    private var actionBlock: (String,String) -> Unit
) : BaseDialog<DialogBlockBinding>(activity1, true) {
    private var  spin:String = ""
    override fun getContentView(): DialogBlockBinding {
        return DialogBlockBinding.inflate(LayoutInflater.from(activity))
    }

    override fun initView() {
        val countryList = getAllCountryCodes()
        val adapter = CountrySpinnerAdapter(activity, countryList)
        binding.spinnerCountry.adapter = adapter

        val savedIso = SharePrefUtils.getSavedCountryIso(activity)
        val savedIndex = countryList.indexOfFirst { it.isoCode.equals(savedIso, ignoreCase = true) }
        if (savedIndex != -1) {
            binding.spinnerCountry.setSelection(savedIndex)
        }

        binding.spinnerCountry.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selected = countryList[position]
                    spin = selected.dialCode
                    SharePrefUtils.saveCountrySelection(activity, selected.isoCode)
                    Log.d("Spinner", selected.toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
            if (type == 0){
                binding.txtTitle.text = activity.getString(R.string.block_a_number)
                binding.txtSpin.gone()
                binding.spinnerCountry.gone()
            }else if (type == 1){
                binding.txtTitle.text = activity.getString(R.string.block_a_sender_name)
                binding.txtContent.text = activity.getString(R.string.any_sender_which_the_name_you_enter)
                binding.txtSpin.gone()
                binding.spinnerCountry.gone()
            }else if (type == 2){
                binding.txtTitle.text = activity.getString(R.string.block_a_country_code)
                binding.edtContent.gone()
                binding.spinnerCountry.visible()
                binding.txtSpin.visible()
                binding.txtContent.gone()
            }else{
                binding.txtTitle.text = activity.getString(R.string.block_a_number_series)
                binding.spinnerCountry.visible()
                binding.txtSpin.gone()
                binding.txtContent.gone()
            }

    }

    override fun bindView() {
        binding.apply {
            ivBlock.tap {
                val number = edtContent.text.toString().trim()
                if (type == 1){
                    actionBlock.invoke(number,"sender")
                }
                if (type == 2 ){
                    actionBlock.invoke(spin,"country")
                    dismiss()
                }else if (type == 3){
                    if (number.isNotEmpty()) {
                        actionBlock.invoke(number,"numberstart")
                        binding.edtContent.text.clear()
                    } else {
                        Toast.makeText(activity, "Vui lòng nhập số", Toast.LENGTH_SHORT).show()
                    }
                } else{
                    if (number.isNotEmpty()) {
                        actionBlock.invoke(number,"number")
                        binding.edtContent.text.clear()
                    } else {
                        Toast.makeText(activity, "Vui lòng nhập số", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            ivClear.tap {
                dismiss()
            }
        }
    }
    fun getAllCountryCodes(): List<CountryCodeItem> {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val regionCodes = phoneUtil.supportedRegions

        return regionCodes.mapNotNull { region ->
            try {
                val code = phoneUtil.getCountryCodeForRegion(region)
                val local = SystemUtil.setLocale(activity)
                val name = Locale("", region).getDisplayCountry(Locale(local.toString()))
                if (name.isBlank()) return@mapNotNull null
                CountryCodeItem(name, "+$code", region)
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.countryName }
    }

}
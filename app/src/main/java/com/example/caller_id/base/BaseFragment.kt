package com.example.caller_id.base

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.caller_id.utils.SystemUtil

abstract class BaseFragment<T : ViewBinding> : Fragment() {
    protected lateinit var binding: T

    protected abstract fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): T
    protected abstract fun initView()
    protected abstract fun viewListener()
    protected abstract fun dataObservable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = setViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SystemUtil.setLocale(requireActivity())
        initView()
        viewListener()
        dataObservable()
    }
    protected fun hideKeyboard() {
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        view?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            it.clearFocus()
        }
    }
    fun isDefaultSmsApp(): Boolean {
        return requireActivity().packageName == Telephony.Sms.getDefaultSmsPackage(requireActivity())
    }


    fun requestDefaultSmsRoleIfNeeded() {
        val context = requireActivity()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireActivity().getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    startActivityForResult(intent, 2001)
                }
            } else {
            }
        } else {
            val defaultApp = Telephony.Sms.getDefaultSmsPackage(context)
            if (defaultApp != context.packageName) {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                    putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                }
                startActivityForResult(intent, 2002)
            } else {
            }
        }
    }
}
package com.example.caller_id.ui.keypad

import android.annotation.SuppressLint
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityKeypadBinding
import com.example.caller_id.model.ContactModel
import com.example.caller_id.utils.SmsUtils.loadContacts
import com.example.caller_id.widget.getLogDebug
import com.example.caller_id.widget.normalize
import com.example.caller_id.widget.showSnackBar
import kotlinx.coroutines.launch

class KeypadActivity : BaseActivity<ActivityKeypadBinding>() {
    private val adapter: KeypadAdapter by lazy { KeypadAdapter(mutableListOf()) }
    private var fullContactList = mutableListOf<ContactModel>()
    private var filteredList = mutableListOf<ContactModel>()
    private var listButtons = listOf<View>()
    private var isDeleting = false

    override fun setViewBinding(): ActivityKeypadBinding {
        return ActivityKeypadBinding.inflate(layoutInflater)
    }

    override fun initView() {
        listButtons = listOf(
            binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6,
            binding.btn7, binding.btn8, binding.btn9,
            binding.btnStar, binding.btn0, binding.btnHash
        )
        binding.edtNumber.showSoftInputOnFocus = false
        binding.rcvContact.layoutManager = LinearLayoutManager(this)
        binding.rcvContact.adapter = adapter

        lifecycleScope.launch {
            fullContactList = loadContacts(this@KeypadActivity).toMutableList()
            filteredList = fullContactList.toMutableList()
            adapter.updateList(filteredList)
            search()
        }
    }

    private fun search() = binding.apply {
        edtNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                filteredList = if (query.isEmpty()) {
                    fullContactList.toMutableList()
                } else {
                    fullContactList.filter { it.number.contains(query) }.toMutableList()
                }
                adapter.updateList(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun viewListener() {
        adapter.onClickItem={data->
            showSnackBar("data")
            binding.edtNumber.setText(data.number)

        }
        listButtons.forEach { btn ->
            btn.setOnClickListener {
                val d = (it as Button).text.toString()
                binding.edtNumber.append(d)
                binding.edtNumber.setSelection(binding.edtNumber.length())
            }
        }

        binding.imgDelete.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDeleting = true
                    startDeleting()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDeleting = false
                    true
                }
                else -> false
            }
        }
    }

    private fun startDeleting() {
        binding.edtNumber.post(object : Runnable {
            override fun run() {
                if (isDeleting && binding.edtNumber.text.isNotEmpty()) {
                    binding.edtNumber.text.delete(
                        binding.edtNumber.length() - 1,
                        binding.edtNumber.length()
                    )
                    binding.edtNumber.postDelayed(this, 100)
                }
            }
        })
    }

    override fun dataObservable() {
    }

}
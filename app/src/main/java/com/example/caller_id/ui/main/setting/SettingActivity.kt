package com.example.caller_id.ui.main.setting

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.widget.RadioGroup
import com.example.caller_id.R
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivitySettingBinding
import com.example.caller_id.sharePreferent.SharePrefUtils
import com.example.caller_id.ui.language.LanguageActivity
import com.example.caller_id.utils.helper.HelperMenu
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.tap

class SettingActivity : BaseActivity<ActivitySettingBinding>(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private var helperMenu: HelperMenu? = null

    override fun setViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(layoutInflater)
    }

    override fun initView() {


    }

    override fun viewListener() {
        binding.apply {
            tvRate.tap { helperMenu?.showDialogRate(false) }
            tvFeedback.tap { helperMenu?.showDialogFeedback() }
            tvShare.tap { helperMenu?.showShareApp() }
            tvPolicy.tap { helperMenu?.showPolicy() }
            tvLanguage.tap { showActivity(LanguageActivity::class.java) }
            ivBack.tap { finish() }
        }
    }


    override fun dataObservable() {
        helperMenu = HelperMenu(this)

        val prefs = getSharedPreferences("data", Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == null)
            return

        if (SharePrefUtils.isRated(this))
            binding.tvRate.gone()
    }

}
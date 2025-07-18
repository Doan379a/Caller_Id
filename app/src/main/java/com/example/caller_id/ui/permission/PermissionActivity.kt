package  com.example.caller_id.ui.permission

import android.content.Intent
import com.example.caller_id.ui.main.MainActivity
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityPermissionBinding
import com.example.caller_id.sharePreferent.SharePrefUtils
import com.example.caller_id.utils.helper.Default.CALL_PHONE
import com.example.caller_id.utils.helper.Default.SEND_READ_SMS
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.visible


class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {


    override fun setViewBinding(): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        if (checkPermission(CALL_PHONE)) {
            allowCallLogPermission()
        }
        if (checkPermission(SEND_READ_SMS)) {
            allowRedSmsPermission()
        }
        if (isDefaultSmsApp()) {
            updateSmsDefaultUI(true)
        } else {
            updateSmsDefaultUI(false)
        }
    }

    override fun viewListener() {
        binding.apply {
            ivSetDefaultSMSPermission.tap {
                requestDefaultSmsRoleIfNeeded()
            }
            ivSetCallLogsPermission.tap {
                showDialogPermission(CALL_PHONE)
            }
            ivSetReadSmsPermission.tap {
                showDialogPermission(SEND_READ_SMS)
            }
            tvContinue.tap {
                SharePrefUtils.forceGoToMain(this@PermissionActivity)
                showActivity(MainActivity::class.java)
                finishAffinity()
            }
        }

    }


    override fun dataObservable() {
    }

    override fun onPermissionGranted() {
        if (checkPermission(CALL_PHONE)) {
            allowCallLogPermission()
        }
        if (checkPermission(SEND_READ_SMS)) {
            allowRedSmsPermission()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onResume() {
        if (checkPermission(CALL_PHONE)) {
            allowCallLogPermission()
        }
        if (checkPermission(SEND_READ_SMS)) {
            allowRedSmsPermission()
        }
        super.onResume()
    }


    private fun allowCallLogPermission() {
        binding.ivSetCallLogsPermission.gone()
        binding.ivSelectCallLogsPermission.visible()
    }
    private fun allowRedSmsPermission() {
        binding.ivSetReadSmsPermission.gone()
        binding.ivSelectRedSmsPermission.visible()
    }

    private fun updateSmsDefaultUI(isDefault: Boolean) {
        if (isDefault) {
            binding.ivSetDefaultSMSPermission.gone()
            binding.ivSelectDefaultSMSPermission.visible()
        } else {
            binding.ivSetDefaultSMSPermission.visible()
            binding.ivSelectDefaultSMSPermission.gone()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == 2001 || requestCode == 2002) && isDefaultSmsApp()) {
            updateSmsDefaultUI(true)
        }
    }

}
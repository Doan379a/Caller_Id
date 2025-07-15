package  com.example.caller_id.ui.permission

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import com.example.caller_id.ui.main.MainActivity
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityPermissionBinding
import com.example.caller_id.sharePreferent.SharePrefUtils
import com.example.caller_id.utils.helper.Default.CALL_PHONE
import com.example.caller_id.utils.helper.Default.SEND_READ_SMS
import com.example.caller_id.utils.helper.Default.STORAGE_PERMISSION
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
            tvContinue.tap {
                SharePrefUtils.forceGoToMain(this@PermissionActivity)
                showActivity(MainActivity::class.java)
                finishAffinity()
            }
        }

    }

    private fun requestDefaultSmsRoleIfNeeded() {
        val context = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    startActivityForResult(intent, 2001)
                }
            } else {
                updateSmsDefaultUI(true)
            }
        } else {
            val defaultApp = Telephony.Sms.getDefaultSmsPackage(context)
            if (defaultApp != context.packageName) {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                    putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                }
                startActivityForResult(intent, 2002)
            } else {
                updateSmsDefaultUI(true)
            }
        }
    }

    override fun dataObservable() {
    }

    override fun onPermissionGranted() {
        if (checkPermission(CALL_PHONE)) {
            allowCallLogPermission()
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
        super.onResume()
    }


    private fun allowCallLogPermission() {
        binding.ivSetCallLogsPermission.gone()
        binding.ivSelectCallLogsPermission.visible()
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

    private fun isDefaultSmsApp(): Boolean {
        return Telephony.Sms.getDefaultSmsPackage(this) == packageName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == 2001 || requestCode == 2002) && isDefaultSmsApp()) {
            updateSmsDefaultUI(true)
        }
    }

}
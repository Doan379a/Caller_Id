package  com.example.caller_id.ui.permission

import com.example.caller_id.ui.main.MainActivity
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityPermissionBinding
import com.example.caller_id.sharePreferent.SharePrefUtils
import com.example.caller_id.utils.helper.Default.STORAGE_PERMISSION
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.visible


class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {


    override fun setViewBinding(): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        if (checkPermission(STORAGE_PERMISSION)) {
            allowCameraPermission()
        }

    }

    override fun viewListener() {
        binding.apply {
            ivSetCameraPermission.tap {
                showDialogPermission(STORAGE_PERMISSION)
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
        if (checkPermission(STORAGE_PERMISSION)) {
            allowCameraPermission()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onResume() {
        if (checkPermission(STORAGE_PERMISSION)) {
            allowCameraPermission()
        }
        super.onResume()
    }

    private fun allowCameraPermission() {
        binding.ivSetCameraPermission.gone()
        binding.ivSelectCameraPermission.visible()
    }

}
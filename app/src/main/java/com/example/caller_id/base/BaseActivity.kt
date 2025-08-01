package com.example.caller_id.base

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.example.caller_id.dialog.PermissionDialog
import com.example.caller_id.utils.SystemUtil
import com.example.caller_id.widget.getTagDebug
import com.example.caller_id.widget.showSnackBar

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    //_binding là biến nullable dùng để lưu trữ binding
    private var _binding: T? = null

    //binding là biến lateinit dùng đẻ khởi tạo binding sau này
    lateinit var binding: T

    //Tag dùng đẻ log
    private var TAG = ""

    //currentApiVersion lưu phiên bản hiện taại
    private var currentApiVersion = 0

    //isServiceBound dùng đẻ kểm tra xem service có được liên kết hay không
    private var isServiceBound = false


    //kết nối với service
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            isServiceBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isServiceBound = false
        }
    }


    // yêu cầu cấp quyền với ActivityResult API
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { callback ->
            if (callback.containsValue(false)) {
                onPermissionDenied()
            } else {
                onPermissionGranted()
            }
        }

    private val requestPermissionActivity =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { callback ->
            if (callback.resultCode == RESULT_OK)
                onPermissionGranted()
            else
                onPermissionDenied()
        }

    //các phương thức trưừu tượng được các lớp con sử dụng
    protected abstract fun setViewBinding(): T
    protected abstract fun initView()
    protected abstract fun viewListener()
    protected abstract fun dataObservable()
    open fun onPermissionGranted() {}
    open fun onPermissionDenied() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        currentApiVersion = Build.VERSION.SDK_INT
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = flags
            val decorView = window.decorView
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    decorView.systemUiVisibility = flags
                }
            }
        }
        super.onCreate(savedInstanceState)
//       try {
//            // Thiết lập ngôn ngữ hệ thống
        SystemUtil.setLocale(this)
        // Khởi tạo binding
        _binding = setViewBinding()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        TAG = "$localClassName check"
        _binding?.let {
            binding = it
            setContentView(binding.root)
            initView()
            dataObservable()
            viewListener()
        }
//    } catch (e: Exception) {         e.printStackTrace()
//
//       }


        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CRASH", "Uncaught: ${throwable.message}", throwable)
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    fun isDefaultSmsApp(): Boolean {
        return packageName == Telephony.Sms.getDefaultSmsPackage(this)
    }

    override fun onResume() {
        super.onResume()
    }

    fun requestDefaultSmsRoleIfNeeded() {
        val context = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
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
    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(connection)
            isServiceBound = false
        }
    }

    //thiết lập lại màn hình
    private fun fullScreen() {
        val windowInsetsController: WindowInsetsControllerCompat? =
            if (Build.VERSION.SDK_INT >= 30) {
                ViewCompat.getWindowInsetsController(window.decorView)
            } else {
                WindowInsetsControllerCompat(window, binding.root)
            }

        if (windowInsetsController == null) {
            return
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        windowInsetsController.hide(WindowInsetsCompat.Type.systemGestures())
    }

    fun showActivity(activity: Class<*>, bundle: Bundle?) {
        val intent = Intent(this, activity)
        intent.putExtras(bundle ?: Bundle())
        startActivity(intent)
    }

    // Chuyển đến Activity khác không cần bundle
    fun showActivity(activity: Class<*>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }


    open fun showDialogPermission(permission: Array<String>) {
        for (per in permission) {
            if (!checkPermission(per)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, per)) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts(
                        "package",
                        applicationContext.packageName,
                        null
                    )
                    intent.data = uri
                    val dialogPermission = PermissionDialog(this) {
                        requestPermissionActivity.launch(intent)
                    }
                    dialogPermission.show()
                } else {
                    requestPermissionLauncher.launch(permission)
                }
                return
            }
        }
        onPermissionGranted()
    }

    override fun onPause() {
        super.onPause()
    }

    protected open fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    protected open fun isNotificationServiceEnabled(): Boolean {
        val cn = packageName
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(this)
        return enabledListeners.contains(cn)
    }

    protected open fun checkPermission(permission: Array<String>): Boolean {
        for (permisson in permission) {
            val allow = ActivityCompat.checkSelfPermission(
                this, permisson
            ) == PackageManager.PERMISSION_GRANTED
            if (!allow) return false
        }
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            currentFocus?.let { view ->
                if (view is EditText) {
                    val outRect = Rect()
                    view.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        // Ẩn bàn phím
                        view.clearFocus()
                        val imm =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
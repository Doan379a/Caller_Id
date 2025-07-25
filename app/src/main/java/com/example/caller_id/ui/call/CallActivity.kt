package com.example.caller_id.ui.call

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.text.SpannableString
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.example.caller_id.R
import com.example.caller_id.base.BaseActivity
import com.example.caller_id.databinding.ActivityCallscreenBinding
import com.example.caller_id.widget.gone
import com.example.caller_id.widget.tap
import com.example.caller_id.widget.visible
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.Locale
import java.util.concurrent.TimeUnit


class CallActivity : BaseActivity<ActivityCallscreenBinding>() {
    private var updatesDisposable: Disposable? = null
    private val timerDisposable: Disposable? = null
    private val compositeDisposable = CompositeDisposable()
    private var tts: TextToSpeech? = null
  //  private var pendingSpeakText: String? = null
    private var isTtsReady = false
  //  private lateinit var announcer: Announcer
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private val handler = Handler(Looper.getMainLooper())
    private var isFlashOn = false
    private var flashRunnable: Runnable? = null
    private var blinking = false
    private var isCheckKeyBoard = false
    private var isCheckMute = false
    private var isCheckSpeaker = false
    override fun setViewBinding(): ActivityCallscreenBinding {
        return ActivityCallscreenBinding.inflate(layoutInflater)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun initView() {
       // announcer = SharePrefUtils.getAnnouncer(this)
       // val idTheme = SharePrefUtils.getCallTheme(this)
       // val listTheme = getDefaultCallTheme()
      //  val selectedTheme = listTheme.find { it.id == idTheme }
       // val imageRes = selectedTheme?.image
        hideBottomNavigationBar()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]
//        Glide.with(this)
//            .load(imageRes)
//            .into(binding.imageBackground)

        setupListeners()

    }

    override fun viewListener() {
        binding.buttonHangup.tap {
            shutdownTtsIfReady()
            CallManager.cancelCall()
        }
        binding.buttonAnswer.tap {
            shutdownTtsIfReady()
            CallManager.acceptCall()
        }
        binding.buttonHangup2.tap {
            binding.llNumber.gone()
            binding.ctlContent.visible()
            shutdownTtsIfReady()
            CallManager.cancelCall()
        }
        binding.ivKeyboard.tap {
            isCheckKeyBoard = !isCheckKeyBoard
            if (isCheckKeyBoard) {
                binding.ivKeyboard.setImageResource(R.drawable.ic_keyboard_selected)
                binding.llNumber.visible()
                binding.ctlContent.gone()
            } else {
                binding.ivKeyboard.setImageResource(R.drawable.ic_keyboard)
                binding.llNumber.gone()
                binding.ctlContent.visible()
            }
        }
        binding.ivMute.tap {
            isCheckMute = !isCheckMute
            CallManager.setMicrophoneMute(isCheckMute)
            binding.ivMute.setImageResource(
                if (isCheckMute) R.drawable.ic_mute_selected
                else R.drawable.ic_mute
            )
        }
        binding.ivVolume.tap {
            isCheckSpeaker = !isCheckSpeaker
            CallManager.setSpeaker( isCheckSpeaker)
            binding.ivVolume.setImageResource(
                if (isCheckSpeaker) R.drawable.ic_callscreen_speaker_selected
                else R.drawable.ic_callscreen_speaker
            )
        }

    }

    override fun dataObservable() {
    }

    override fun onResume() {
        super.onResume()
        updatesDisposable = CallManager.updates()
            .doOnError { throwable: Any? ->
                Log.e("LOG_TAG", "Error processing call")
            }
            .subscribe { it: Any ->
                updateView(it as GsmCall)
            }
    }

    private fun updateView(gsmCall: GsmCall) {
        if (gsmCall.status != GsmCall.Status.ACTIVE) {
            binding.textStatus.visible()
            binding.textDuration.gone()
        }
        when (gsmCall.status) {
            GsmCall.Status.CONNECTING -> {
                shutdownTtsIfReady()
                binding.textStatus.text = getString(R.string.connecting)
            }

            GsmCall.Status.DIALING -> {
                shutdownTtsIfReady()
                binding.buttonAnswer.gone()
                binding.buttonHangup.visible()
                binding.textStatus.text = getString(R.string.is_calling)
               // pendingSpeakText = "-"
            }

            GsmCall.Status.RINGING -> {
                var announcement = ""

                binding.textStatus.text = getString(R.string.incoming_call)
//                announcement = if (announcer.optionalName && announcer.optionalPhone) {
//                    getString(R.string.there_is_call_from) + (gsmCall.displayName
//                        ?: getString(R.string.unknown)) + getString(R.string.number) + (gsmCall.displayNumber
//                        ?: getString(R.string.unknown))
//                } else if (announcer.optionalPhone) {
//                    getString(R.string.there_is_an_incoming_number) + (gsmCall.displayNumber
//                        ?: getString(R.string.unknown))
//                } else {
//                    getString(R.string.there_is_call_from) + (gsmCall.displayName
//                        ?: getString(R.string.unknown))
//                }
              //  pendingSpeakText = announcement
                binding.buttonAnswer.visible()
                binding.buttonHangup.visible()
            }

            GsmCall.Status.ACTIVE -> {
                binding.buttonAnswer.gone()
                binding.buttonHangup.gone()
                binding.llMore.visible()
                stopFlashBlinking()
                binding.textStatus.text = ""
                binding.textStatus.gone()
                binding.textDuration.visible()
                startTimer()
            }

            GsmCall.Status.DISCONNECTED -> {
                stopFlashBlinking()
                shutdownTtsIfReady()
                binding.textStatus.text = getString(R.string.finished_call)
                binding.llMore.gone()
                binding.buttonAnswer.gone()
                binding.buttonHangup.gone()
                binding.llMore.postDelayed({ finish() }, 3000)
                stopTimer()
            }

            GsmCall.Status.UNKNOWN -> binding.textStatus.text = ""
        }

        binding.textDisplayName.apply {
            text = gsmCall.displayName ?: getString(R.string.unknown)
        }

        binding.textDisplayNumber.text = gsmCall.displayNumber?.also {
            Log.d("LOG_TAG", "Display number: $it")
        } ?: "Unknown"

    }

    override fun onPause() {
        super.onPause()
    }

    private fun hideBottomNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }

    private fun startTimer() {
        val timer = Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { time: Long -> binding.textDuration.text = toDurationString(time) },
                { error: Throwable -> Log.e("TIMER", "Lỗi: " + error.message) }
            )

        compositeDisposable.add(timer)
    }

    private fun stopTimer() {
        compositeDisposable.clear()
    }

    private fun shutdownTtsIfReady() {
        if (isTtsReady) {
            tts?.stop()
            tts?.shutdown()
        } else {
            Log.w("TTS", "Không thể shutdown vì TTS chưa sẵn sàng.")
        }
    }

    private fun toDurationString(l: Long): String {
        return String.format("%02d:%02d:%02d", l / 3600, (l % 3600) / 60, (l % 60))
    }


    private fun stopFlashBlinking() {
        blinking = false
        flashRunnable?.let { handler.removeCallbacks(it) }
        if (isFlashOn) {
            try {
                cameraManager.setTorchMode(cameraId!!, false)
                isFlashOn = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupListeners() {
        binding.tv0.setOnClickListener {
            appendNumber("0")
        }
        binding.tv1.setOnClickListener {
            appendNumber("1")
        }
        binding.tv2.setOnClickListener {
            appendNumber("2")
        }
        binding.tv3.setOnClickListener {
            appendNumber("3")
        }
        binding.tv4.setOnClickListener {
            appendNumber("4")
        }
        binding.tv5.setOnClickListener {
            appendNumber("5")
        }
        binding.tv6.setOnClickListener {
            appendNumber("6")
        }
        binding.tv7.setOnClickListener {
            appendNumber("7")
        }
        binding.tv8.setOnClickListener {
            appendNumber("8")
        }
        binding.tv9.setOnClickListener {
            appendNumber("9")
        }
        binding.tvSao.setOnClickListener {
            appendNumber("*")
        }
        binding.tvThang.setOnClickListener {
            appendNumber("#")
        }

    }

    private fun appendNumber(number: String) {
        CallManager.playDtm(number)
        val currentText = binding.tvOperation.text.toString()
        val newText = currentText + number
        binding.tvOperation.text = SpannableString(newText)
    }

}

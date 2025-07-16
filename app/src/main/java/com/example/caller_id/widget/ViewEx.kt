package com.example.caller_id.widget


import android.content.Context
import android.telephony.TelephonyManager
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.util.Locale


fun View.tap(action: (view: View?) -> Unit) {
    setOnClickListener {
        it.isEnabled = false
        it.postDelayed({ it.isEnabled = true }, 1500)
        action(it)
    }
}


fun View.tapin(action: (view: View?) -> Unit) {
    setOnClickListener {
        it.isEnabled = false
        it.postDelayed({ it.isEnabled = true }, 10)
        action(it)
    }
}

fun View.tapRotate(action: (view: View?) -> Unit) {
    setOnClickListener {
        it.isEnabled = false
        it.postDelayed({ it.isEnabled = true }, 500)
        action(it)
    }
}


fun View.visible() {
    visibility = View.VISIBLE // hiện view
}

fun View.gone() {
    visibility = View.GONE // ẩn view
}

fun View.invisible() {
    visibility = View.INVISIBLE
}
fun View.visibleOrGone(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

fun TextView.setDrawableTopWithTint(drawableRes: Int, colorRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes)
    drawable?.let {
        val wrappedDrawable = it.mutate()
        wrappedDrawable.setTint( colorRes)
        setCompoundDrawablesWithIntrinsicBounds(null, wrappedDrawable, null, null)
    }
}
fun TextView.setDrawableStartWithTint(drawableResId: Int, color: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableResId)?.mutate()
    drawable?.setTint(color)
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}


fun isInternationalNumber(context: Context, number: String): Boolean {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val simCountry = telephonyManager.simCountryIso.uppercase(Locale.ROOT)

    return try {
        val parsedNumber: Phonenumber.PhoneNumber =
            phoneUtil.parse(number, simCountry)

        parsedNumber.countryCode != phoneUtil.getCountryCodeForRegion(simCountry)
    } catch (e: Exception) {
        false
    }

}

fun normalizeToE164(context: Context, number: String): String {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val simCountry = tm.simCountryIso?.uppercase(Locale.ROOT)

    return try {
        val parsed = phoneUtil.parse(number, simCountry)
        phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
    } catch (e: Exception) {
        number
    }
}



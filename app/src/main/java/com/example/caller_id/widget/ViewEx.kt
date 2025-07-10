package com.example.caller_id.widget


import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat


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





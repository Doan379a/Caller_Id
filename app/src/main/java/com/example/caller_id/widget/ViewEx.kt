package com.example.caller_id.widget


import android.view.View


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





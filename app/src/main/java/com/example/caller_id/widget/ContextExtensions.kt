package com.example.caller_id.widget

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import java.text.Normalizer

/* ---------------------------------------------- Resources ---------------------------------------------- */
fun Any?.getTagDebug(keyCheck: String="DOAN_1"): String {
    val className = this?.javaClass?.simpleName ?: "UnknownClass"
    return "$className - $keyCheck"
}
fun Any?.getLogDebug(keyCheck: String = "DOAN_1",message: String ) {
    val tag = this?.javaClass?.simpleName ?: "UnknownClass"
    android.util.Log.d("$tag - $keyCheck", message)
}

fun Context?.getResString(@StringRes stringId: Int): String {
    return this?.resources?.getString(stringId) ?: ""
}

fun Context?.getDrawableResource(@DrawableRes drawableId: Int): Drawable? {
    this?.let {
        return ContextCompat.getDrawable(it, drawableId)
    } ?: run {
        return null
    }
}

/* ---------------------------------------------- Toast ---------------------------------------------- */

fun Context?.showToast(message: String) {
    (this as? Activity)?.runOnUiThread {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

fun Context?.showToast(@StringRes stringId: Int) {
    val message = getResString(stringId)
    showToast(message)
}

//fun Context?.debugToast(message: String) {
//    if (BuildConfig.DEBUG) {
//        showToast(message)
//    }
//}

/* ---------------------------------------------- SnackBar ---------------------------------------------- */

fun Context?.showSnackBar(message: String) {
    (this as? Activity)?.runOnUiThread {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}

fun Context.spToPx(sp: Float): Float {
    return sp * resources.displayMetrics.scaledDensity
}

fun Context.pxToSp(px: Float): Float {
    return px / resources.displayMetrics.scaledDensity
}

fun String.normalize(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return Regex("\\p{InCombiningDiacriticalMarks}+")
        .replace(temp, "")
}

fun Bitmap.toRoundedBitmap(radiusDp: Float, context: Context): Bitmap {
    val radiusPx = radiusDp * context.resources.displayMetrics.density

    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())

    canvas.drawRoundRect(rect, radiusPx, radiusPx, paint)

    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, 0f, 0f, paint)

    return output
}


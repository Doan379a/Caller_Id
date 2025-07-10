package com.example.caller_id.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import java.io.IOException

fun Context.getBitmapFromAsset(context: Context, strName: String): Bitmap? {
    val assetManager = context.assets
    return try {
        val istr = assetManager.open(strName)
        BitmapFactory.decodeStream(istr)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
fun Context.getTypefaceFromAsset(context: Context,fontPath: String): Typeface? {
    return if (fontPath == "Default") {
        Typeface.DEFAULT
    } else {
        Typeface.createFromAsset(context.assets, fontPath)
    }
}

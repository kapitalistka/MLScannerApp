package com.maslo.mlscanner

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log

private const val TAG = "BitmapExt"
fun Bitmap.cropCenter(size: Int)
/*(
    newWidth:Int = min(width,height),
    newHeight:Int = min(width,height)
)*/: Bitmap? {
    Log.d(TAG, "cropCenter")
    val newWidth: Int = size//min(width,height),
    val newHeight: Int = size//min(width,height)
    // calculate x and y offset
    val xOffset = (width - newWidth) / 2
    val yOffset = (height - newHeight) / 2

    return try {
        Bitmap.createBitmap(
            this, // source bitmap
            xOffset, // x coordinate of the first pixel in source
            yOffset, // y coordinate of the first pixel in source
            newWidth, // new width
            newHeight // new height
        )

    } catch (e: IllegalArgumentException) {
        null
    }
}


fun Bitmap.createSquaredBitmap(): Bitmap? {
    Log.d(TAG, "createSquaredBitmap")
    val dim = Math.max(this.width, this.height)
    val dstBmp = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(dstBmp)
    canvas.drawColor(Color.BLACK)
    canvas.drawBitmap(this, ((dim - this.width) / 2).toFloat(), ((dim - this.height) / 2).toFloat(), null)
    return dstBmp
}

fun Bitmap.createScaledBitmap(w: Int, h: Int, filter: Boolean): Bitmap {
    Log.d(TAG, "createScaledBitmap")
    return Bitmap.createScaledBitmap(this, w, h, filter)
}
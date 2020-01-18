package com.chaddysroom.vloggingapp.utils.draw_util

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView


class SurfaceViewDraw(surface: SurfaceView, context: Context){
    val holder = surface.holder

    fun drawBoundingBox(boundingBox: Rect){
        val canvas: Canvas = holder.lockCanvas()
        drawRect(canvas = canvas, rect = boundingBox)
        holder.unlockCanvasAndPost(canvas)
    }
}

fun drawRect(canvas:Canvas, rect: Rect){
    val paint = Paint()
    val width : Float = 0.toFloat()
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    paint.strokeWidth = width
    paint.color = Color.YELLOW
    paint.style = Paint.Style.STROKE
    canvas.drawRect(rect, paint)
    Log.i("DRAWRECT", "DRAWN")
}


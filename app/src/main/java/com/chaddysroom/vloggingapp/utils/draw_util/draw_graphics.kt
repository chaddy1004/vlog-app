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

    fun drawBoundingBox(boundingBox: RectF){
        val canvas: Canvas = holder.lockCanvas()
        drawRect(canvas = canvas, rect = boundingBox)
        holder.unlockCanvasAndPost(canvas)
    }
    fun clearScreen(){
        val canvas: Canvas = holder.lockCanvas()
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        holder.unlockCanvasAndPost(canvas)
    }
}

fun drawRect(canvas:Canvas, rect: RectF){
    val paint = Paint()
    val width : Float = 5f
//    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    paint.strokeWidth = width
    paint.color = Color.YELLOW
    paint.style = Paint.Style.STROKE
    canvas.drawRect(rect, paint)
    Log.i("DRAWRECT", "DRAWN")
}


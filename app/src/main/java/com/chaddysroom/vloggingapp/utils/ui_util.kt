package com.chaddysroom.vloggingapp.utils
import android.text.method.Touch.onTouchEvent
import android.view.MotionEvent
import android.support.v4.view.ViewCompat.animate
import android.R.attr.x
import android.R.attr.y
import android.content.Context
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.view.ViewGroup
import android.view.View.OnTouchListener
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.util.Log
import android.view.View


class MovableFloatingActionButton : FloatingActionButton, View.OnTouchListener {

    private var downRawX: Float = 0.toFloat()
    private var downRawY: Float = 0.toFloat()
    private var dX: Float = 0.toFloat()
    private var dY: Float = 0.toFloat()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setOnTouchListener(this)
    }


    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {


        val layoutParams = view.getLayoutParams() as ViewGroup.MarginLayoutParams
//        Log.i("FAB_top", layoutParams.topMargin.toString())
//        Log.i("FAB_bottom", layoutParams.bottomMargin.toString())
//        Log.i("FAB_left", layoutParams.leftMargin.toString())
//        Log.i("FAB_right", layoutParams.rightMargin.toString())


        val action = motionEvent.action
        if (action == MotionEvent.ACTION_DOWN) {

            downRawX = motionEvent.rawX
            downRawY = motionEvent.rawY
            dX = view.getX() - downRawX
            dY = view.getY() - downRawY

            return true // Consumed

        } else if (action == MotionEvent.ACTION_MOVE) {

            val viewWidth = view.getWidth()
            val viewHeight = view.getHeight()

            val viewParent = view.parent as View
            val parentWidth = viewParent.getWidth()
            val parentHeight = viewParent.getHeight()


            var newX = motionEvent.rawX + dX
            newX = Math.max(
                layoutParams.leftMargin.toFloat(),
                newX
            ) // Don't allow the FAB past the left hand side of the parent
            newX = Math.min(
                (parentWidth - viewWidth - layoutParams.rightMargin).toFloat(),
                newX
            ) // Don't allow the FAB past the right hand side of the parent

            var newY = motionEvent.rawY + dY
            Log.i("height", parentHeight.toString())
            Log.i("newY", newY.toString())
            Log.i("FAB_left", layoutParams.leftMargin.toString())
            Log.i("FAB_right", layoutParams.rightMargin.toString())
            newY = Math.max(layoutParams.topMargin.toFloat(), newY) // Don't allow the FAB past the top of the parent
            newY = Math.min(
                (parentHeight - viewHeight - layoutParams.bottomMargin).toFloat(),
                newY
            ) // Don't allow the FAB past the bottom of the parent

            view.animate()
                .x(newX)
                .y(newY)
                .setDuration(0)
                .start()

            return true // Consumed

        } else if (action == MotionEvent.ACTION_UP) {

            val upRawX = motionEvent.rawX
            val upRawY = motionEvent.rawY

            val upDX = upRawX - downRawX
            val upDY = upRawY - downRawY

            return if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                performClick()
            } else { // A drag
                true // Consumed
            }

        } else {
            return super.onTouchEvent(motionEvent)
        }

    }

    companion object {

        private val CLICK_DRAG_TOLERANCE =
            10f // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.
    }

}
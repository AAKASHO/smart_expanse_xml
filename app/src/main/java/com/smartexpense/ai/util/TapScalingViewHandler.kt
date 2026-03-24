package com.smartexpense.ai.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat

class TapScalingViewHandler:  GestureDetector.OnGestureListener, View.OnClickListener, GestureDetector.OnDoubleTapListener {

    private lateinit var gestureDetectorCompat: GestureDetectorCompat
    private lateinit var context: Context
    private lateinit var view: View

    private var x1 = 0f
    private var y1 = 0f
    private var x2 = 0f
    private var y2 = 0f
    private var dx = 0f
    private var dy = 0f
    private var scaleDownTo = 0.94f
    private var runClickListener = false

    private var longPressed = false

    fun initialize(context: Context, view: View, scaleDownTo: Float) {
        this.context = context
        this.view = view
        this.scaleDownTo = scaleDownTo
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun init() {

        view.setOnTouchListener { _: View, event: MotionEvent? ->

            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    x1 = event.x
                    y1 = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    checkIfThresholdMoved(event)
                }

                MotionEvent.ACTION_CANCEL -> {
                    handleCancel()
                }
                MotionEvent.ACTION_UP -> {
                    if (longPressed)
                        onLongPressConfirmed()
                }
            }
            event?.let {
                if (!::gestureDetectorCompat.isInitialized) {
                    gestureDetectorCompat = GestureDetectorCompat(context, this)
                    gestureDetectorCompat.setOnDoubleTapListener(this)
                }
                gestureDetectorCompat.onTouchEvent(it)
            }
            true
        }
    }

    override fun onClick(v: View?) {

    }

    private fun onLongPressConfirmed() {
        runClickListener = true
        scaleOriginal()
    }


    private fun handleCancel() {
        scaleOriginal()
    }

    private fun checkIfThresholdMoved(event: MotionEvent) {

        x2 = event.x
        y2 = event.y
        dx = x2 - x1
        dy = y2 - y1

        if (dx > MAX_CLICK_DISTANCE || dy > MAX_CLICK_DISTANCE)
            scaleOriginal()
    }

    override fun onDoubleTap(p0: MotionEvent): Boolean {
        return true
    }

    override fun onDoubleTapEvent(p0: MotionEvent): Boolean {
        return true
    }

    override fun onSingleTapConfirmed(p0: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent) {
    }

    override fun onDown(p0: MotionEvent): Boolean {
        longPressed = true
        runClickListener = false
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
        scaleDown()
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        scaleDown()
        longPressed = false
        runClickListener = true
        return true
    }


    private fun scaleOriginal() {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f)
        scaleUpX.duration = 50
        scaleUpY.duration = 50
        val scaleUp = AnimatorSet()
        scaleUp.play(scaleUpX).with(scaleUpY)
        scaleUp.addListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                if(runClickListener) {
                    view.performClick()
                    runClickListener = false
                }
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        scaleUp.start()
        longPressed = false
    }

    private fun scaleDown() {
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", scaleDownTo)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", scaleDownTo)
        scaleDownX.duration = 75
        scaleDownY.duration = 75
        val scaleDown = AnimatorSet()
        scaleDown.play(scaleDownX).with(scaleDownY)
        scaleDown.addListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                if(!longPressed) scaleOriginal()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        scaleDown.start()
    }

    companion object {
        private const val MAX_CLICK_DISTANCE = 5
    }
}
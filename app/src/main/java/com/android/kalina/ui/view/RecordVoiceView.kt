package com.android.kalina.ui.view

import android.content.Context
import android.graphics.Color
import android.os.Vibrator
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class RecordVoiceView(val context: Context,
                      private val recordGroup: View,
                      val timeTextView: TextView,
                      val cancelView: View,
                      private val actionView: View,
                      private val availableWidth: Float,
                      private val timeLimit: Long) {

    private var touchListener: MoveViewTouchListener
    private val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
    private var active = false
    private var cancel = false
    var recordListener: RecordVoiceListener? = null

    init {
        touchListener = createMoveViewTouchListener()
        actionView.setOnTouchListener(touchListener)
    }

    private fun createMoveViewTouchListener() = MoveViewTouchListener(actionView).apply {
        setEnableY(false)
        setAlertBound(0.3F)
        setMaxTranslationX(availableWidth)
        setListener(object : MoveViewTouchListener.ActionVoiceViewListener {
            override fun onStart() {
                active = true
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(50)
                expand()

                recordListener?.onRecordStart()
            }

            override fun onFinish() {
                active = false

                if (!cancel) {
                    collapse()
                    recordListener?.onRecordFinish()
                }

                this@RecordVoiceView.reset()
            }

            override fun onOutBound() {
                cancel = true
                recordListener?.onRecordCancel()
                collapse()
            }

            override fun onTranslateOffsetChanged(offset: Float, translate: Float) {
                val validTranslate = Math.max(translate, 0F)

//                cancelView.translationX = -validTranslate
                cancelView.alpha = offset
            }
        })
    }

    private fun reset() {
        cancel = false
        cancelView.clearAnimation()
        cancelView.translationX = 0F
        cancelView.alpha = 1f
        touchListener.reset()
        setTime(0)
    }

    fun setEnable(enable: Boolean) {
        if (enable) {
            actionView.setOnTouchListener(touchListener)
        } else {
            actionView.setOnTouchListener(null)
        }
    }

    fun setTime(milliseconds: Long) {
        timeTextView.text = timeFormat.format(Date(milliseconds))

        if (timeLimit < milliseconds) {
            collapse()
            cancel = true
            recordListener?.onRecordFinish()
        }
    }

    private fun expand() {
        recordGroup.visibility = View.VISIBLE
        actionView.bringToFront()
        timeTextView.bringToFront()
//        actionView.setBackgroundDrawable(ContextCompat.getDrawable(context, getBackgroundResourceId(false)))
//
//        micView.setImageDrawable(VectorDrawableCompat.create(context.resources, getActionViewResourceId(true, false), context.theme))

        val anim = ScaleAnimation(1F, 2F, 1F, 2F,
                actionView.measuredWidth.toFloat() / 2, actionView.measuredHeight.toFloat() / 2)

        anim.duration = 250
        anim.interpolator = LinearInterpolator()
        anim.fillAfter = true
        anim.isFillEnabled = true
        actionView.startAnimation(anim)
        touchListener.setScaleFactor(2f)
    }

    private fun collapse() {
        resetMoveViewListener()

        recordGroup.visibility = View.GONE
        actionView.animation?.cancel()
        actionView.bringToFront()
        actionView.clearFocus()
//        micView.setImageDrawable(VectorDrawableCompat.create(context.resources, getActionViewResourceId(false, false), context.theme))
        actionView.setBackgroundColor(Color.TRANSPARENT)

        actionView.translationX = 0F
        actionView.translationY = 0F
    }

    private fun resetMoveViewListener() {
        touchListener.destroy()
        touchListener = createMoveViewTouchListener()
        actionView.setOnTouchListener(touchListener)
    }

    interface RecordVoiceListener {
        fun onRecordStart()

        fun onRecordFinish()

        fun onRecordCancel()
    }
}
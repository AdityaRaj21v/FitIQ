package com.example.fitiq

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CircularProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress = 0
    private val strokeWidth = 12f
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@CircularProgressView.strokeWidth
        color = Color.LTGRAY
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@CircularProgressView.strokeWidth
        color = Color.BLACK
        strokeCap = Paint.Cap.ROUND
    }

    fun setProgress(value: Int) {
        progress = value.coerceIn(0, 100)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val diameter = Math.min(width, height) - strokeWidth
        val left = (width - diameter) / 2
        val top = (height - diameter) / 2
        val rect = RectF(left, top, left + diameter, top + diameter)

        canvas.drawArc(rect, 0f, 360f, false, backgroundPaint)
        canvas.drawArc(rect, -90f, (progress * 360f) / 100f, false, progressPaint)
    }
}

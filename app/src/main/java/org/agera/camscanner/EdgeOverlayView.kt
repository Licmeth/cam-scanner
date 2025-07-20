package org.agera.camscanner

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class EdgeOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    var points: List<Point>? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        points?.let {
            val paint = Paint().apply {
                color = Color.GREEN
                strokeWidth = 5f
                style = Paint.Style.STROKE
            }
            if (it.size == 4) {
                val path = Path().apply {
                    moveTo(it[0].x.toFloat(), it[0].y.toFloat())
                    for (i in 1..3) lineTo(it[i].x.toFloat(), it[i].y.toFloat())
                    close()
                }
                canvas.drawPath(path, paint)
            }
        }
    }
}
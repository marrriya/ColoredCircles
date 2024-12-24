package com.example.coloredcircles

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt
import kotlin.random.Random

data class Circle(var x: Float, var y: Float, var radius: Float, var color: Int, var isDragging: Boolean = false) {
    fun contains(touchX: Float, touchY: Float): Boolean {
        val dx = touchX - x
        val dy = touchY - y
        return sqrt(dx * dx + dy * dy) <= radius
    }
}

class GameView(ctx: Context) : View(ctx) {
    var h = 1000
    var w = 1000
    val paint = Paint()
    val random = Random(System.currentTimeMillis())
    val circleRadius = 50f
    var circles: MutableList<Circle> = mutableListOf()
    var targetCircleIndex: Int = 0
    var draggingCircle: Circle? = null
    var gameOver = false

    init {
        generateCircles(5)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        h = bottom - top
        w = right - left
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.LTGRAY)
        paint.style = Paint.Style.FILL
        circles.forEach { circle ->
            paint.color = circle.color
            canvas.drawCircle(circle.x, circle.y, circleRadius, paint)
        }

        // Рисуем "лузу" во всю ширину и 100px высоту
        val targetRectY = h - 100f // Y координата верха прямоугольника
        paint.color = circles[targetCircleIndex].color
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, targetRectY, w.toFloat(), targetRectY + 100f, paint)

        if (gameOver) {
            paint.color = Color.RED
            paint.textSize = 50f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Game Over!", w / 2f, h / 2f, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val touchX = it.x
            val touchY = it.y
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    circles.forEachIndexed { index, circle ->
                        if (circle.contains(touchX, touchY)) {
                            draggingCircle = circle
                            draggingCircle?.isDragging = true
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    draggingCircle?.let { circle ->
                        circle.x = touchX
                        circle.y = touchY
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    draggingCircle?.let { circle ->
                        if (isInsideTarget(circle)) {
                            circles.remove(circle)
                            if (circles.isEmpty()) {
                                gameOver = true
                            } else {
                                targetCircleIndex = (targetCircleIndex + 1) % circles.size
                            }
                        }
                        draggingCircle?.isDragging = false
                        draggingCircle = null
                        invalidate()
                    }
                }

                else -> {}
            }
        }
        return true
    }

    private fun isInsideTarget(circle: Circle): Boolean {
        val targetRectY = h - 100f
        return circle.x >= 0 && circle.x <= w && circle.y >= targetRectY && circle.y <= targetRectY + 100
    }


    private fun generateCircles(numCircles: Int) {
        circles.clear()
        for (i in 1..numCircles) {
            var x: Float
            var y: Float
            do {
                x = random.nextFloat() * (w - 2 * circleRadius) + circleRadius
                y = random.nextFloat() * (h - 100 - 2 * circleRadius) + circleRadius // Изменено, чтобы кружки не перекрывали "лузу"
            } while (circles.any { sqrt((x - it.x) * (x - it.x) + (y - it.y) * (y - it.y)) < 2 * circleRadius })
            circles.add(Circle(x, y, circleRadius, Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))))
        }
    }
}
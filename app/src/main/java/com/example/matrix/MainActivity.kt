package com.example.matrix

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.atan2
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private var imageView: CustomImageView? = null
    private var textView: CustomTextView? = null
    private val matrix: Matrix = Matrix()
    private val savedMatrix: Matrix = Matrix()
    private var startX = 0f
    private var startY = 0f
    private var mOldDistance = 0f
    private var mOldRotation = 0f
    private var mMidPoint: PointF? = null
    private var mCurrentMode = -1


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.bt_visible_text_view).setOnClickListener {
            imageView!!.visibility = View.GONE
            textView!!.visibility = View.VISIBLE
            savedMatrix.set(textView!!.getMatrixView())
            matrix.set(savedMatrix)
        }
        findViewById<Button>(R.id.bt_visible_image_view).setOnClickListener {
            imageView!!.visibility = View.VISIBLE
            textView!!.visibility = View.GONE
            savedMatrix.set(imageView!!.getMatrixView())
            matrix.set(savedMatrix)
        }
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        setOnTouchEvent(imageView!!)
        setOnTouchEvent(textView!!)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouchEvent(view: View) {
        view.setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    savedMatrix.set(getMatrixView(view))
                    startX = event.x
                    startY = event.y
                    mCurrentMode = ActionModeTouch.DRAG
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (event.pointerCount >= 2) {
                        val distance = calculateDistance(event)
                        if (distance > 10f) {
                            savedMatrix.set(getMatrixView(view))
                            mOldDistance = distance
                            mMidPoint = calculateMidPoint(event)
                            mOldRotation = calculateRotation(event)
                            mCurrentMode = ActionModeTouch.ZOOM
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    when (mCurrentMode) {
                        ActionModeTouch.ZOOM -> {
                            val newDistance = calculateDistance(event)
                            if (newDistance > 10f && mMidPoint != null) {
                                val scale = newDistance / mOldDistance
                                matrix.set(savedMatrix)
                                matrix.postScale(scale, scale, mMidPoint!!.x, mMidPoint!!.y)

                                val newRotation = calculateRotation(event)
                                val deltaRotation = newRotation - mOldRotation
                                matrix.postRotate(deltaRotation, mMidPoint!!.x, mMidPoint!!.y)
                            }
                        }

                        ActionModeTouch.DRAG -> {
                            val dx = event.x - startX
                            val dy = event.y - startY
                            matrix.set(savedMatrix)
                            matrix.postTranslate(dx, dy)
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mCurrentMode = ActionModeTouch.NONE
                }
            }
            getMatrixView(view).set(matrix)
            view.invalidate()
            true
        }
    }

    private fun getMatrixView(view: View): Matrix {
        return if (view is CustomImageView)
            view.getMatrixView()
        else (view as CustomTextView).getMatrixView()
    }

    private fun calculateDistance(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun calculateMidPoint(event: MotionEvent): PointF {
        val x = (event.getX(0) + event.getX(1)) / 2
        val y = (event.getY(0) + event.getY(1)) / 2
        return PointF(x, y)
    }

    private fun calculateRotation(event: MotionEvent): Float {
        val deltaX = event.getX(0) - event.getX(1)
        val deltaY = event.getY(0) - event.getY(1)
        return Math.toDegrees(atan2(deltaY.toDouble(), deltaX.toDouble())).toFloat()
    }
}
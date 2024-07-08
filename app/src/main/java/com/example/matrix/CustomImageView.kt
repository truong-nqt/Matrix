package com.example.matrix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CustomImageView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {

    private var matrix: Matrix? = null

    init {
        matrix = Matrix()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.concat(matrix)
        super.onDraw(canvas)
    }

    fun getMatrixView(): Matrix {
        return matrix!!
    }
}
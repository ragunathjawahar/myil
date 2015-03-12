/*
 * Copyright (C) 2015 Mobs & Geeks
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobsandgeeks.myil.dashboard

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Paint
import android.animation.ValueAnimator
import com.mobsandgeeks.myil.R
import android.content.res.TypedArray
import android.graphics.Paint.Cap

/**
 * @author Ragunath Jawahar {@literal <rj@mobsandgeeks.com>}
 */
public class CircularProgressBar(context: Context, attrs: AttributeSet?)
        : View(context, attrs) {

    // Constants
    private val STROKE_WIDTH_FRACTION = 0.075f
    private val COLOR_PROGRESS_BAR: Int = 0xff6a8afe.toInt()
    private val COLOR_PROGRESS_BAR_BACKGROUND: Int = 0xffababab.toInt()
    private val ANIMATION_DURATION = 600L
    private val DEFAULT_MAX = 100

    private val EDGE_FLAT = 0
    private val EDGE_ROUNDED = 1

    // Metrics
    private var strokeWidth = 0.0f
    private val progressBarRectF = RectF()

    // Properties
    private var progressAngle = 0f

    // Graphics
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Animation
    private val progressAnimator = ValueAnimator.ofFloat()

    // Public properties
    public var progressBarColor: Int = COLOR_PROGRESS_BAR
        set(value) {
            $progressBarColor = value
            invalidate()
        }

    public var progressBarBackgroundColor: Int = COLOR_PROGRESS_BAR_BACKGROUND
        set(value) {
            $progressBarBackgroundColor = value
            invalidate()
        }

    public var edges: Edge = Edge.FLAT
        set(value) {
            $edges = value
            invalidate()
        }

    public var value: Float = 0f
        set(value) {
            $value = value
            animateProgressBar()
        }

    public var max: Int = DEFAULT_MAX
        set(value) {
            $max = value
            animateProgressBar()
        }

    // Initializer
    {
        obtainXmlAttributes(context, attrs)
        initProgressAnimator()
    }

    override fun onDraw(canvas: Canvas) {
        paint.setStrokeWidth(strokeWidth)
        paint.setStyle(Paint.Style.STROKE)
        paint.setStrokeCap(if (Edge.FLAT == edges) Cap.BUTT else Cap.ROUND)

        // Progress bar background
        paint.setColor(progressBarBackgroundColor)
        canvas.drawOval(progressBarRectF, paint)

        // Progress bar
        paint.setColor(progressBarColor)
        canvas.drawArc(progressBarRectF, 0f, progressAngle, false, paint)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        // Progress bar diameter
        val diameter = Math.min(width, height)
        strokeWidth = diameter * STROKE_WIDTH_FRACTION

        // View Center
        val centerX = width / 2
        val centerY = height / 2

        // Progress bar bounds
        val squareSide = diameter - strokeWidth
        val halfSquareSide = squareSide / 2
        progressBarRectF.left = centerX - halfSquareSide
        progressBarRectF.top = centerY - halfSquareSide
        progressBarRectF.right = centerX + halfSquareSide
        progressBarRectF.bottom = centerY + halfSquareSide
    }

    private fun obtainXmlAttributes(context: Context, attrs: AttributeSet?) {
        var typedArray: TypedArray = context.obtainStyledAttributes(
                attrs, R.styleable.CircularProgressBar)

        try {
            if (typedArray.hasValue(R.styleable.CircularProgressBar_progressBarColor)) {
                progressBarColor = typedArray.getColor(
                        R.styleable.CircularProgressBar_progressBarColor,
                        COLOR_PROGRESS_BAR)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_progressBarBackgroundColor)) {
                progressBarBackgroundColor = typedArray.getColor(
                        R.styleable.CircularProgressBar_progressBarBackgroundColor,
                        COLOR_PROGRESS_BAR_BACKGROUND)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_edges)) {
                val xmlEdges = typedArray.getInt(R.styleable.CircularProgressBar_edges, -1)
                edges = if (xmlEdges == -1 || xmlEdges == EDGE_FLAT) Edge.FLAT else Edge.ROUNDED
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_max)) {
                max = typedArray.getInt(R.styleable.CircularProgressBar_max, DEFAULT_MAX)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_value)) {
                value = typedArray.getFloat(R.styleable.CircularProgressBar_value, 0f)
            }
        } finally {
            typedArray.recycle()
        }
    }

    private fun initProgressAnimator() {
        progressAnimator.setDuration(ANIMATION_DURATION)
        progressAnimator.addUpdateListener({ animation ->
            progressAngle = animation.getAnimatedValue() as Float
            invalidate()
        })
    }

    private fun animateProgressBar() {
        val newProgressAngle = value / max * 360

        // Cancel ongoing animation
        if (progressAnimator.isRunning()) {
            progressAnimator.cancel()
        }

        // Start a new animation
        progressAnimator.setFloatValues(progressAngle, newProgressAngle)
        progressAnimator.start()
    }

    /**
     * Enumeration to specify the style of the Progress Bar edges
     */
    enum class Edge {
        FLAT
        ROUNDED
    }
}

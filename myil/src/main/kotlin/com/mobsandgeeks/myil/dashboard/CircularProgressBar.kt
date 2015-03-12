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
    private val STROKE_FRACTION_BAR = 0.075f
    private val STROKE_FRACTION_BAR_BACKGROUND = 1.0f
    private val COLOR_BAR = 0xff6a8afe.toInt()
    private val COLOR_BAR_BACKGROUND = 0xffababab.toInt()
    private val ANIMATION_DURATION = 600L
    private val MAX_DEFAULT = 100

    private val EDGE_FLAT = 0
    private val EDGE_ROUNDED = 1

    // Metrics
    private var barStrokeWidth = 0.0f
    private val barRectF = RectF()

    // Properties
    private var barProgressAngle = 0f

    // Graphics
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Animation
    private val progressAnimator = ValueAnimator.ofFloat()

    // Public properties
    public var barColor: Int = COLOR_BAR
        set(value) {
            $barColor = value
            invalidate()
        }

    public var barBackgroundColor: Int = COLOR_BAR_BACKGROUND
        set(value) {
            $barBackgroundColor = value
            invalidate()
        }

    public var barBackgroundStrokeFraction: Float = STROKE_FRACTION_BAR_BACKGROUND
        set(value) {
            assertProgressBarWidth(value)
            $barBackgroundStrokeFraction = value
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

    public var max: Int = MAX_DEFAULT
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
        paint.setStyle(Paint.Style.STROKE)
        paint.setStrokeCap(if (Edge.FLAT == edges) Cap.BUTT else Cap.ROUND)

        // Progress bar background
        paint.setStrokeWidth(barStrokeWidth * barBackgroundStrokeFraction)
        paint.setColor(barBackgroundColor)
        canvas.drawOval(barRectF, paint)

        // Progress bar
        paint.setStrokeWidth(barStrokeWidth)
        paint.setColor(barColor)
        canvas.drawArc(barRectF, 0f, barProgressAngle, false, paint)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        // Progress bar diameter
        val diameter = Math.min(width, height)
        barStrokeWidth = diameter * STROKE_FRACTION_BAR

        // View Center
        val centerX = width / 2
        val centerY = height / 2

        // Progress bar bounds
        val squareSide = diameter - barStrokeWidth
        val halfSquareSide = squareSide / 2
        barRectF.left = centerX - halfSquareSide
        barRectF.top = centerY - halfSquareSide
        barRectF.right = centerX + halfSquareSide
        barRectF.bottom = centerY + halfSquareSide
    }

    private fun obtainXmlAttributes(context: Context, attrs: AttributeSet?) {
        var typedArray: TypedArray = context.obtainStyledAttributes(
                attrs, R.styleable.CircularProgressBar)

        try {
            if (typedArray.hasValue(R.styleable.CircularProgressBar_barColor)) {
                barColor = typedArray.getColor(
                        R.styleable.CircularProgressBar_barColor,
                        COLOR_BAR)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_barBackgroundColor)) {
                barBackgroundColor = typedArray.getColor(
                        R.styleable.CircularProgressBar_barBackgroundColor,
                        COLOR_BAR_BACKGROUND)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_barBackgroundStrokeFraction)) {
                barBackgroundStrokeFraction = typedArray.getFloat(
                        R.styleable.CircularProgressBar_barBackgroundStrokeFraction,
                        STROKE_FRACTION_BAR_BACKGROUND)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_edges)) {
                val xmlEdges = typedArray.getInt(R.styleable.CircularProgressBar_edges, -1)
                edges = if (xmlEdges == -1 || xmlEdges == EDGE_FLAT) Edge.FLAT else Edge.ROUNDED
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_max)) {
                max = typedArray.getInt(R.styleable.CircularProgressBar_max, MAX_DEFAULT)
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
            barProgressAngle = animation.getAnimatedValue() as Float
            invalidate()
        })
    }

    private fun assertProgressBarWidth(value: Float) {
        if (value < 0 || value > 1) {
            throw IllegalArgumentException("'barBackgroundWidth' should be "
                    + "a float between 0.0 and 1.0, your's was ${value}")
        }
    }

    private fun animateProgressBar() {
        val newProgressAngle = value / max * 360

        // Cancel ongoing animation
        if (progressAnimator.isRunning()) {
            progressAnimator.cancel()
        }

        // Start a new animation
        progressAnimator.setFloatValues(barProgressAngle, newProgressAngle)
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

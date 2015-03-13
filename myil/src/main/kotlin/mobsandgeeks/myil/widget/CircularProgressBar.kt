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

package mobsandgeeks.myil.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.Canvas
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Rect
import android.graphics.Paint
import android.os.Handler
import android.animation.ValueAnimator
import com.mobsandgeeks.myil.R
import mobsandgeeks.myil.extensions.format

/**
 * @author Ragunath Jawahar {@literal <rj@mobsandgeeks.com>}
 */
public class CircularProgressBar(context: Context, attrs: AttributeSet?)
        : View(context, attrs) {

    // Constants
    private val STROKE_FRACTION_BAR_DEFAULT = 0.075f
    private val STROKE_FRACTION_BAR_MIN = 0.01f
    private val STROKE_FRACTION_BAR_MAX = 0.20f

    private val STROKE_FRACTION_BAR_BACKGROUND_DEFAULT = 1.0f
    private val STROKE_FRACTION_BAR_BACKGROUND_MIN = 0.0f
    private val STROKE_FRACTION_BAR_BACKGROUND_MAX = 1.0f

    private val TEXT_SIZE_FRACTION = 0.28f

    private val COLOR_BAR = 0xff6a8afe.toInt()
    private val COLOR_BAR_BACKGROUND = 0xffababab.toInt()
    private val COLOR_TEXT = Color.BLACK

    private val MAX_DEFAULT = 100.0f

    private val SHOW_TEXT = true

    private val ANIMATION_DURATION = 750L
    private val ANIMATION_FIRST_DELAY = 100L

    // Enumeration constants
    private val EDGE_FLAT = 0
    private val EDGE_ROUNDED = 1

    private val DISPLAY_TEXT_PERCENT = 0
    private val DISPLAY_TEXT_PROGRESS = 1

    private val NUMBER_FORMAT_INTEGER = 0
    private val NUMBER_FORMAT_DECIMAL = 1

    // Metrics
    private var barStrokeWidth = 0.0f
    private val barRectF = RectF()
    private val textBoundsRect = Rect()
    private var textSize = 0.0f

    // Properties
    private var barProgressAngle = 0.0f

    // Graphics
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Animation
    private val animationHandler = Handler()
    private val progressAnimator = ValueAnimator.ofFloat()

    // Public properties
    var barColor: Int = COLOR_BAR
        set(value) {
            $barColor = value
            invalidate()
        }

    var barBackgroundColor: Int = COLOR_BAR_BACKGROUND
        set(value) {
            $barBackgroundColor = value
            invalidate()
        }

    var textColor: Int = COLOR_TEXT
        set(value) {
            $textColor = value
            invalidate()
        }

    var showText: Boolean = SHOW_TEXT
        set(value) {
            $showText = value
            invalidate()
        }

    var barStrokeFraction: Float = STROKE_FRACTION_BAR_DEFAULT
        set(value) {
            assertBarStrokeFraction(value)
            $barStrokeFraction = value
            barStrokeWidth = getDiameter() * value
            invalidate()
        }

    var barBackgroundStrokeFraction: Float = STROKE_FRACTION_BAR_BACKGROUND_DEFAULT
        set(value) {
            assertBarBackgroundStrokeFraction(value)
            $barBackgroundStrokeFraction = value
            invalidate()
        }

    var edges: Edge = Edge.ROUNDED
        set(value) {
            $edges = value
            invalidate()
        }

    var displayTextAs: DisplayText = DisplayText.PERCENT
        set(value) {
            $displayTextAs = value
            invalidate()
        }

    var numberFormat: NumberFormat = NumberFormat.INTEGER
        set(value) {
            $numberFormat = value
            invalidate()
        }

    var progress: Float = 0.0f
        set(value) {
            $progress = value
            animateProgressBar()
        }

    var max: Float = MAX_DEFAULT
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
        paint.setStrokeCap(if (Edge.FLAT == edges) Paint.Cap.BUTT else Paint.Cap.ROUND)

        // Progress bar background
        paint.setStrokeWidth(barStrokeWidth * barBackgroundStrokeFraction)
        paint.setColor(barBackgroundColor)
        canvas.drawOval(barRectF, paint)

        // Progress bar
        paint.setStrokeWidth(barStrokeWidth)
        paint.setColor(barColor)
        canvas.drawArc(barRectF, 0.0f, barProgressAngle, false, paint)

        // Text
        if (showText) {
            val isPercent = DisplayText.PERCENT.equals(displayTextAs)
            val number = if (isPercent) (barProgressAngle / 360 * 100)
                    else (barProgressAngle * max / 360)
            val displayNumber = if (NumberFormat.INTEGER.equals(numberFormat))
                    number.toInt() else number.format(1)
            val text = if (isPercent) "${displayNumber}%" else "${displayNumber}"

            paint.setStyle(Paint.Style.FILL)
            paint.setTextSize(textSize)
            paint.setColor(textColor)

            paint.getTextBounds(text, 0, text.length(), textBoundsRect)
            val textWidth = paint.measureText(text, 0, text.length())
            val centerX = barRectF.centerX() - textWidth / 2
            val centerY = barRectF.centerY() + textBoundsRect.height() / 2

            canvas.drawText(text, 0, text.length(), centerX, centerY, paint)
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        // Progress bar diameter
        val diameter = Math.min(width, height)
        barStrokeWidth = diameter * barStrokeFraction

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

        // Text
        textSize = diameter * TEXT_SIZE_FRACTION
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) {
            animateProgressBar(ANIMATION_FIRST_DELAY)
        }
    }

    private fun obtainXmlAttributes(context: Context, attrs: AttributeSet?) {
        var typedArray: TypedArray = context.obtainStyledAttributes(
                attrs, R.styleable.CircularProgressBar)

        try {
            if (typedArray.hasValue(R.styleable.CircularProgressBar_barColor)) {
                $barColor = typedArray.getColor(
                        R.styleable.CircularProgressBar_barColor,
                        COLOR_BAR)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_barBackgroundColor)) {
                $barBackgroundColor = typedArray.getColor(
                        R.styleable.CircularProgressBar_barBackgroundColor,
                        COLOR_BAR_BACKGROUND)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_android_textColor)) {
                $textColor = typedArray.getColor(
                        R.styleable.CircularProgressBar_android_textColor,
                        COLOR_TEXT)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_barStrokeFraction)) {
                $barStrokeFraction = typedArray.getFloat(
                        R.styleable.CircularProgressBar_barStrokeFraction,
                        STROKE_FRACTION_BAR_DEFAULT)
                assertBarStrokeFraction(barStrokeFraction)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_barBackgroundStrokeFraction)) {
                $barBackgroundStrokeFraction = typedArray.getFloat(
                        R.styleable.CircularProgressBar_barBackgroundStrokeFraction,
                        STROKE_FRACTION_BAR_BACKGROUND_DEFAULT)
                assertBarBackgroundStrokeFraction(barBackgroundStrokeFraction)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_edges)) {
                val xmlEdges = typedArray.getInt(R.styleable.CircularProgressBar_edges, -1)
                $edges = if (xmlEdges == -1 || xmlEdges == EDGE_FLAT) Edge.FLAT else Edge.ROUNDED
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_max)) {
                $max = typedArray.getFloat(R.styleable.CircularProgressBar_max, MAX_DEFAULT)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_progress)) {
                $progress = typedArray.getFloat(R.styleable.CircularProgressBar_progress, 0.0f)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_showText)) {
                $showText = typedArray.getBoolean(
                        R.styleable.CircularProgressBar_showText, SHOW_TEXT)
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_displayTextAs)) {
                val xmlDisplayTextAs = typedArray.getInt(
                        R.styleable.CircularProgressBar_displayTextAs, -1)
                val isPercent = xmlDisplayTextAs == -1 || xmlDisplayTextAs == DISPLAY_TEXT_PERCENT
                $displayTextAs = if (isPercent) DisplayText.PERCENT else DisplayText.PROGRESS
            }
            if (typedArray.hasValue(R.styleable.CircularProgressBar_numberFormat)) {
                val xmlNumberFormat = typedArray.getInt(
                        R.styleable.CircularProgressBar_numberFormat, -1)
                val isInteger = xmlNumberFormat == -1 || xmlNumberFormat == NUMBER_FORMAT_INTEGER
                $numberFormat = if (isInteger) NumberFormat.INTEGER else NumberFormat.DECIMAL
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

    private fun assertBarStrokeFraction(value: Float) {
        assertRange("barStrokeFraction", STROKE_FRACTION_BAR_MIN,
                STROKE_FRACTION_BAR_MAX, value)
    }

    private fun assertBarBackgroundStrokeFraction(value: Float) {
        assertRange("barBackgroundStrokeFraction", STROKE_FRACTION_BAR_BACKGROUND_MIN,
                STROKE_FRACTION_BAR_BACKGROUND_MAX, value)
    }

    private fun assertRange(propertyName: String, minValue: Float, maxValue: Float, value: Float) {
        if (value < minValue || value > maxValue) {
            throw IllegalArgumentException("'${propertyName}' should be "
                    + "a float between ${minValue} and ${maxValue}, but was ${value}")
        }
    }

    private fun getDiameter(): Int {
        return Math.min(getWidth(), getHeight())
    }

    private fun animateProgressBar(delay: Long = 0) {
        // Cancel ongoing animation
        if (progressAnimator.isRunning()) {
            progressAnimator.cancel()
        }

        animationHandler.removeCallbacksAndMessages(null)
        animationHandler.postDelayed({() ->
            val newProgressAngle = progress / max * 360

            // Start a new animation
            progressAnimator.setFloatValues(barProgressAngle, newProgressAngle)
            progressAnimator.start()
        }, delay)
    }

    /**
     * Style for the progress bar edges.
     */
    enum class Edge {
        FLAT
        ROUNDED
    }

    /**
     * Text display modes.
     */
    enum class DisplayText {
        PERCENT
        PROGRESS
    }

    /**
     * Number format used for displaying progress / percentage.
     */
    enum class NumberFormat {
        INTEGER
        DECIMAL
    }
}

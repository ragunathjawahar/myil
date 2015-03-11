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

/**
 * @author Ragunath Jawahar {@literal <rj@mobsandgeeks.com>}
 */
public class CircularProgressBar(context: Context, attrs: AttributeSet?)
        : View(context, attrs) {

    // Constants
    val STROKE_WIDTH_FRACTION = 0.075f
    val COLOR_DEFAULT_PROGRESS_BAR_BG: Int = 0xffababab.toInt()
    val COLOR_DEFAULT_PROGRESS_BAR: Int = 0xff6a8afe.toInt()
    val MAX_VALUE = 100f;

    // Metrics
    var strokeWidth = 0.0f
    val progressBarRectF = RectF()

    // Graphics
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Properties
    public var value: Float = 0f
        set(value) {
            $value = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        paint.setStrokeWidth(strokeWidth)
        paint.setStyle(Paint.Style.STROKE)

        // Progress bar background
        paint.setColor(COLOR_DEFAULT_PROGRESS_BAR_BG)
        canvas.drawOval(progressBarRectF, paint)

        // Progress bar
        paint.setColor(COLOR_DEFAULT_PROGRESS_BAR)
        val progressAngle = value / MAX_VALUE * 360;
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
}

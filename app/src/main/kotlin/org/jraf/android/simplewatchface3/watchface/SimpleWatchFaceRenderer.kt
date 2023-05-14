/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2023-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:Suppress("NOTHING_TO_INLINE")

package org.jraf.android.simplewatchface3.watchface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.view.SurfaceHolder
import android.view.animation.OvershootInterpolator
import androidx.core.graphics.withSave
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import org.jraf.android.simplewatchface3.R
import org.jraf.android.simplewatchface3.util.drawableId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val INTERACTIVE_DRAW_MODE_UPDATE_DELAY_MILLIS = 16L

private const val HOUR_HAND_LENGTH_RATIO = .6F
private const val HOUR_HAND_WIDTH_RATIO = .02F

private const val MINUTE_HAND_LENGTH_RATIO = .9F
private const val MINUTE_HAND_WIDTH_RATIO = .01F

private const val SECOND_HAND_LENGTH_RATIO = .9F
private const val SECOND_HAND_WIDTH_RATIO = .006F

private const val SECOND_HAND_ANIMATION_TIME_FRACTION = .92F
private val SECOND_HAND_ANIMATION_INTERPOLATOR = OvershootInterpolator(2F)

private const val NUMBER_MAJOR_SIZE_RATIO = 1F / 8F
private const val NUMBER_MINOR_SIZE_RATIO = 1F / 12F


class SimpleWatchFaceRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    currentUserStyleRepository: CurrentUserStyleRepository,
    watchState: WatchState,
    private val complicationSlotsManager: ComplicationSlotsManager,
) : Renderer.CanvasRenderer2<SimpleWatchFaceRenderer.SharedAssets>(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    canvasType = CanvasType.HARDWARE,
    interactiveDrawModeUpdateDelayMillis = INTERACTIVE_DRAW_MODE_UPDATE_DELAY_MILLIS,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false
) {
    class SharedAssets : Renderer.SharedAssets {
        override fun onDestroy() {}
    }

    override suspend fun createSharedAssets() = SharedAssets()

    private val isInteractiveMode get() = renderParameters.drawMode == DrawMode.INTERACTIVE

    private val dialPaint = Paint().apply {
        isAntiAlias = true
        isSubpixelText = true // <- not sure about this one
        textAlign = Paint.Align.CENTER
        typeface = Typeface.createFromAsset(context.assets, "fonts/Oswald-Medium.ttf")
    }

    private val handPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        setShadowLayer(context.resources.getDimensionPixelSize(R.dimen.shadow_radius).toFloat(), 0F, 0F, Color.BLACK)
    }

    private val digitsMarginVertical = context.resources.getDimensionPixelSize(R.dimen.digits_margin_vertical).toFloat()

    private val textBounds = Rect()


    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets,
    ) {
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets,
    ) {
        drawBackground(canvas)
        drawDial(canvas)
        drawComplications(canvas, zonedDateTime)
        drawHands(canvas, zonedDateTime)
    }

    private inline fun drawBackground(canvas: Canvas) {
//        canvas.drawColor(Color.BLACK)
        canvas.drawColor(Color.DKGRAY)

    }

    private val numberHeights = mutableMapOf<Int, Int>().apply {
        for (numberIndex in 0..11) {
            dialPaint.textSize = screenBounds.width() * (if (numberIndex % 3 == 0) NUMBER_MAJOR_SIZE_RATIO else NUMBER_MINOR_SIZE_RATIO)
            val text = if (numberIndex == 0) "12" else numberIndex.toString()
            dialPaint.getTextBounds(text, 0, text.length, textBounds)
            put(numberIndex, textBounds.height())
        }
    }

    private fun drawDial(canvas: Canvas) {
        var dialRadius: Float = Float.MAX_VALUE
        for (numberIndex in 0..11) {
            val textHeight = numberHeights[numberIndex]!!
            dialRadius = min(dialRadius, centerX - textHeight / 2 - digitsMarginVertical)
        }

        for (numberIndex in 0..11) {
            val angle = 2.0 * PI / 12.0 * numberIndex
            val text = if (numberIndex == 0) "12" else numberIndex.toString()
            val textHeight = numberHeights[numberIndex]!!
            val cx = sin(angle) * dialRadius + centerX
            val cy = -cos(angle) * dialRadius + centerY
            dialPaint.color = Color.YELLOW
            dialPaint.textSize = screenBounds.width() * (if (numberIndex % 3 == 0) NUMBER_MAJOR_SIZE_RATIO else NUMBER_MINOR_SIZE_RATIO)
            canvas.drawText(
                text,
                cx.toFloat(),
                cy.toFloat() + textHeight / 2,
                dialPaint
            )
        }
    }

    private inline fun drawComplications(canvas: Canvas, zonedDateTime: ZonedDateTime) {
        for ((_, complicationSlot) in complicationSlotsManager.complicationSlots) {
            if (complicationSlot.enabled &&
                SimpleWatchFaceComplicationSlot.fromId(complicationSlot.id).shouldDraw(isInteractiveMode = isInteractiveMode)
            ) {
                val renderer = complicationSlot.renderer as CanvasComplicationDrawable
                val complicationType = complicationSlot.complicationData.value.type
                renderer.drawable =
                    ComplicationDrawable.getDrawable(
                        context = context,
                        id = complicationType.drawableId()
                    )!!
                        .apply {
                            activeStyle.borderColor = 0xFFFF0000.toInt()
                            activeStyle.rangedValuePrimaryColor = 0xFFFF0000.toInt()
                            activeStyle.rangedValueSecondaryColor = 0x4DFF0000
                        }
                complicationSlot.render(canvas, zonedDateTime, renderParameters)
            }
        }
    }

    private inline fun drawHands(canvas: Canvas, zonedDateTime: ZonedDateTime) {
        val hour = zonedDateTime.hour
        val minute = zonedDateTime.minute
        val second = zonedDateTime.second
        val secondFraction = zonedDateTime.nano / 1_000_000_000F

        val hourRotation = hour * 30F + (minute / 60F) * 30F
        val minuteRotation = minute * 6F + (second / 60F) * 6F
        val secondRotation = second * 6F +
                if (secondFraction > SECOND_HAND_ANIMATION_TIME_FRACTION) {
                    val animationFraction =
                        (secondFraction - SECOND_HAND_ANIMATION_TIME_FRACTION) * (1F / (1F - SECOND_HAND_ANIMATION_TIME_FRACTION))
                    SECOND_HAND_ANIMATION_INTERPOLATOR.getInterpolation(animationFraction)
                } else {
                    0F
                } * 6F

        canvas.withSave {
            // Hour
            handPaint.color = 0xFFFF0000.toInt()
            handPaint.strokeWidth = HOUR_HAND_WIDTH_RATIO * screenBounds.height()
            canvas.rotate(hourRotation, centerX, centerY)
            canvas.drawLine(
                centerX,
                centerY,
                centerX,
                centerY - HOUR_HAND_LENGTH_RATIO * (screenBounds.height() / 2),
                handPaint
            )

            // Minute
            handPaint.color = 0xFF00FF00.toInt()
            handPaint.strokeWidth = MINUTE_HAND_WIDTH_RATIO * screenBounds.height()
            canvas.rotate(minuteRotation - hourRotation, centerX, centerY)
            canvas.drawLine(
                centerX,
                centerY,
                centerX,
                centerY - MINUTE_HAND_LENGTH_RATIO * (screenBounds.height() / 2),
                handPaint
            )

            // Second
            if (isInteractiveMode) {
                handPaint.color = 0xFF0000FF.toInt()
                handPaint.strokeWidth = SECOND_HAND_WIDTH_RATIO * screenBounds.height()
                canvas.rotate(secondRotation - minuteRotation, centerX, centerY)
                canvas.drawLine(
                    centerX,
                    centerY + SECOND_HAND_LENGTH_RATIO * (screenBounds.height() / 20),
                    centerX,
                    centerY - SECOND_HAND_LENGTH_RATIO * (screenBounds.height() / 2),
                    handPaint
                )
            }
        }
    }
}

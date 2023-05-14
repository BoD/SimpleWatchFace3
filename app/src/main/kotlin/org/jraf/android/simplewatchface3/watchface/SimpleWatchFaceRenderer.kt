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

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.SurfaceHolder
import androidx.core.graphics.withSave
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import org.jraf.android.simplewatchface3.util.drawableId
import java.time.ZonedDateTime

private const val INTERACTIVE_DRAW_MODE_UPDATE_DELAY_MILLIS = 1000L

private const val HOUR_HAND_LENGTH_RATIO = .6F
private const val HOUR_HAND_WIDTH_RATIO = .02F

private const val MINUTE_HAND_LENGTH_RATIO = .9F
private const val MINUTE_HAND_WIDTH_RATIO = .01F

private const val SECOND_HAND_LENGTH_RATIO = .9F
private const val SECOND_HAND_WIDTH_RATIO = .006F

class SimpleWatchFaceRenderer(
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

    private val handPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

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
        drawComplications(canvas, zonedDateTime)
        drawHands(canvas, zonedDateTime)
    }

    private inline fun drawBackground(canvas: Canvas) {
        canvas.drawColor(0xFF202000.toInt())
    }

    private inline fun drawComplications(canvas: Canvas, zonedDateTime: ZonedDateTime) {
        for ((_, complicationSlot) in complicationSlotsManager.complicationSlots) {
            if (complicationSlot.enabled &&
                SimpleWatchFaceComplicationSlot.fromId(complicationSlot.id).shouldDraw(isInteractiveMode = isInteractiveMode)
            ) {
//                complicationSlot.complicationSlotBounds =

                val renderer = complicationSlot.renderer as CanvasComplicationDrawable
                val complicationType = complicationSlot.complicationData.value.type
                renderer.drawable =
                    ComplicationDrawable.getDrawable(
                        context = renderer.drawable.context!!,
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
        val second = zonedDateTime.second
        val minute = zonedDateTime.minute
        val hour = zonedDateTime.hour

        val secondRotation = second * 6F
        val minuteRotation = minute * 6F + (second / 60F) * 6F
        val hourRotation = hour * 30F + (minute / 60F) * 30F

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

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

package org.jraf.android.simplewatchface3.watchface

import android.graphics.RectF
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasComplicationFactory
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationExperimental
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import org.jraf.android.simplewatchface3.util.isBig
import org.jraf.android.simplewatchface3.util.isSmall

class SimpleWatchFaceService : WatchFaceService() {
    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository,
    ): WatchFace {
        return WatchFace(
            watchFaceType = WatchFaceType.ANALOG,
            renderer = SimpleWatchFaceRenderer(
                surfaceHolder = surfaceHolder,
                currentUserStyleRepository = currentUserStyleRepository,
                watchState = watchState,
                complicationSlotsManager = complicationSlotsManager,
            ),
        )
    }

    @OptIn(ComplicationExperimental::class)
    override fun createComplicationSlotsManager(currentUserStyleRepository: CurrentUserStyleRepository): ComplicationSlotsManager {
        val canvasComplicationFactory = CanvasComplicationFactory { watchState, invalidateCallback ->
            CanvasComplicationDrawable(
                drawable = ComplicationDrawable(this),
                watchState = watchState,
                invalidateCallback = invalidateCallback
            )
        }

        return ComplicationSlotsManager(
            complicationSlotCollection = setOf(
                ComplicationSlot.createRoundRectComplicationSlotBuilder(
                    id = 0,
                    canvasComplicationFactory = canvasComplicationFactory,
                    supportedTypes = listOf(
                        ComplicationType.RANGED_VALUE,
                        ComplicationType.LONG_TEXT,
                        ComplicationType.SHORT_TEXT,
                        ComplicationType.MONOCHROMATIC_IMAGE,
                        ComplicationType.LIST,
                    ),
                    defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                        systemDataSource = SystemDataSources.DATA_SOURCE_DAY_AND_DATE,
                        systemDataSourceDefaultType = ComplicationType.SHORT_TEXT
                    ),
                    bounds = ComplicationSlotBounds(
                        ComplicationType.values().associateWith {
                            when {
                                it.isSmall() -> RectF(
                                    .5f - .125F,
                                    .125F,
                                    .5f + .125F,
                                    .125F + .25F,
                                )

                                it.isBig() -> RectF(
                                    .125F,
                                    .125F,
                                    1F - .125F,
                                    .125F + .25F,
                                )

                                else -> RectF(
                                    0F,
                                    0F,
                                    0F,
                                    0F,
                                )
                            }
                        }
                    )
                )
                    .build()
            ),
            currentUserStyleRepository = currentUserStyleRepository,
        )
    }
}

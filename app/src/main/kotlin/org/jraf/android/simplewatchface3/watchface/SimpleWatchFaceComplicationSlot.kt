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
import androidx.annotation.StringRes
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationExperimental
import androidx.wear.watchface.complications.data.ComplicationType
import org.jraf.android.simplewatchface3.R
import org.jraf.android.simplewatchface3.util.isBig
import org.jraf.android.simplewatchface3.util.isSmall

private const val SMALL_COMPLICATION_DIAMETER = .33F

private const val HALF = .5F
private const val QUARTER = .25F

enum class SimpleWatchFaceComplicationSlot(
    val id: Int,
    @StringRes
    val nameResId: Int,
    val supportedTypes: List<ComplicationType>,
    val defaultDataSourcePolicy: DefaultComplicationDataSourcePolicy,
    val bounds: ComplicationSlotBounds,
    val isVisibleWhenNotInteractive: Boolean,
) {
    @OptIn(ComplicationExperimental::class)
    TOP(
        id = 0,
        nameResId = R.string.complicationSlot_top,
        supportedTypes = listOf(
            ComplicationType.RANGED_VALUE,
            ComplicationType.LONG_TEXT,
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
            ComplicationType.LIST,
        ),
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
            systemDataSource = SystemDataSources.DATA_SOURCE_DAY_AND_DATE,
            systemDataSourceDefaultType = ComplicationType.LONG_TEXT
        ),
        bounds = ComplicationSlotBounds(
            ComplicationType.values().associateWith {
                when {
                    it.isSmall() -> rectCenteredAt(
                        x = HALF,
                        y = QUARTER,
                        width = SMALL_COMPLICATION_DIAMETER,
                    )

                    it.isBig() -> rectCenteredAt(
                        x = HALF,
                        y = QUARTER,
                        width = 1F - QUARTER,
                        height = SMALL_COMPLICATION_DIAMETER,
                    )

                    else -> RectF(0F, 0F, 0F, 0F)
                }
            }
        ),
        isVisibleWhenNotInteractive = true,
    ),

    LEFT(
        id = 1,
        nameResId = R.string.complicationSlot_left,
        supportedTypes = listOf(
            ComplicationType.RANGED_VALUE,
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        ),
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
            systemDataSource = SystemDataSources.DATA_SOURCE_WATCH_BATTERY,
            systemDataSourceDefaultType = ComplicationType.RANGED_VALUE
        ),
        bounds = ComplicationSlotBounds(
            rectCenteredAt(
                x = QUARTER,
                y = HALF,
                width = SMALL_COMPLICATION_DIAMETER,
            )
        ),
        isVisibleWhenNotInteractive = false,
    ),

    RIGHT(
        id = 2,
        nameResId = R.string.complicationSlot_right,
        supportedTypes = listOf(
            ComplicationType.RANGED_VALUE,
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
        ),
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
            systemDataSource = SystemDataSources.DATA_SOURCE_STEP_COUNT,
            systemDataSourceDefaultType = ComplicationType.SHORT_TEXT
        ),
        bounds = ComplicationSlotBounds(
            rectCenteredAt(
                x = 1F - QUARTER,
                y = HALF,
                width = SMALL_COMPLICATION_DIAMETER,
            )
        ),
        isVisibleWhenNotInteractive = false,
    ),

    @OptIn(ComplicationExperimental::class)
    BOTTOM(
        id = 3,
        nameResId = R.string.complicationSlot_bottom,
        supportedTypes = listOf(
            ComplicationType.RANGED_VALUE,
            ComplicationType.LONG_TEXT,
            ComplicationType.SHORT_TEXT,
            ComplicationType.MONOCHROMATIC_IMAGE,
            ComplicationType.LIST,
        ),
        defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
            systemDataSource = SystemDataSources.DATA_SOURCE_NEXT_EVENT,
            systemDataSourceDefaultType = ComplicationType.LONG_TEXT
        ),
        bounds = ComplicationSlotBounds(
            ComplicationType.values().associateWith {
                when {
                    it.isSmall() -> rectCenteredAt(
                        x = HALF,
                        y = 1F - QUARTER,
                        width = SMALL_COMPLICATION_DIAMETER,
                    )

                    it.isBig() -> rectCenteredAt(
                        x = HALF,
                        y = 1F - QUARTER,
                        width = 1F - QUARTER,
                        height = SMALL_COMPLICATION_DIAMETER,
                    )

                    else -> RectF(0F, 0F, 0F, 0F)
                }
            }
        ),
        isVisibleWhenNotInteractive = false,
    ),
    ;

    companion object {
        fun fromId(id: Int) = values().first { it.id == id }
    }
}

private fun rectCenteredAt(x: Float, y: Float, width: Float, height: Float = width) =
    RectF(
        x - width / 2,
        y - height / 2,
        x + width / 2,
        y + height / 2,
    )

fun SimpleWatchFaceComplicationSlot.shouldDraw(isInteractiveMode: Boolean) =
    isInteractiveMode || isVisibleWhenNotInteractive

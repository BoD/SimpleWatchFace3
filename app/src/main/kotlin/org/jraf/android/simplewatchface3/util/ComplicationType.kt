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

package org.jraf.android.simplewatchface3.util

import androidx.annotation.DrawableRes
import androidx.wear.watchface.complications.data.ComplicationExperimental
import androidx.wear.watchface.complications.data.ComplicationType
import org.jraf.android.simplewatchface3.R

@OptIn(ComplicationExperimental::class)
private val BIG_COMPLICATION_STYLES = setOf(ComplicationType.LONG_TEXT, ComplicationType.LIST)

private val SMALL_COMPLICATION_STYLES = setOf(
    ComplicationType.SHORT_TEXT,
    ComplicationType.RANGED_VALUE,
    ComplicationType.MONOCHROMATIC_IMAGE,
)

fun ComplicationType.isBig(): Boolean = this in BIG_COMPLICATION_STYLES
fun ComplicationType.isSmall(): Boolean = this in SMALL_COMPLICATION_STYLES

@DrawableRes
fun ComplicationType.drawableId() = when {
    this == ComplicationType.RANGED_VALUE -> R.drawable.complication_ranged
    isBig() -> R.drawable.complication_big
    else -> R.drawable.complication_small
}

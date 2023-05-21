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

package org.jraf.android.simplewatchface3.configuration

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toBitmap
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.items
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
import androidx.wear.watchface.complications.ComplicationDataSourceInfo
import kotlinx.coroutines.launch
import org.jraf.android.simplewatchface3.R
import org.jraf.android.simplewatchface3.watchface.SimpleWatchFaceComplicationSlot

@Composable
fun SimpleWatchFaceConfigurationScreen(
    accentColor: Color,
    complicationsDataSourceInfo: Map<Int, ComplicationDataSourceInfo?>,
    onChooseComplicationClick: (Int) -> Unit,
    onPickAccentColorClick: () -> Unit,
) {
    val scalingLazyListState = rememberScalingLazyListState()
    Scaffold(
        timeText = { TimeText(Modifier.scrollAway(scalingLazyListState)) },
        vignette = { Vignette(VignettePosition.TopAndBottom) },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = scalingLazyListState)
        },
    ) {
        val coroutineScope = rememberCoroutineScope()
        val focusRequester = remember { FocusRequester() }

        ScalingLazyColumn(
            modifier = Modifier
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        scalingLazyListState.scrollBy(it.verticalScrollPixels)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            state = scalingLazyListState,
        ) {
            item {
                ListHeader {
                    Text(stringResource(R.string.configuration_colors))
                }
            }

            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = stringResource(R.string.configuration_accentColor))
                    },
                    colors = ChipDefaults.secondaryChipColors(),
                    onClick = { onPickAccentColorClick() },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(ChipDefaults.IconSize)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                    }
                )
            }

            item {
                ListHeader {
                    Text(stringResource(R.string.configuration_complications))
                }
            }

            items(SimpleWatchFaceComplicationSlot.values()) { slot ->
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = stringResource(slot.nameResId))
                    },
                    secondaryLabel = complicationsDataSourceInfo[slot.id]?.name?.let { name ->
                        {
                            Text(name)
                        }
                    },
                    icon = {
                        complicationsDataSourceInfo[slot.id]?.icon?.loadDrawable(LocalContext.current)?.let { iconDrawable ->
                            val sizePx = with(LocalDensity.current) { ChipDefaults.IconSize.toPx() }.toInt()
                            Icon(bitmap = iconDrawable.toBitmap(width = sizePx, height = sizePx).asImageBitmap(), contentDescription = null)
                        } ?: Box(
                            modifier = Modifier.size(ChipDefaults.IconSize)
                        )
                    },
                    colors = ChipDefaults.secondaryChipColors(),
                    onClick = { onChooseComplicationClick(slot.id) }
                )
            }
        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}

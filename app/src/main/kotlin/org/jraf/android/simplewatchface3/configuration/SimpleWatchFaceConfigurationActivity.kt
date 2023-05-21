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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.wear.watchface.editor.EditorSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SimpleWatchFaceConfigurationActivity : ComponentActivity() {
    private lateinit var editorSession: EditorSession
    private val editorSessionInitialized = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            editorSession = EditorSession.createOnWatchEditorSession(this@SimpleWatchFaceConfigurationActivity)
            editorSessionInitialized.value = true
        }

        setContent {
            val editorSessionInitialized by editorSessionInitialized.collectAsState(false)
            if (!editorSessionInitialized) {
                return@setContent
            }

            val complicationsDataSourceInfo by editorSession.complicationsDataSourceInfo.collectAsState(emptyMap())
            SimpleWatchFaceConfigurationScreen(
                accentColor = Color(0xFF00FF00),
                complicationsDataSourceInfo = complicationsDataSourceInfo,
                onChooseComplicationClick = { complicationSlotId ->
                    lifecycleScope.launch {
                        editorSession.openComplicationDataSourceChooser(complicationSlotId)
                    }
                },
                onPickAccentColorClick = {},
            )
        }
    }
}

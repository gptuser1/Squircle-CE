/*
 * Copyright Squircle CE contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blacksquircle.ui.feature.explorer.ui.extract

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.blacksquircle.ui.core.effect.sendNavigationResult
import com.blacksquircle.ui.ds.PreviewBackground
import com.blacksquircle.ui.ds.SquircleTheme
import com.blacksquircle.ui.ds.dialog.AlertDialog
import com.blacksquircle.ui.ds.textfield.TextField
import com.blacksquircle.ui.feature.explorer.R
import com.blacksquircle.ui.feature.explorer.api.navigation.ExtractDialog
import com.blacksquircle.ui.feature.explorer.ui.explorer.ARG_USER_INPUT
import com.blacksquircle.ui.feature.explorer.ui.explorer.KEY_EXTRACT_FILE

@Composable
internal fun ExtractScreen(
    navArgs: ExtractDialog,
    navController: NavController,
) {
    ExtractScreen(
        fileName = navArgs.fileName,
        destPath = navArgs.destPath,
        encrypted = navArgs.encrypted,
        onConfirmClicked = { password ->
            sendNavigationResult(
                key = KEY_EXTRACT_FILE,
                result = bundleOf(ARG_USER_INPUT to password)
            )
            navController.popBackStack()
        },
        onCancelClicked = {
            navController.popBackStack()
        },
    )
}

@Composable
private fun ExtractScreen(
    fileName: String,
    destPath: String,
    encrypted: Boolean,
    onConfirmClicked: (String) -> Unit = {},
    onCancelClicked: () -> Unit = {},
) {
    var password by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        title = stringResource(R.string.explorer_extract_dialog_title),
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.explorer_extract_dialog_message,
                        fileName,
                        destPath,
                    ),
                    color = SquircleTheme.colors.colorTextAndIconSecondary,
                    style = SquircleTheme.typography.text16Regular,
                )
                if (encrypted) {
                    TextField(
                        inputText = password,
                        onInputChanged = { value -> password = value },
                        labelText = stringResource(R.string.explorer_extract_dialog_password_label),
                    )
                }
            }
        },
        confirmButton = stringResource(R.string.explorer_extract_dialog_button_extract),
        dismissButton = stringResource(android.R.string.cancel),
        confirmButtonEnabled = !encrypted || password.isNotEmpty(),
        onConfirmClicked = { onConfirmClicked(password) },
        onDismissClicked = onCancelClicked,
        onDismiss = onCancelClicked,
    )
}

@PreviewLightDark
@Composable
private fun ExtractScreenPreview() {
    PreviewBackground {
        ExtractScreen(
            fileName = "archive.zip",
            destPath = "/storage/emulated/0/Download",
            encrypted = true,
        )
    }
}

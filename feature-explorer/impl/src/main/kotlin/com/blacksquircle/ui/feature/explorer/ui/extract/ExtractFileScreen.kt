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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blacksquircle.ui.core.extensions.daggerViewModel
import com.blacksquircle.ui.ds.PreviewBackground
import com.blacksquircle.ui.ds.SquircleTheme
import com.blacksquircle.ui.ds.dialog.AlertDialog
import com.blacksquircle.ui.ds.textfield.TextField
import com.blacksquircle.ui.feature.explorer.R
import com.blacksquircle.ui.feature.explorer.api.navigation.ExtractFileRoute
import com.blacksquircle.ui.feature.explorer.internal.ExplorerComponent

@Composable
internal fun ExtractFileScreen(
    navArgs: ExtractFileRoute,
    viewModel: ExtractFileViewModel = daggerViewModel { context ->
        val component = ExplorerComponent.buildOrGet(context)
        ExtractFileViewModel.Factory().also(component::inject)
    }
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    ExtractFileScreen(
        navArgs = navArgs,
        viewState = viewState,
        onConfirmClicked = { password -> viewModel.onConfirmClicked(navArgs, password) },
        onCancelClicked = viewModel::onBackClicked,
    )
}

@Composable
private fun ExtractFileScreen(
    navArgs: ExtractFileRoute,
    viewState: ExtractFileViewState,
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
                        navArgs.fileName,
                        navArgs.destPath,
                    ),
                    color = SquircleTheme.colors.colorTextAndIconSecondary,
                    style = SquircleTheme.typography.text16Regular,
                )
                if (navArgs.encrypted) {
                    TextField(
                        inputText = password,
                        onInputChanged = { value -> password = value },
                        labelText = stringResource(R.string.explorer_extract_dialog_password_label),
                        errorText = stringResource(R.string.explorer_extract_dialog_wrong_password),
                        error = viewState.wrongPassword,
                    )
                }
            }
        },
        confirmButton = stringResource(R.string.explorer_extract_dialog_button_extract),
        dismissButton = stringResource(android.R.string.cancel),
        confirmButtonEnabled = !viewState.isVerifying &&
            (!navArgs.encrypted || password.isNotEmpty()),
        onConfirmClicked = { onConfirmClicked(password) },
        onDismissClicked = onCancelClicked,
    )
}

@PreviewLightDark
@Composable
private fun ExtractFileScreenPreview() {
    PreviewBackground {
        ExtractFileScreen(
            navArgs = ExtractFileRoute(
                fileUri = "file:///storage/emulated/0/Download/archive.zip",
                filesystemUuid = "local",
                fileName = "archive.zip",
                destPath = "/storage/emulated/0/Download",
                encrypted = true,
            ),
            viewState = ExtractFileViewState(),
        )
    }
}

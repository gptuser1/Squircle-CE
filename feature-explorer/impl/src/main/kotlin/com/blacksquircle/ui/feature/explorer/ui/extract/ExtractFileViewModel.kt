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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blacksquircle.ui.core.effect.ResultEventBus
import com.blacksquircle.ui.core.provider.coroutine.DispatcherProvider
import com.blacksquircle.ui.feature.explorer.api.factory.FilesystemFactory
import com.blacksquircle.ui.feature.explorer.api.navigation.ExtractFileRoute
import com.blacksquircle.ui.feature.explorer.ui.explorer.KEY_EXTRACT_FILE
import com.blacksquircle.ui.filesystem.base.model.FileModel
import com.blacksquircle.ui.navigation.api.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider

internal class ExtractFileViewModel @Inject constructor(
    private val filesystemFactory: FilesystemFactory,
    private val dispatcherProvider: DispatcherProvider,
    private val navigator: Navigator,
) : ViewModel() {

    private val _viewState = MutableStateFlow(ExtractFileViewState())
    val viewState: StateFlow<ExtractFileViewState> = _viewState.asStateFlow()

    fun onConfirmClicked(route: ExtractFileRoute, password: String) {
        if (_viewState.value.isVerifying) {
            return
        }
        viewModelScope.launch {
            _viewState.update {
                it.copy(isVerifying = true, wrongPassword = false)
            }
            val isValid = withContext(dispatcherProvider.io()) {
                try {
                    val filesystem = filesystemFactory.create(route.filesystemUuid)
                    val fileModel = FileModel(
                        fileUri = route.fileUri,
                        filesystemUuid = route.filesystemUuid,
                    )
                    filesystem.verifyArchivePassword(fileModel, password.ifBlank { null })
                } catch (e: Exception) {
                    false
                }
            }
            if (isValid) {
                ResultEventBus.sendResult(KEY_EXTRACT_FILE, password)
                navigator.goBack()
            } else {
                _viewState.update {
                    it.copy(isVerifying = false, wrongPassword = true)
                }
            }
        }
    }

    fun onBackClicked() {
        navigator.goBack()
    }

    class Factory : ViewModelProvider.Factory {

        @Inject
        lateinit var viewModelProvider: Provider<ExtractFileViewModel>

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return viewModelProvider.get() as T
        }
    }
}

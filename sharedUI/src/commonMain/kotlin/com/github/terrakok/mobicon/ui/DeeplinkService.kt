package com.github.terrakok.mobicon.ui

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Immutable
class DeeplinkService {
    private val state = MutableStateFlow("")
    val deepLink: StateFlow<String> get() = state
    fun setDeepLink(link: String) { state.value = link }
}
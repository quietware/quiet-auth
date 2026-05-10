package io.quiet.auth.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

@Composable
fun useCurrentTime(tickMs: Long = 1_000L): State<Long> {
    val state = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(tickMs) {
        while (true) {
            state.longValue = System.currentTimeMillis()
            delay(tickMs)
        }
    }
    return state
}

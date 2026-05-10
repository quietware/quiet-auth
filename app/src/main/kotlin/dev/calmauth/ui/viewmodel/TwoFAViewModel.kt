package dev.calmauth.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.calmauth.CalmAuthApp
import dev.calmauth.data.TokenRepository
import dev.calmauth.domain.AddTwoFAInput
import dev.calmauth.domain.OtpAlgorithm
import dev.calmauth.domain.ParsedOtpAuth
import dev.calmauth.domain.TwoFAItem
import dev.calmauth.domain.normalizeTwoFAInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TwoFAUiState(
    val isLoading: Boolean = true,
    val items: List<TwoFAItem> = emptyList(),
)

class TwoFAViewModel(private val repository: TokenRepository) : ViewModel() {

    private val _state = MutableStateFlow(TwoFAUiState())
    val state: StateFlow<TwoFAUiState> = _state.asStateFlow()

    init {
        _state.value = TwoFAUiState(isLoading = false, items = repository.load())
    }

    fun addTwoFA(input: AddTwoFAInput) {
        val normalized = normalizeTwoFAInput(input)
        val item = TwoFAItem(
            id = System.currentTimeMillis().toString(),
            name = normalized.name,
            account = normalized.account,
            secret = normalized.secret,
            digits = normalized.digits,
            period = normalized.period,
            algorithm = normalized.algorithm,
        )
        update(_state.value.items + item)
    }

    fun addParsedOtpAuth(parsed: ParsedOtpAuth) {
        addTwoFA(
            AddTwoFAInput(
                name = parsed.name,
                account = parsed.account,
                secret = parsed.secret,
                digits = parsed.digits,
                period = parsed.period,
                algorithm = parsed.algorithm ?: OtpAlgorithm.SHA1,
            )
        )
    }

    fun removeTwoFA(id: String) {
        update(_state.value.items.filterNot { it.id == id })
    }

    fun replaceAll(items: List<TwoFAItem>) {
        update(items)
    }

    private fun update(items: List<TwoFAItem>) {
        _state.value = _state.value.copy(items = items)
        runCatching { repository.save(items) }
    }

    companion object {
        fun factory(app: CalmAuthApp) = viewModelFactory {
            initializer { TwoFAViewModel(app.tokenRepository) }
        }
    }
}

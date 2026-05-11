package io.quiet.auth.ui.patterns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.quiet.auth.ui.components.EmptyState
import io.quiet.auth.ui.components.LazyColumnMMD
import io.quiet.auth.ui.components.TokenRow

data class TokenListItemUi(
    val id: String,
    val name: String,
    val subtitle: String,
    val code: String,
)

@Composable
fun TokenListScreen(
    items: List<TokenListItemUi>,
    emptyMessage: String,
    onTokenClick: (String) -> Unit,
) {
    LazyColumnMMD(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        if (items.isEmpty()) {
            item {
                EmptyState(title = emptyMessage, description = "")
            }
        } else {
            items(items = items, key = { it.id }) { item ->
                TokenRow(
                    name = item.name,
                    subtitle = item.subtitle,
                    code = item.code,
                    onClick = { onTokenClick(item.id) },
                )
            }
        }
    }
}

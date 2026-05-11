package io.quiet.auth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.quiet.auth.ui.foundation.QuietSpacing

@Composable
fun QuietScaffold(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBarMMD(title = title)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = QuietSpacing.screenHorizontal, vertical = QuietSpacing.screenVertical),
                verticalArrangement = Arrangement.Top,
            ) {
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                    )
                }
                content()
            }
            if (bottomBar != null) {
                HorizontalDividerMMD()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = QuietSpacing.screenHorizontal, vertical = QuietSpacing.itemGap),
                ) {
                    bottomBar()
                }
            }
        }
    }
}

@Composable
fun QuietTopBar(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .statusBarsPadding()
            .padding(horizontal = QuietSpacing.screenHorizontal, vertical = QuietSpacing.itemGap),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun QuietBottomActions(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    primaryEnabled: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(QuietSpacing.itemGap)) {
        PrimaryButton(
            text = primaryLabel,
            onClick = onPrimaryClick,
            enabled = primaryEnabled,
        )
        if (secondaryLabel != null && onSecondaryClick != null) {
            SecondaryButton(text = secondaryLabel, onClick = onSecondaryClick)
        }
    }
}

@Composable
fun QuietListRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val rowModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(vertical = QuietSpacing.itemGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailing != null) trailing()
    }
    HorizontalDividerMMD()
}

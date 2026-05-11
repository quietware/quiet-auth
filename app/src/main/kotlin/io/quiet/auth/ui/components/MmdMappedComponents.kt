package io.quiet.auth.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Local mapping layer matching wireframe naming; this keeps screen code aligned with the MMD plan
 * while still allowing us to swap the underlying implementation safely.
 */
@Composable
fun TopAppBarMMD(
    title: String,
    modifier: Modifier = Modifier,
) {
    QuietTopBar(title = title)
}

@Composable
fun LazyColumnMMD(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
fun HorizontalDividerMMD(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(modifier = modifier)
}

@Composable
fun TextMMD(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        style = style,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun OutlinedButtonMMD(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
    ) {
        TextMMD(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun TextFieldMMD(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { TextMMD(label, style = MaterialTheme.typography.bodyMedium) },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun SwitchMMD(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
    )
}

@Composable
fun RadioButtonMMD(
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ModalBottomSheetMMD(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        content()
    }
}

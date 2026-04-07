package com.stark.kmpcontact.android.contacts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stark.kmpcontact.android.contacts.presentation.ContactEditorField
import com.stark.kmpcontact.android.contacts.presentation.ContactEditorState
import com.stark.kmpcontact.android.contacts.ui.theme.ContFieldContactInfo
import com.stark.kmpcontact.android.contacts.ui.theme.MainTabBarIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactEditorScreen(
    title: String,
    state: ContactEditorState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onFieldChange: (ContactEditorField, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = state.name.isNotBlank(),
                    ) {
                        Text(
                            text = "Save",
                            color = if (state.name.isNotBlank()) MainTabBarIcons else Color.Gray,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ContactEditorFieldItem(
                label = "Name",
                value = state.name,
                onValueChange = { onFieldChange(ContactEditorField.NAME, it) },
            )
            ContactEditorFieldItem(
                label = "Email",
                value = state.email,
                onValueChange = { onFieldChange(ContactEditorField.EMAIL, it) },
            )
            ContactEditorFieldItem(
                label = "Phone",
                value = state.phone,
                onValueChange = { onFieldChange(ContactEditorField.PHONE, it) },
            )
            ContactEditorFieldItem(
                label = "Note",
                value = state.note,
                onValueChange = { onFieldChange(ContactEditorField.NOTE, it) },
                minLines = 3,
            )
            ContactEditorFieldItem(
                label = "Tags",
                value = state.tagsText,
                onValueChange = { onFieldChange(ContactEditorField.TAGS, it) },
                supportingText = "Comma-separated values",
            )
        }
    }
}

@Composable
private fun ContactEditorFieldItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = label) },
        minLines = minLines,
        singleLine = minLines == 1,
        supportingText = supportingText?.let { hint ->
            { Text(text = hint) }
        },
    )
}

package com.stark.kmpcontact.android.contacts.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stark.kmpcontact.domain.model.Contact

@Composable
fun ListItemContact(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = contact.phone.orEmpty().ifBlank { "No phone" },
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = contact.email.orEmpty().ifBlank { "No email" },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

package com.stark.kmpcontact.android.contacts.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.android.contacts.ui.theme.ContTextEmailColor
import com.stark.kmpcontact.android.contacts.ui.theme.ContTagText

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
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ContactAvatar(name = contact.name)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val secondaryText = contact.email
                    ?: contact.phone
                    ?: contact.profile?.customStatus?.statusText
                    ?: "No contact info"
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ContTextEmailColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                contact.contact?.note
                    ?.takeIf { it.isNotBlank() }
                    ?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = ContTagText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
            }
        }
    }
}

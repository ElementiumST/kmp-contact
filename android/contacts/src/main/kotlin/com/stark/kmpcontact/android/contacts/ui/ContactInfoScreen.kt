package com.stark.kmpcontact.android.contacts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stark.kmpcontact.android.contacts.ui.theme.ContActionButtonForContactBackground
import com.stark.kmpcontact.android.contacts.ui.theme.ContFieldContactInfo
import com.stark.kmpcontact.android.contacts.ui.theme.ContInfoBoxBackground
import com.stark.kmpcontact.android.contacts.ui.theme.ContTagBackground
import com.stark.kmpcontact.android.contacts.ui.theme.ContTagText
import com.stark.kmpcontact.android.contacts.ui.theme.MainTabBarIcons
import com.stark.kmpcontact.domain.model.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactInfoScreen(
    contact: Contact,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onMessageClick: () -> Unit,
    onAudioCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text(text = "Contact") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = "Back", color = ContFieldContactInfo)
                    }
                },
                actions = {
                    TextButton(onClick = onEdit) {
                        Text(text = "Edit", color = MainTabBarIcons)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ContInfoBoxBackground.copy(alpha = 0.08f))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // TODO: The original Android screen used a blurred banner and rich avatar.
                    // Replace this simplified avatar/header once design assets are available.
                    ContactAvatar(
                        name = contact.name,
                        modifier = Modifier.size(88.dp),
                    )
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    contact.profile?.customStatus?.statusText
                        ?.takeIf { it.isNotBlank() }
                        ?.let { status ->
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ContFieldContactInfo,
                            )
                        }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ActionChip(label = "Message", onClick = onMessageClick)
                        ActionChip(label = "Audio", onClick = onAudioCallClick)
                        ActionChip(label = "Video", onClick = onVideoCallClick)
                    }
                }
            }

            ContactInfoSection(
                title = "Main info",
                entries = listOfNotNull(
                    detailEntry("Email", contact.email),
                    detailEntry("Phone", contact.phone),
                    detailEntry("About", contact.profile?.aboutSelf),
                    detailEntry("Note", contact.contact?.note),
                    detailEntry("Additional contact", contact.profile?.additionalContact),
                    detailEntry("Type", contact.interlocutorType),
                ),
            )

            contact.contact?.tags
                ?.filter { it.isNotBlank() }
                ?.takeIf { it.isNotEmpty() }
                ?.let { tags ->
                    ContactInfoSection(
                        title = "Tags",
                        entries = tags.map { tag -> tag to tag },
                        renderValueAsTag = true,
                    )
                }

            ContactInfoSection(
                title = "Technical info",
                entries = listOfNotNull(
                    detailEntry("Contact id", contact.contact?.contactId),
                    detailEntry("Profile id", contact.profile?.profileId),
                    detailEntry("LDAP id", contact.ldapUser?.ldapUserId),
                    detailEntry("External domain", contact.externalInfo?.externalDomainName),
                ),
            )
        }
    }
}

@Composable
private fun ContactInfoSection(
    title: String,
    entries: List<Pair<String, String>>,
    renderValueAsTag: Boolean = false,
) {
    if (entries.isEmpty()) {
        return
    }

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            entries.forEachIndexed { index, (label, value) ->
                if (renderValueAsTag) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ContTagBackground,
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = ContTagText,
                        )
                    }
                } else {
                    DetailRow(label = label, value = value)
                }

                if (index != entries.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ContActionButtonForContactBackground,
        onClick = onClick,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = MainTabBarIcons,
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = ContFieldContactInfo,
        )
        Text(
            text = value ?: "Not provided",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
        )
    }
}

private fun detailEntry(
    label: String,
    value: String?,
): Pair<String, String>? {
    val normalizedValue = value?.takeIf { it.isNotBlank() } ?: return null
    return label to normalizedValue
}

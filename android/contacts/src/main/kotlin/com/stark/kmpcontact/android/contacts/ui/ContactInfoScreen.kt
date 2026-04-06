package com.stark.kmpcontact.android.contacts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stark.kmpcontact.domain.model.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactInfoScreen(
    contact: Contact,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = contact.name) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = "Back")
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DetailRow(label = "name", value = contact.name)
            DetailRow(label = "email", value = contact.email)
            DetailRow(label = "phone", value = contact.phone)
            DetailRow(label = "interlocutorType", value = contact.interlocutorType)

            DetailRow(label = "contact.contactId", value = contact.contact?.contactId)
            DetailRow(label = "contact.type", value = contact.contact?.type)
            DetailRow(label = "contact.ownerProfileId", value = contact.contact?.ownerProfileId)
            DetailRow(label = "contact.createdAt", value = contact.contact?.createdAt?.toString())
            DetailRow(label = "contact.updatedAt", value = contact.contact?.updatedAt?.toString())
            DetailRow(label = "contact.deleted", value = contact.contact?.deleted?.toString())
            DetailRow(label = "contact.note", value = contact.contact?.note)
            DetailRow(label = "contact.tags", value = contact.contact?.tags?.joinToString())

            DetailRow(label = "profile.profileId", value = contact.profile?.profileId)
            DetailRow(label = "profile.userType", value = contact.profile?.userType)
            DetailRow(label = "profile.avatarResourceId", value = contact.profile?.avatarResourceId)
            DetailRow(label = "profile.additionalContact", value = contact.profile?.additionalContact)
            DetailRow(label = "profile.aboutSelf", value = contact.profile?.aboutSelf)
            DetailRow(label = "profile.companyId", value = contact.profile?.companyId)
            DetailRow(label = "profile.isGuest", value = contact.profile?.isGuest?.toString())
            DetailRow(label = "profile.deleted", value = contact.profile?.deleted?.toString())
            DetailRow(label = "profile.customStatus.statusText", value = contact.profile?.customStatus?.statusText)

            DetailRow(label = "ldapUser.ldapUserId", value = contact.ldapUser?.ldapUserId)
            DetailRow(label = "ldapUser.targets", value = contact.ldapUser?.targets?.joinToString())

            DetailRow(label = "externalInfo.externalDomainId", value = contact.externalInfo?.externalDomainId)
            DetailRow(label = "externalInfo.externalDomainName", value = contact.externalInfo?.externalDomainName)
            DetailRow(label = "externalInfo.externalDomainHost", value = contact.externalInfo?.externalDomainHost)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = label)
        Text(text = value ?: "null")
    }
}

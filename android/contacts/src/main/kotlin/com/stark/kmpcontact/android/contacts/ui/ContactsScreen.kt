package com.stark.kmpcontact.android.contacts.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stark.kmpcontact.android.contacts.presentation.ContactsDestination
import com.stark.kmpcontact.android.contacts.presentation.ContactsViewModel
import com.stark.kmpcontact.domain.model.Contact
import com.stark.kmpcontact.support.paging.PagingState

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val destination by viewModel.destination.collectAsState()
    val selectedContact by viewModel.selectedContact.collectAsState()
    val visibleContacts by viewModel.visibleContacts.collectAsState()
    val createDraft by viewModel.createDraft.collectAsState()
    val editDraft by viewModel.editDraft.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val contactsState by viewModel.contactsState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.toastMessages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    when (destination) {
        ContactsDestination.LIST -> ContactsListRoute(
            contacts = visibleContacts,
            contactsState = contactsState,
            searchQuery = searchQuery,
            onSearchChange = viewModel::onSearchQueryChange,
            onCreateClick = viewModel::openCreateContact,
            onRetry = viewModel::retryLoading,
            onContactClick = viewModel::onContactClick,
            onLoadMore = viewModel::loadNextPage,
            modifier = modifier,
        )
        ContactsDestination.INFO -> {
            val contact = selectedContact
            if (contact == null) {
                viewModel.onContactInfoBack()
            } else {
                ContactInfoScreen(
                    contact = contact,
                    onBack = viewModel::onContactInfoBack,
                    onEdit = viewModel::openEditContact,
                    onMessageClick = viewModel::onMessageClick,
                    onAudioCallClick = viewModel::onAudioCallClick,
                    onVideoCallClick = viewModel::onVideoCallClick,
                    modifier = modifier,
                )
            }
        }
        ContactsDestination.EDIT -> {
            if (selectedContact == null) {
                viewModel.onContactInfoBack()
            } else {
                ContactEditorScreen(
                    title = "Edit contact",
                    state = editDraft,
                    onBack = viewModel::dismissEditContact,
                    onSave = viewModel::saveEditDraft,
                    onFieldChange = viewModel::updateEditField,
                    modifier = modifier,
                )
            }
        }
        ContactsDestination.CREATE -> {
            ContactEditorScreen(
                title = "Create contact",
                state = createDraft,
                onBack = viewModel::dismissCreateContact,
                onSave = viewModel::saveCreateDraft,
                onFieldChange = viewModel::updateCreateField,
                modifier = modifier,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsListRoute(
    contacts: List<Contact>,
    contactsState: PagingState<Contact>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onRetry: () -> Unit,
    onContactClick: (Contact) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorMessage = contactsState.error?.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Contacts") },
                actions = {
                    TextButton(onClick = onCreateClick) {
                        Text(text = "New")
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        when {
            contactsState.isLoading && contactsState.items.isEmpty() -> LoadingState(
                modifier = Modifier.padding(innerPadding),
            )
            errorMessage != null && contactsState.items.isEmpty() -> MessageState(
                message = errorMessage,
                actionLabel = "Retry",
                onAction = onRetry,
                modifier = Modifier.padding(innerPadding),
            )
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text = "Search contacts") },
                            singleLine = true,
                        )
                    }

                    if (contacts.isEmpty()) {
                        item {
                            MessageState(
                                message = if (searchQuery.isBlank()) {
                                    "No contacts available."
                                } else {
                                    "Nothing matches your search."
                                },
                                fillAvailableSpace = false,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    itemsIndexed(
                        items = contacts,
                        key = { _, contact -> contact.stableListKey() },
                    ) { index, contact ->
                        if (index >= contacts.lastIndex - 2) {
                            LaunchedEffect(
                                index,
                                contacts.size,
                                contactsState.hasNext,
                                contactsState.isLoadingMore,
                            ) {
                                if (contactsState.hasNext && !contactsState.isLoadingMore) {
                                    onLoadMore()
                                }
                            }
                        }

                        ListItemContact(
                            contact = contact,
                            onClick = { onContactClick(contact) },
                        )
                    }

                    item {
                        when {
                            contactsState.isLoadingMore -> LoadingState(
                                fillAvailableSpace = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                            )
                            errorMessage != null && contactsState.items.isNotEmpty() -> MessageState(
                                message = errorMessage,
                                actionLabel = "Retry",
                                onAction = onRetry,
                                fillAvailableSpace = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                            )
                            else -> Spacer(modifier = Modifier.height(1.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState(
    fillAvailableSpace: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = if (fillAvailableSpace) modifier.fillMaxSize() else modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageState(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    fillAvailableSpace: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = if (fillAvailableSpace) modifier.fillMaxSize() else modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = message)

            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

private fun Contact.stableListKey(): String {
    return contact?.contactId
        ?: profile?.profileId
        ?: email
        ?: phone
        ?: name
}

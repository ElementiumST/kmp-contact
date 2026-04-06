package com.stark.kmpcontact.android.contacts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.stark.kmpcontact.android.contacts.presentation.ContactsViewModel
import com.stark.kmpcontact.domain.model.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val selectedContact by viewModel.selectedContact.collectAsState()

    if (selectedContact != null) {
        ContactInfoScreen(
            contact = selectedContact!!,
            onBack = viewModel::onContactInfoBack,
            modifier = modifier,
        )
        return
    }

    val lazyPagingItems = viewModel.contactsPagingData.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Contacts") },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        when (val refreshState = lazyPagingItems.loadState.refresh) {
            is LoadState.Loading -> LoadingState(modifier = Modifier.padding(innerPadding))
            is LoadState.Error -> MessageState(
                message = refreshState.error.message ?: "Failed to load contacts.",
                modifier = Modifier.padding(innerPadding),
            )
            is LoadState.NotLoading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(lazyPagingItems.itemCount) { index ->
                        val contact = lazyPagingItems[index] ?: return@items
                        ListItemContact(
                            contact = contact,
                            onClick = { viewModel.onContactClick(contact) },
                        )
                    }

                    item {
                        when (val appendState = lazyPagingItems.loadState.append) {
                            is LoadState.Loading -> LoadingState(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                            )
                            is LoadState.Error -> MessageState(
                                message = appendState.error.message ?: "Failed to load next page.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                            )
                            is LoadState.NotLoading -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message)
    }
}

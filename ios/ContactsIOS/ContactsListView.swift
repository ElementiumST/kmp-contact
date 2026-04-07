import SwiftUI

struct ContactsListView: View {
    @ObservedObject var viewModel: ContactsListViewModel

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isInitialLoading && viewModel.contacts.isEmpty {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = viewModel.initialError, viewModel.contacts.isEmpty {
                    VStack(spacing: 12) {
                        Text(error)
                            .multilineTextAlignment(.center)
                        Button("Retry") {
                            Task {
                                await viewModel.refresh()
                            }
                        }
                    }
                    .padding(24)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        ForEach(viewModel.contacts) { contact in
                            NavigationLink(value: contact) {
                                ContactRowView(contact: contact)
                                    .onAppear {
                                        viewModel.loadNextPageIfNeeded(currentItem: contact)
                                    }
                            }
                        }

                        if viewModel.isLoadingNextPage {
                            HStack {
                                Spacer()
                                ProgressView()
                                Spacer()
                            }
                        } else if let appendError = viewModel.appendError {
                            VStack(alignment: .leading, spacing: 8) {
                                Text(appendError)
                                    .font(.footnote)
                                    .foregroundStyle(.secondary)
                                Button("Retry next page") {
                                    if let lastItem = viewModel.contacts.last {
                                        viewModel.loadNextPageIfNeeded(currentItem: lastItem)
                                    }
                                }
                            }
                            .padding(.vertical, 8)
                        }
                    }
                    .listStyle(.plain)
                    .refreshable {
                        await viewModel.refresh()
                    }
                }
            }
            .navigationTitle("Contacts")
            .navigationDestination(for: ContactPresentationModel.self) { contact in
                ContactDetailView(contact: contact)
            }
        }
        .onAppear {
            viewModel.loadIfNeeded()
        }
    }
}

private struct ContactRowView: View {
    let contact: ContactPresentationModel

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(contact.name)
                .font(.headline)

            if let phone = contact.phone, !phone.isEmpty {
                Text(phone)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            if let email = contact.email, !email.isEmpty {
                Text(email)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
}

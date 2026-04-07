import SwiftUI
import ContactsShared

@main
struct ContactsIOSApp: App {
    @StateObject private var viewModel = ContactsListViewModel(
        module: CreateIosContactsModuleKt.createIosContactsModule(
            config: AppConfiguration.shared.moduleConfig
        )
    )

    var body: some Scene {
        WindowGroup {
            ContactsListView(viewModel: viewModel)
        }
    }
}

class AppConfiguration {
    static let shared = AppConfiguration()

    var moduleConfig: IosContactsConfig {
        IosContactsConfig(
            serverUrl: stringValue(for: "SERVER_URL"),
            authLogin: stringValue(for: "AUTH_LOGIN"),
            authPassword: stringValue(for: "AUTH_PASSWORD"),
            authRememberMe: boolValue(for: "AUTH_REMEMBER_ME")
        )
    }

    private func stringValue(for key: String) -> String {
        guard let value = Bundle.main.object(forInfoDictionaryKey: key) as? String, !value.isEmpty else {
            fatalError("Missing \(key) in Info.plist")
        }
        return value
    }

    private func boolValue(for key: String) -> Bool {
        if let boolValue = Bundle.main.object(forInfoDictionaryKey: key) as? Bool {
            return boolValue
        }
        if let stringValue = Bundle.main.object(forInfoDictionaryKey: key) as? String {
            return NSString(string: stringValue).boolValue
        }
        return false
    }
}

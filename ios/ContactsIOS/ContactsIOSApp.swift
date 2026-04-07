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
            serverUrl: resolvedStringValue(for: "SERVER_URL", fallback: Defaults.serverUrl),
            authLogin: resolvedStringValue(for: "AUTH_LOGIN", fallback: Defaults.authLogin),
            authPassword: resolvedStringValue(for: "AUTH_PASSWORD", fallback: Defaults.authPassword),
            authRememberMe: boolValue(for: "AUTH_REMEMBER_ME", fallback: Defaults.authRememberMe)
        )
    }

    private func resolvedStringValue(for key: String, fallback: String) -> String {
        guard let rawValue = Bundle.main.object(forInfoDictionaryKey: key) as? String else {
            return fallback
        }
        let value = rawValue.trimmingCharacters(in: .whitespacesAndNewlines)

        if value.isEmpty || value.contains("$(") || value == "https://localhost" || value == "http://localhost" {
            return fallback
        }

        return value
    }

    private func boolValue(for key: String, fallback: Bool) -> Bool {
        if let boolValue = Bundle.main.object(forInfoDictionaryKey: key) as? Bool {
            return boolValue
        }
        if let stringValue = Bundle.main.object(forInfoDictionaryKey: key) as? String {
            return NSString(string: stringValue).boolValue
        }
        return fallback
    }

    private enum Defaults {
        static let serverUrl = "https://alpha.hi-tech.org/api/rest"
        static let authLogin = "mobileuser3@testivcs.su"
        static let authPassword = "test"
        static let authRememberMe = false
    }
}

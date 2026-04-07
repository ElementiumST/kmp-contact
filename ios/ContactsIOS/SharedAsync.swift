import Foundation
import ContactsShared

enum SharedModuleError: LocalizedError {
    case missingResult

    var errorDescription: String? {
        switch self {
        case .missingResult:
            return "Shared module returned no result."
        }
    }
}

extension IosContactsModule {
    func loadContactsPageAsync(page: Int) async throws -> IosContactsPageResult {
        try await withCheckedThrowingContinuation { continuation in
            self.loadContactsPage(page: Int32(page)) { result, error in
                if let error {
                    continuation.resume(throwing: error)
                } else if let result {
                    continuation.resume(returning: result)
                } else {
                    continuation.resume(throwing: SharedModuleError.missingResult)
                }
            }
        }
    }
}

func unwrapKotlinBool(_ value: KotlinBoolean?) -> Bool? {
    value?.boolValue
}

func unwrapKotlinLong(_ value: KotlinLong?) -> Int64? {
    value?.int64Value
}

package io.getsafenow.libraries.gsn_matrix.api.widget

interface CallAnalyticCredentialsProvider {
    val posthogUserId: String?
    val posthogApiHost: String?
    val posthogApiKey: String?
    val rageshakeSubmitUrl: String?
    val sentryDsn: String?
}

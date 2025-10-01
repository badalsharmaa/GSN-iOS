package io.getsafenow.libraries.gsn_matrix.api.platform


/**
 * This service is responsible for initializing the platform-related settings of the SDK.
 */
interface InitPlatformService {
    /**
     * Initialize the platform-related settings of the SDK.
     * @param tracingConfiguration the tracing configuration to use for logging.
     */
    fun init(tracingConfiguration: TracingConfiguration)
}

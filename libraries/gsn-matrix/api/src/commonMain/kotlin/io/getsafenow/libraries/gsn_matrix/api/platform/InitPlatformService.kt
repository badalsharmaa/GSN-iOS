package io.getsafenow.libraries.gsn_matrix.api.platform

import io.getsafenow.libraries.gsn_matrix.api.tracing.TracingConfiguration


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

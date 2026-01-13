package io.getsafenow.libraries.gsn_showcase.annotation

import kotlin.reflect.KClass
/**
 * Internal metadata marker for codegen. Not intended for direct use in app code.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
@Suppress("LongParameterList")
annotation class GsnShowCaseCodegenMetadata(
    val packageSimpleName: String,
    val packageName: String,
    val showCaseElementName: String,
    val showCaseName: String,
    val showCaseGroup: String,
    val showCaseKDoc: String,
    val showCaseMetadataType: String,
    val enclosingClass: Array<KClass<*>> = emptyArray(),
    val showCaseWidthDp: Int = -1,
    val showCaseHeightDp: Int = -1,
    val insideWrapperClass: Boolean = false,
    val insideObject: Boolean = false,
    val previewParameterClass: Array<KClass<*>> = emptyArray(),
    val previewParameterName: String = "",
    val showCaseStyleName: String = "",
    val isDefaultStyle: Boolean = false,
    val generatedPropertyName: String = "",
    val tags: Array<String> = emptyArray(),
    val extraMetadata: Array<String> = emptyArray()
)
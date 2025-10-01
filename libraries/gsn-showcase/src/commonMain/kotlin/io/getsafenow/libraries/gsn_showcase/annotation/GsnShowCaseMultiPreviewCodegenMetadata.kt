package io.getsafenow.libraries.gsn_showcase.annotation

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class GsnShowCaseMultiPreviewCodegenMetadata(
    val previewName: String,
    val previewGroup: String,
    val supportTypeQualifiedName: String,
    val packageName: String,
    val showCaseWidth: Int,
    val showCaseHeight: Int,
)
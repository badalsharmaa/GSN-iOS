package io.getsafenow.libraries.gsn_core.extensionshelper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResultTest {

    @Test
    fun testFlatMap() {
        val initial = Result.success("initial")

        val otherResult = initial.flatMap { Result.success("other") }
        val errorResult = initial.flatMap { Result.failure<String>(IllegalStateException("error")) }

        assertEquals("other", otherResult.getOrNull())
        assertEquals("error", errorResult.exceptionOrNull()?.message)

        val e = assertFailsWith<IllegalStateException> {
            initial.flatMap<String, String> { error("caught error") }
        }
        assertEquals("caught error", e.message)

        val initialError = Result.failure<String>(IllegalStateException("initial error"))
        val mapErrorToSuccess = initialError.flatMap { Result.success("other") }
        val mapErrorToError = initialError.flatMap { Result.failure<String>(IllegalStateException("error")) }
        val mapErrorAndCatch: Result<String> = initialError.flatMap { error("error") }

        assertEquals("initial error", mapErrorToSuccess.exceptionOrNull()?.message)
        assertEquals("initial error", mapErrorToError.exceptionOrNull()?.message)
        assertEquals("initial error", mapErrorAndCatch.exceptionOrNull()?.message)
    }

    @Test
    fun testFlatMapCatching() {
        val initial = Result.success("initial")

        val otherResult = initial.flatMapCatching { Result.success("other") }
        val errorResult = initial.flatMapCatching { Result.failure<String>(IllegalStateException("error")) }
        val caughtExceptionResult: Result<String> = initial.flatMapCatching { error("caught error") }

        assertEquals("other", otherResult.getOrNull())
        assertEquals("error", errorResult.exceptionOrNull()?.message)
        assertEquals("caught error", caughtExceptionResult.exceptionOrNull()?.message)

        val initialError = Result.failure<String>(IllegalStateException("initial error"))
        val mapErrorToSuccess = initialError.flatMapCatching { Result.success("other") }
        val mapErrorToError = initialError.flatMapCatching { Result.failure<String>(IllegalStateException("error")) }
        val mapErrorAndCatch: Result<String> = initialError.flatMapCatching { error("error") }

        assertEquals("initial error", mapErrorToSuccess.exceptionOrNull()?.message)
        assertEquals("initial error", mapErrorToError.exceptionOrNull()?.message)
        assertEquals("initial error", mapErrorAndCatch.exceptionOrNull()?.message)
    }
}
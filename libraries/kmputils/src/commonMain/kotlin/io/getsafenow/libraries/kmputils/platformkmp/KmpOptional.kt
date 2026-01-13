package io.getsafenow.libraries.kmputils.platformkmp

/**
 * Multiplatform Optional implementation similar to java.util.Optional.
 */
sealed class KmpOptional<out T> {

    abstract fun isPresent(): Boolean
    fun isEmpty(): Boolean = !isPresent()

    abstract fun get(): T

    fun ifPresent(action: (T) -> Unit) {
        if (this is Some) action(value)
    }

    fun ifPresentOrElse(action: (T) -> Unit, emptyAction: () -> Unit) {
        when (this) {
            is Some -> action(value)
            None -> emptyAction()
        }
    }

    fun <U> map(mapper: (T) -> U): KmpOptional<U> = when (this) {
        is Some -> Some(mapper(value))
        None -> None
    }

    fun <U> flatMap(mapper: (T) -> KmpOptional<U>): KmpOptional<U> = when (this) {
        is Some -> mapper(value)
        None -> None
    }

    fun filter(predicate: (T) -> Boolean): KmpOptional<T> = when (this) {
        is Some -> if (predicate(value)) this else None
        None -> None
    }

    fun orElse(other: @UnsafeVariance T): T = when (this) {
        is Some -> value
        None -> other
    }

    fun orElseGet(supplier: () -> @UnsafeVariance T): T = when (this) {
        is Some -> value
        None -> supplier()
    }

    fun orElseThrow(): T = when (this) {
        is Some -> value
        None -> throw NoSuchElementException("No value present")
    }

    fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X): T = when (this) {
        is Some -> value
        None -> throw exceptionSupplier()
    }

    override fun toString(): String = when (this) {
        is Some -> "KmpOptional[$value]"
        None -> "KmpOptional.empty"
    }

    override fun equals(other: Any?): Boolean = when {
        this is Some<*> && other is Some<*> -> this.value == other.value
        this is None && other is None -> true
        else -> false
    }

    override fun hashCode(): Int = when (this) {
        is Some -> value?.hashCode() ?: 0
        None -> 0
    }

    object None : KmpOptional<Nothing>() {
        override fun isPresent(): Boolean = false
        override fun get(): Nothing = throw NoSuchElementException("No value present")
    }

    data class Some<T>(val value: T) : KmpOptional<T>() {
        override fun isPresent(): Boolean = true
        override fun get(): T = value
    }

    companion object {
        fun <T> empty(): KmpOptional<T> = None
        fun <T> of(value: T): KmpOptional<T> =
            value?.let { Some(it) } ?: throw NullPointerException("value is null")
        fun <T> ofNullable(value: T?): KmpOptional<T> =
            if (value == null) None else Some(value)
    }
}
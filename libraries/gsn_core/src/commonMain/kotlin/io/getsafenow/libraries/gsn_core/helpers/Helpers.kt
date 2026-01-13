package io.getsafenow.libraries.gsn_core.helpers

fun Boolean?.defaultTrue() = this ?: true

fun Boolean?.defaultFalse() = this ?: false

/**
 * Fixed-size circular buffer.
 *
 * Stores up to [bufferSize] items in a rotating fashion.
 * When full, new items overwrite the oldest ones.
 * Not thread safe.
 */
class GsnCircularBuffer<T : Any>(bufferSize: Int, factory: (Int) -> Array<T?>) {
    companion object {
        inline fun <reified T : Any> create(bufferSize: Int) =
            GsnCircularBuffer(bufferSize) { Array<T?>(bufferSize) { null } }
    }

    private val buffer = factory(bufferSize)
    private var writeIndex = 0

    fun contains(value: T): Boolean = buffer.contains(value)

    fun add(value: T) {
        if (writeIndex == buffer.size) {
            writeIndex = 0
        }
        buffer[writeIndex] = value
        writeIndex++
    }
}

/**
 * Simple circular buffer with fixed capacity.
 *
 * Stores up to [bufferSize] elements in a rotating manner.
 * When full, the oldest element is overwritten.
 * Not thread safe, kmp friendly.
 */
class GsnKmpCircularBuffer<T : Any>(bufferSize: Int, factory: (Int) -> Array<T?>) {

    companion object {
        inline fun <reified T : Any> create(bufferSize: Int): GsnKmpCircularBuffer<T> =
            GsnKmpCircularBuffer(bufferSize) { Array<T?>(bufferSize) { null } }
    }

    private val buffer = factory(bufferSize)
    private var writeIndex = 0

    /**
     * Returns true if [value] exists in the buffer.
     */
    fun contains(value: T): Boolean = buffer.contains(value)

    /**
     * Insert a new value into the buffer.
     * If the buffer is full, overwrites the oldest element.
     */
    fun add(value: T) {
        if (writeIndex == buffer.size) {
            writeIndex = 0
        }
        buffer[writeIndex] = value
        writeIndex++
    }
}

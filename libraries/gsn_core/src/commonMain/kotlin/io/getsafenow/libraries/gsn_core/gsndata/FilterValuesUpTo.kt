package io.getsafenow.libraries.gsn_core.gsndata

/**
 * Returns a list containing up to [count] values that match the given [predicate].
 * If fewer values match, all of them are returned.
 *
 * @param T the type of values in the list.
 * @param count the maximum number of matching values to take.
 * @param predicate the condition used to match values.
 * @return a list containing up to [count] matching values.
 */
inline fun <T> Iterable<T>.filterValuesUpTo(count: Int, predicate: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (element in this) {
        if (predicate(element)) {
            result.add(element)
            if (result.size == count) {
                break
            }
        }
    }
    return result
}

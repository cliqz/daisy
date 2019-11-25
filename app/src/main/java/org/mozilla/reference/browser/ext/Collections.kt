package org.mozilla.reference.browser.ext

infix fun <T> Iterable<T>.distinct(other: Iterable<T>): Set<T> {
    return this.subtract(other) + other.subtract(this)
}

package org.mozilla.reference.browser.ext

import androidx.core.util.AtomicFile
import java.io.OutputStream

fun AtomicFile.withOutputStream(block: (stream: OutputStream) -> Unit) {
    val stream = this.startWrite()
    try {
        block(stream)
        this.finishWrite(stream)
    } catch (_: Throwable) {
        this.failWrite(stream)
    }
}

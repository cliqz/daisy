/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.ext

import android.database.sqlite.SQLiteDatabase

/**
 * Encapsulate a lambda in a [SQLiteDatabase] transaction
 */
fun SQLiteDatabase.beginTransaction(
    transaction: SQLiteDatabase.() -> Unit
): Unit = beginTransaction(transaction, { throw it })

/**
 * Encapsulate a lambda in a [SQLiteDatabase] transaction, adding also a catch block.
 * This can be used to return also a value from the transaction itself.
 */
@Suppress("TooGenericExceptionCaught")
fun <T> SQLiteDatabase.beginTransaction(
    transaction: SQLiteDatabase.() -> T,
    catchBlock: (e: Exception) -> T
): T {
    beginTransaction()
    return try {
        val result = transaction.invoke(this)
        setTransactionSuccessful()
        result
    } catch (e: Exception) {
        catchBlock(e)
    } finally {
        endTransaction()
    }
}

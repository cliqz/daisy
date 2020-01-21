/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.database

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

internal class DatabaseHandler(private val helper: SQLiteOpenHelper) {
    private var mDatabase: SQLiteDatabase? = null
    val database: SQLiteDatabase?
        get() {
            if (isClosed) {
                mDatabase = helper.writableDatabase
            }
            return mDatabase
        }

    val isClosed: Boolean
        get() {
            synchronized(helper) { return mDatabase == null || !mDatabase!!.isOpen }
        }

    fun close() {
        if (!isClosed) {
            mDatabase!!.close()
            mDatabase = null
        }
    }

    fun forceReload() {
        close()
        mDatabase = helper.writableDatabase
    }

    init {
        try {
            mDatabase = helper.writableDatabase
        } catch (e: SQLiteException) { /*
            We found a crash on the PlayStore related to the line in the try block. We try to
            postpone the DB creation as a partial solution due to lack of information regarding
            the problem.
            */
            mDatabase = null
            Log.e("DatabaseHandler", "Can't open the DB", e)
        }
    }
}

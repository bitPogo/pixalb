/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.mock

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.db.SqlCursor
import tech.antibytes.util.test.MockError

class QueryStub<T : Any>(
    mapper: ((SqlCursor) -> T),
    val _execute: (() -> SqlCursor),
) : Query<T>(
    mutableListOf(),
    mapper,
) {
    override fun execute(): SqlCursor {
        return _execute.invoke()
    }
}

class SqlCursorStub(
    var _next: (() -> Boolean)? = null,
) : SqlCursor {
    override fun close() {
        /* Do nothing */
    }

    override fun getBytes(index: Int): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun getDouble(index: Int): Double? {
        TODO("Not yet implemented")
    }

    override fun getLong(index: Int): Long? {
        TODO("Not yet implemented")
    }

    override fun getString(index: Int): String? {
        TODO("Not yet implemented")
    }

    override fun next(): Boolean {
        return _next?.invoke()
            ?: throw MockError.MissingStub("Missing Sideeffect next")
    }
}

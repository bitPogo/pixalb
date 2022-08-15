/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.database

import com.squareup.sqldelight.db.SqlDriver

internal const val testDatabase = "test"

expect class DatabaseDriver constructor() {
    val dataBase: PixabayDataBase

    fun open(schema: SqlDriver.Schema)

    fun close()
}

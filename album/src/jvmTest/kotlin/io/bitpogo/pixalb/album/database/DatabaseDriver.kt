/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.database

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import kotlinx.serialization.json.Json

actual class DatabaseDriver {
    private var driver: SqlDriver? = null
    actual val dataBase: PixabayDataBase
        get() = PixabayDataBase(
            driver!!,
            ImageAdapter = Image.Adapter(
                ListAdapter(Json),
            ),
            QueryAdapter = Query.Adapter(
                InstantAdapter(),
            ),
        )

    actual suspend fun open(schema: SqlDriver.Schema) {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        schema.create(driver!!)
    }

    actual fun close() {
        driver?.close()
        driver = null
    }
}

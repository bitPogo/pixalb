/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.store.database

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import io.bitpogo.pixalb.store.database.Images
import io.bitpogo.pixalb.store.database.Queries
import kotlinx.serialization.json.Json
import io.bitpogo.pixalb.store.database.PixabayDataBase

actual class DatabaseDriver {
    private var driver: SqlDriver? = null
    actual val dataBase: PixabayDataBase
        get() = PixabayDataBase(
            driver!!,
            ImagesAdapter = Images.Adapter(
                ListAdapter(Json),
            ),
            QueriesAdapter = Queries.Adapter(
                InstantAdapter(),
            ),
        )

    actual fun open(schema: SqlDriver.Schema) {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        schema.create(driver!!)
    }

    actual fun close() {
        driver?.close()
        driver = null
    }
}

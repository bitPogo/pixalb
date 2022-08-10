/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.store.database

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.bitpogo.pixalb.store.database.Images
import io.bitpogo.pixalb.store.database.Queries
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
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
        val app = ApplicationProvider.getApplicationContext<Application>()
        driver = AndroidSqliteDriver(schema, app, testDatabase)
    }

    actual fun close() {
        driver?.close()
        driver = null

        val app = ApplicationProvider.getApplicationContext<Application>()
        app.deleteDatabase(testDatabase)
    }
}

/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album.database

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
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

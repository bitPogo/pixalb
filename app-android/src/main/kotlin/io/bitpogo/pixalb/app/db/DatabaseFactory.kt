/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app.db

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import io.bitpogo.pixalb.album.database.Image
import io.bitpogo.pixalb.album.database.InstantAdapter
import io.bitpogo.pixalb.album.database.ListAdapter
import io.bitpogo.pixalb.album.database.PixabayDataBase
import io.bitpogo.pixalb.album.database.Query
import io.bitpogo.pixalb.app.AppContract
import io.bitpogo.pixalb.app.AppContract.DatabaseFactory.Companion.DATABASE_NAME
import kotlinx.serialization.json.Json

object DatabaseFactory : AppContract.DatabaseFactory {
    override fun create(schema: SqlDriver.Schema, context: Context): PixabayDataBase {
        val driver = AndroidSqliteDriver(
            schema,
            context,
            DATABASE_NAME,
        )

        return PixabayDataBase(
            driver,
            ImageAdapter = Image.Adapter(
                ListAdapter(Json),
            ),
            QueryAdapter = Query.Adapter(
                InstantAdapter(),
            ),
        )
    }
}

/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.app

import android.content.Context
import com.squareup.sqldelight.db.SqlDriver
import io.bitpogo.pixalb.album.database.PixabayDataBase

object AppContract {
    interface DatabaseFactory {
        fun create(schema: SqlDriver.Schema, context: Context): PixabayDataBase

        companion object {
            const val DATABASE_NAME = "PixbayAlbum.db"
        }
    }

    const val INITIAL_QUERY = "fruits"

    enum class Routes {
        OVERVIEW,
        DETAILVIEW,
    }

    enum class LogTag(val value: String) {
        CLIENT_INFO("PIXABAY_CLIENT_INFO"),
        CLIENT_WARN("PIXABAY_CLIENT_WARN"),
        CLIENT_ERROR("PIXABAY_CLIENT_ERROR"),
        CLIENT_LOG("PIXABAY_CLIENT_LOG"),
    }
}

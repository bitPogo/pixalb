/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.lib

import android.content.Context
import com.squareup.sqldelight.db.SchemaMock
import com.squareup.sqldelight.db.SqlDriver
import io.bitpogo.pixalb.album.database.PixabayDataBase
import io.bitpogo.pixalb.app.AppContract
import io.bitpogo.pixalb.app.db.DatabaseFactory
import io.bitpogo.pixalb.app.kmock
import io.bitpogo.pixalb.fixture.MockContext
import org.junit.Test
import tech.antibytes.kfixture.fixture
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.kmock.Mock
import tech.antibytes.util.test.fulfils

@Mock(
    SqlDriver.Schema::class
)
class DatabaseFactorySpec {
    private val fixture = kotlinFixture()

    @Test
    fun `It fulfils DatabaseFactory`() {
        DatabaseFactory fulfils AppContract.DatabaseFactory::class
    }

    @Test
    fun `Given open is called with a Schema and Context, it creates a Database`() {
        // Given
        val context: Context = MockContext
        val schema: SchemaMock = kmock(relaxUnitFun = true)

        schema._version returns fixture.fixture()

        // When
        val actual = DatabaseFactory.create(schema, context)

        // Then
        actual fulfils PixabayDataBase::class
    }
}

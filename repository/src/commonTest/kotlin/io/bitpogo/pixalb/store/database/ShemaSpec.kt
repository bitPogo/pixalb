/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.store.database

import io.bitpogo.pixalb.store.fixture.pixabayItemsFixture
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import tech.antibytes.kfixture.fixture
import tech.antibytes.util.test.annotations.RobolectricConfig
import tech.antibytes.util.test.annotations.RobolectricTestRunner
import tech.antibytes.util.test.annotations.RunWithRobolectricTestRunner
import tech.antibytes.kfixture.kotlinFixture
import tech.antibytes.util.test.mustBe
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@RobolectricConfig(manifest = "--none")
@RunWithRobolectricTestRunner(RobolectricTestRunner::class)
class SchemaSpec {
    private val fixture = kotlinFixture()
    private val db = DatabaseDriver()

    @BeforeTest
    fun setUp() {
        db.open(PixabayDataBase.Schema)
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun `It adds and retrieves Images`() {
        // Given
        val pixabayItems = fixture.pixabayItemsFixture(total = 25, size = 25)
        val query: String = fixture.fixture()
        val now = Clock.System.now()
        val systemTZ = TimeZone.currentSystemDefault()
        val tomorrow = now.plus(1, DateTimeUnit.DAY, systemTZ)

        // When
        // Insert
        val dbQueries: ImageQueries = db.dataBase.imageQueries

        dbQueries.addQuery(
            query,
            pixabayItems.total,
            pixabayItems.total,
            tomorrow
        )

        pixabayItems.items.forEach { item ->
            dbQueries.transaction {
                dbQueries.addImage(
                    imageId = item.id,
                    user = item.user,
                    tags = listOf(item.tags),
                    downloads = item.downloads.toInt(),
                    likes = item.likes.toInt(),
                    comments = item.comments.toInt(),
                    previewUrl = item.preview,
                    largeUrl = item.large
                )

                dbQueries.addImageQuery(
                    inquery = query,
                    imageId = item.id
                )
            }
        }

        // Fetch
        val queryInfo = dbQueries.fetchQueryInfo(query, now).executeAsOne()
        val images = dbQueries.fetchImages(query, 0).executeAsList()

        // Then
        queryInfo.totalPages mustBe pixabayItems.total
        queryInfo.storedPages mustBe pixabayItems.total
        queryInfo.inquery mustBe query

        images.size mustBe pixabayItems.items.size
        images.forEachIndexed { idx, item ->
            item.imageId mustBe pixabayItems.items[idx].id
            item.user mustBe pixabayItems.items[idx].user
            item.previewUrl mustBe pixabayItems.items[idx].preview
            item.largeUrl mustBe pixabayItems.items[idx].large
            item.tags mustBe listOf(pixabayItems.items[idx].tags)
            item.comments.toUInt() mustBe pixabayItems.items[idx].comments
            item.likes.toUInt() mustBe pixabayItems.items[idx].likes
            item.downloads.toUInt() mustBe pixabayItems.items[idx].downloads
        }
    }

    @Test
    fun `It adds and retrieves Images while allowing overwriting`() {
        // Given
        val pixabayItems = fixture.pixabayItemsFixture(total = 25, size = 25)
        val query: String = fixture.fixture()
        val now = Clock.System.now()
        val systemTZ = TimeZone.currentSystemDefault()
        val tomorrow = now.plus(1, DateTimeUnit.DAY, systemTZ)

        // When
        // Insert
        val dbQueries: ImageQueries = db.dataBase.imageQueries

        repeat(fixture.fixture(1, 5)) {
            dbQueries.addQuery(
                query,
                pixabayItems.total,
                pixabayItems.total,
                tomorrow
            )

            pixabayItems.items.forEach { item ->
                dbQueries.transaction {
                    dbQueries.addImage(
                        imageId = item.id,
                        user = item.user,
                        tags = listOf(item.tags),
                        downloads = item.downloads.toInt(),
                        likes = item.likes.toInt(),
                        comments = item.comments.toInt(),
                        previewUrl = item.preview,
                        largeUrl = item.large
                    )

                    dbQueries.addImageQuery(
                        inquery = query,
                        imageId = item.id
                    )
                }
            }
        }

        // Fetch
        val queryInfo = dbQueries.fetchQueryInfo(query, now).executeAsOne()
        val images = dbQueries.fetchImages(query, 0).executeAsList()

        // Then
        queryInfo.totalPages mustBe pixabayItems.total
        queryInfo.storedPages mustBe pixabayItems.total
        queryInfo.inquery mustBe query

        images.size mustBe pixabayItems.items.size
        images.forEachIndexed { idx, item ->
            item.imageId mustBe pixabayItems.items[idx].id
            item.user mustBe pixabayItems.items[idx].user
            item.previewUrl mustBe pixabayItems.items[idx].preview
            item.largeUrl mustBe pixabayItems.items[idx].large
            item.tags mustBe listOf(pixabayItems.items[idx].tags)
            item.comments.toUInt() mustBe pixabayItems.items[idx].comments
            item.likes.toUInt() mustBe pixabayItems.items[idx].likes
            item.downloads.toUInt() mustBe pixabayItems.items[idx].downloads
        }
    }

    @Test
    fun `It adds and retrieves Images while respecting the paging`() {
        // Given
        val pixabayItems = fixture.pixabayItemsFixture(total = 100, size = 100)
        val query: String = fixture.fixture()
        val now = Clock.System.now()
        val systemTZ = TimeZone.currentSystemDefault()
        val tomorrow = now.plus(1, DateTimeUnit.DAY, systemTZ)
        val offset: Int = fixture.fixture(1, 50)

        // When
        // Insert
        val dbQueries: ImageQueries = db.dataBase.imageQueries

        dbQueries.addQuery(
            query,
            pixabayItems.total,
            pixabayItems.total,
            tomorrow
        )

        pixabayItems.items.forEach { item ->
            dbQueries.transaction {
                dbQueries.addImage(
                    imageId = item.id,
                    user = item.user,
                    tags = listOf(item.tags),
                    downloads = item.downloads.toInt(),
                    likes = item.likes.toInt(),
                    comments = item.comments.toInt(),
                    previewUrl = item.preview,
                    largeUrl = item.large
                )

                dbQueries.addImageQuery(
                    inquery = query,
                    imageId = item.id
                )
            }
        }

        // Fetch
        val queryInfo = dbQueries.fetchQueryInfo(query, now).executeAsOne()
        val images = dbQueries.fetchImages(query, offset.toLong()).executeAsList()

        // Then
        queryInfo.totalPages mustBe pixabayItems.total
        queryInfo.storedPages mustBe pixabayItems.total
        queryInfo.inquery mustBe query

        images.size mustBe 50
        images.forEachIndexed { idx, item ->
            item.imageId mustBe pixabayItems.items[idx + offset].id
            item.user mustBe pixabayItems.items[idx + offset].user
            item.previewUrl mustBe pixabayItems.items[idx + offset].preview
            item.largeUrl mustBe pixabayItems.items[idx + offset].large
            item.tags mustBe listOf(pixabayItems.items[idx + offset].tags)
            item.comments.toUInt() mustBe pixabayItems.items[idx + offset].comments
            item.likes.toUInt() mustBe pixabayItems.items[idx + offset].likes
            item.downloads.toUInt() mustBe pixabayItems.items[idx + offset].downloads
        }
    }

    @Test
    fun `It adds and retrieves Images while respecting the date limit`() {
        // Given
        val query: String = fixture.fixture()
        val now = Clock.System.now()
        val systemTZ = TimeZone.currentSystemDefault()
        val tomorrow = now.plus(1, DateTimeUnit.DAY, systemTZ)

        // When
        // Insert
        val dbQueries: ImageQueries = db.dataBase.imageQueries

        dbQueries.addQuery(
            query,
            100,
            100,
            now
        )

        val queryInfo = dbQueries.fetchQueryInfo(query, tomorrow).executeAsOneOrNull()

        // Then
        queryInfo mustBe null
    }

    @Test
    fun `It adds, updates and retrieves the PageIndices`() {
        // Given
        val query: String = fixture.fixture()
        val now = Clock.System.now()
        val systemTZ = TimeZone.currentSystemDefault()
        val tomorrow = now.plus(1, DateTimeUnit.DAY, systemTZ)
        val pages: Int = fixture.fixture(50, 100)

        // When
        // Insert
        val dbQueries: ImageQueries = db.dataBase.imageQueries

        dbQueries.addQuery(
            query,
            50,
            100,
            tomorrow
        )

        dbQueries.updatePageIndex(
            pages,
            query
        )

        val queryInfo = dbQueries.fetchQueryInfo(query, now).executeAsOne().storedPages

        // Then
        queryInfo mustBe pages
    }
}

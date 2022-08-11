/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package io.bitpogo.pixalb.album

import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher

actual val testScope1: CoroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
actual val testScope2: CoroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

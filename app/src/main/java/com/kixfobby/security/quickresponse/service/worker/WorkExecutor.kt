/*
 * Copyright 2021 LiteKite Startup. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kixfobby.security.quickresponse.service.worker

import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.service.worker.WorkExecutor
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An Executor that uses [ThreadPoolExecutor] with the available thread pool size and runs
 * work in background.
 */

@Singleton
class WorkExecutor @Inject constructor() : Executor {
    // A thread pool executor instance
    private val pool: ThreadPoolExecutor
    override fun execute(command: Runnable) {
        pool.execute(command)
    }

    /**
     * This Executor [ThreadPoolExecutor] will be kept in memory and it needs to be cleared by
     * ourselves when there was no work or when it's necessary.
     */
    private fun shutdownAndAwaitTermination() {
        val TERMINATION_AWAIT_TIMEOUT = 60
        pool.shutdown() // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(TERMINATION_AWAIT_TIMEOUT.toLong(), TimeUnit.SECONDS)) {
                pool.shutdownNow() // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(TERMINATION_AWAIT_TIMEOUT.toLong(), TimeUnit.SECONDS)) {
                    BaseActivity.printLog(TAG, "Pool did not terminate")
                }
            }
        } catch (ie: InterruptedException) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow()
        }
    }

    companion object {
        private val TAG = WorkExecutor::class.java.name
    }

    /**
     * Creates a new instance of [WorkExecutor] and it creates a new [ ]
     */
    init {
        // Gets the number of available cores (not always the same as the maximum number of cores)
        val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
        // Sets the Time Unit to seconds
        val KEEP_ALIVE_TIME = 1
        val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS
        // Creates a thread pool executor
        pool = ThreadPoolExecutor(
            NUMBER_OF_CORES,  // Initial pool size
            NUMBER_OF_CORES,  // Max pool size
            KEEP_ALIVE_TIME.toLong(),
            KEEP_ALIVE_TIME_UNIT,
            LinkedBlockingQueue()
        )
        // clears thread pool when the jvm exits or gets terminated.
        Runtime.getRuntime().addShutdownHook(Thread { shutdownAndAwaitTermination() })
    }
}
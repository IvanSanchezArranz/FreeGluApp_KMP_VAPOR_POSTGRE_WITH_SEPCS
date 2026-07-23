@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.ivan.freeglukmp.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.ivan.freeglukmp.data.db.AppDatabase
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        val databaseName = "freeglu.db"
        val fileManager = NSFileManager.defaultManager
        val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        val documentsDirectory = paths.first() as String
        val destinationPath = "$documentsDirectory/$databaseName"

        if (!fileManager.fileExistsAtPath(destinationPath)) {
            val bundlePath = NSBundle.mainBundle.pathForResource("freeglu", "db")
            if (bundlePath != null) {
                try {
                    fileManager.copyItemAtPath(bundlePath, destinationPath, null)
                } catch (e: Exception) {
                    println("Failed to copy iOS bundle DB asset: ${e.message}")
                }
            } else {
                println("freeglu.db resource not found in iOS bundle")
            }
        }

        return NativeSqliteDriver(AppDatabase.Schema, databaseName)
    }
}

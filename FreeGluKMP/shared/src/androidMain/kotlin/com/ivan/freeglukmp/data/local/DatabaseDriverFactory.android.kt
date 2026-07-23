package com.ivan.freeglukmp.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ivan.freeglukmp.data.db.AppDatabase
import com.ivan.freeglukmp.AndroidContextProvider
import java.io.FileOutputStream

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        val context = AndroidContextProvider.context ?: throw IllegalStateException("Android Context not initialized in AndroidContextProvider")
        val databaseName = "freeglu.db"
        val dbFile = context.getDatabasePath(databaseName)

        if (!dbFile.exists()) {
            try {
                dbFile.parentFile?.mkdirs()
                context.assets.open(databaseName).use { inputStream ->
                    FileOutputStream(dbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return AndroidSqliteDriver(AppDatabase.Schema, context, databaseName)
    }
}

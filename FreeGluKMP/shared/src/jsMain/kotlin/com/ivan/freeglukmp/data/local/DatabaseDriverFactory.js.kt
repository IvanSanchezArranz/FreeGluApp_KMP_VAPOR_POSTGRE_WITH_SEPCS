package com.ivan.freeglukmp.data.local

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        throw UnsupportedOperationException("SQLDelight not implemented on JS")
    }
}

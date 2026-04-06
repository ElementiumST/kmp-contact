package com.stark.kmpcontact.android.contacts.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactsSQLiteOpenHelper(
    context: Context,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE contacts (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                phone TEXT,
                email TEXT,
                interlocutor_type TEXT NOT NULL
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
        db.execSQL("DROP TABLE IF EXISTS contacts")
        onCreate(db)
    }

    private companion object {
        const val DATABASE_NAME = "contacts.db"
        const val DATABASE_VERSION = 1
    }
}

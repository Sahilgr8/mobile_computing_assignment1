package com.example.contextmonitoringapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper


class AssignmentDbHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        try {
            db.execSQL(SQL_CREATE_ENTRIES)
        } catch (e: SQLiteException) {
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        const val DATABASE_VERSION: Int = 1
        const val DATABASE_NAME: String = "sahil.db"

        private const val SQL_CREATE_ENTRIES = ("CREATE TABLE tblPat ("
                + " recID integer PRIMARY KEY autoincrement, "
                + " timestamp TEXT, "
                + " heartRate NUMERIC, "
                + " respiratoryRate NUMERIC, "
                + " symptomsNausea NUMERIC, "
                + " symptomsHeadache NUMERIC, "
                + " symptomsDiarrhea NUMERIC, "
                + " symptomsSoarThroat NUMERIC, "
                + " symptomsFever NUMERIC, "
                + " symptomsMuscleAche NUMERIC, "
                + " symptomsLOS NUMERIC, "
                + " symptomsCough NUMERIC, "
                + " symptomsSOB NUMERIC, "
                + " FeelingFT NUMERIC ); ")

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS tblPat"
    }
}
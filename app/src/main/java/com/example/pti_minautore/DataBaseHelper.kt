package com.example.pti_minautore
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context


class DataBaseHelper(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{

        private val DATABASE_NAME = "CSV2SQL"
        private val DATABASE_VERSION = 1

        private val TABLE_NAME = "animaux"
        private val COL_ID = "Id"
        private val COL_SEX = "Sex"
        private val COL_MOM = "Mom"
        private val COL_DAD = "Dad"
    }
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_PRODUCTS_TABLE = ("CREATE TABLE " +
                TABLE_NAME + "("
                + COL_ID + " INTEGER PRIMARY KEY," +
                COL_SEX + " TEXT" +
                COL_MOM + "TEXT" +
                COL_DAD + "TEXT" +")")
        db.execSQL(CREATE_PRODUCTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun getAllProducts(): java.util.ArrayList<HashMap<String, String>> {
        val proList: java.util.ArrayList<HashMap<String, String>>
        proList = java.util.ArrayList()
        val selectQuery = "SELECT  * FROM $TABLE_NAME"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                //Id, Company,Name,Price
                val map = HashMap<String, String>()
                map["Id"] = cursor.getString(0)
                map["Sex"] = cursor.getString(1)
                map["Mom"] = cursor.getString(2)
                map["Dad"] = cursor.getString(3)
                proList.add(map)
            } while (cursor.moveToNext())
        }

        return proList
    }



}
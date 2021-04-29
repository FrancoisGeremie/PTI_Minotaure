package com.example.pti_minautore

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class DBHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "DBTest"
        private const val DATABASE_TABLE = "BETAIL"

        private const val KEY_CODE = "code"
        private const val KEY_SEXE = "sexe"
        private const val KEY_MERE = "mere"
        private const val KEY_PERE = "pere"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        val req = "CREATE TABLE " + DATABASE_TABLE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_CODE + "  INTEGER," + KEY_SEXE + " TEXT," + KEY_MERE + " INTEGER," + KEY_PERE + " TEXT" + ")"
        db?.execSQL(req)
    }

    //Si on modifie la db --> update le numero de la version
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE)
        onCreate(db)
    }

    // Inserer des donnees
    fun writeData(animal: AnimalClass): Long{
        val db = this.writableDatabase

        var cv = ContentValues()
        cv.put(KEY_CODE, animal.code)
        cv.put(KEY_SEXE, animal.sexe)
        cv.put(KEY_MERE, animal.mere)
        cv.put(KEY_PERE, animal.pere)

        val success = db.insert(DATABASE_TABLE, null, cv)

        db.close()

        return success
    }

    // Lire des donnees
    fun readData(): ArrayList<AnimalClass>{
        val animalList: ArrayList<AnimalClass> = ArrayList<AnimalClass>()
        val db = this.readableDatabase

        val req = "SELECT * FROM " + DATABASE_TABLE
        var cursor: Cursor? = null

        try{
            cursor = db.rawQuery(req, null)
        }catch(e: SQLiteException){
            db.execSQL(req)
            return ArrayList()
        }

        var code: Int
        var sexe: String
        var mere: Int
        var pere: String

        if(cursor.moveToFirst()){
            do{
                code = cursor.getInt(cursor.getColumnIndex(KEY_CODE))
                sexe = cursor.getString(cursor.getColumnIndex(KEY_SEXE))
                mere = cursor.getInt(cursor.getColumnIndex(KEY_MERE))
                pere = cursor.getString(cursor.getColumnIndex(KEY_PERE))

                val animal = AnimalClass(code, sexe, mere, pere)
                animalList.add(animal)
            }while(cursor.moveToNext())
        }
        return animalList
    }

    // Update un animal
    fun updateData(animal: AnimalClass): Int {
        val db = this.writableDatabase

        var cv = ContentValues()
        cv.put(KEY_CODE, animal.code)
        cv.put(KEY_SEXE, animal.sexe)
        cv.put(KEY_MERE, animal.mere)
        cv.put(KEY_PERE, animal.pere)

        // Updating Row
        val success = db.update(DATABASE_TABLE, cv, KEY_CODE + "=" + animal.code, null)
        //2nd argument is String containing nullColumnHack

        // Closing database connection
        db.close()
        return success
    }
}
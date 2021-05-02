package com.example.pti_minautore

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(
                context,
                DB_NAME,
                null,
                DB_VERSION
        ) {
    private var mDataBase: SQLiteDatabase? = null
    private val mContext: Context
    private var mNeedUpdate = false

    @Throws(IOException::class)
    fun updateDataBase() {
        if (mNeedUpdate) {
            val dbFile =
                    File(DB_PATH + DB_NAME)
            if (dbFile.exists()) dbFile.delete()
            mNeedUpdate = false
        }
    }

    private fun checkDataBase(): Boolean {
        val dbFile =
                File(DB_PATH + DB_NAME)
        return dbFile.exists()
    }

    fun IsInDB(id : String) : Boolean{
        val db = this.readableDatabase

        val req = "SELECT * FROM " + DATABASE_TABLE+ " WHERE id='${id}'"
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(req, null)
            if (cursor.getCount() != 0) return true
            else return false
        }catch(e: SQLiteException){
            return false
        }



    }
    fun getIDFromName (name :String):String{
        val req = "SELECT id FROM ${DATABASE_TABLE} WHERE name='${name}'"
        var id = "_"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(req, null)
            if (cursor.getCount() != 0) {

                if (cursor.moveToFirst()) {
                    id = cursor.getString(0)
                }
            }
        }catch(e: SQLiteException){

        }
        return id
    }

    fun getFromAnything (search :String):ArrayList<AnimalClass>{
        val animalList: ArrayList<AnimalClass> = ArrayList<AnimalClass>()
        val req = "SELECT * FROM ${DATABASE_TABLE} WHERE id = '${search}' OR name = '${search}'"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(req, null)
        }catch(e: SQLiteException){
            db.execSQL(req)
            return ArrayList()
        }
        var id: String
        var sex: String
        var mom: String
        var dad: String
        var dadname: String
        var name: String

        if(cursor.moveToFirst()){
            do{
                id = cursor.getString(cursor.getColumnIndex(KEY_ID))
                sex = cursor.getString(cursor.getColumnIndex(KEY_SEX))
                mom = cursor.getString(cursor.getColumnIndex(KEY_MOM))
                dad = cursor.getString(cursor.getColumnIndex(KEY_DAD))
                dadname = cursor.getString(cursor.getColumnIndex(KEY_DAD_NAME))
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME))

                val animal = AnimalClass(id,sex,mom,dad,dadname,name)
                animalList.add(animal)
            }while(cursor.moveToNext())
        }
        return animalList
    }

    fun findBestMatch(id:String):String {
        var mate = ""
        if (checkDataBase()) {
            this.readableDatabase
            close()
            try {
                // imaginons qu'on a identifié le numéro 3183
                // après avoir effectué la query
                val detected = id


                val select = "select sex, mom, dad, dadname from DB_troupeau where id= '${detected}'"


                val selectcursor =this.readableDatabase.rawQuery(select, null)

                //vérifier que detected est dans la db
                if (selectcursor.getCount() != 0){

                    if (selectcursor.moveToFirst()) {
                        val sex = selectcursor.getString(0)
                        val mom = selectcursor.getString(1)
                        val dad = selectcursor.getString(2)



                        // vérifier avec la daronne pour paufiner
                        val selectdaronne = "select mom, dad, dadname from DB_troupeau where id= '${mom}'"
                        val daronnecursor = this.readableDatabase.rawQuery(selectdaronne, null)

                        //daronne dans la db
                        if (daronnecursor.getCount() != 0){
                            println("daronne dans la db")
                            val darmom = selectcursor.getString(0)
                            val dardad = selectcursor.getString(1)

                            val query = "select id from DB_troupeau where id!='${detected}' AND id!='${mom}' AND id!='${dad}' AND id!='${darmom}' AND id!='${dardad}' AND sex!='${sex}' AND mom!='${detected}' AND mom!='${mom}' AND mom!='${darmom}' AND dad!='${detected}' AND dad!='${dad}' AND dad!='${dardad}'"
                            val cursor = this.readableDatabase.rawQuery(query, null)

                            if (cursor.moveToFirst()) {
                               mate = cursor.getString(0)
                            }


                        }

                        //daronne pas dans la db
                        else {
                            println("daronne pas dans la db")
                            val query = "select id, sex, mom, dad from DB_troupeau where id!='${detected}' AND id!='${mom}' AND id!='${dad}' AND sex!='${sex}' AND mom!='${detected}' AND mom!='${mom}' AND dad!='${detected}' AND dad!='${dad}'"
                            val cursor = this.readableDatabase.rawQuery(query, null)
                            if (cursor.moveToFirst()) {
                                mate=cursor.getString(0)
                            }


                        }

                    }
                }


            } catch(e: Exception) { e.printStackTrace() }
        }
        return mate
    }

    @Throws(IOException::class)
    private fun copyDBFile() {
        val mInput =
                mContext.assets.open(DB_NAME)
        //InputStream mInput = mContext.getResources().openRawResource(R.raw.info);
        val mOutput: OutputStream =
                FileOutputStream(DB_PATH + DB_NAME)
        val mBuffer = ByteArray(1024)
        var mLength: Int
        while (mInput.read(mBuffer).also { mLength = it } > 0) mOutput.write(mBuffer, 0, mLength)
        mOutput.flush()
        mOutput.close()
        mInput.close()
    }

    @Throws(SQLException::class)
    fun openDataBase(): Boolean {
        mDataBase = SQLiteDatabase.openDatabase(
                DB_PATH + DB_NAME,
                null,
                SQLiteDatabase.CREATE_IF_NECESSARY
        )
        return mDataBase != null
    }

    // Inserer des donnees
    fun writeData(animal: AnimalClass): Long{
        val db = this.writableDatabase

        var cv = ContentValues()
        cv.put(KEY_ID, animal.id)
        cv.put(KEY_SEX, animal.sex)
        cv.put(KEY_MOM, animal.mom)
        cv.put(KEY_DAD, animal.dad)
        cv.put(KEY_DAD_NAME, animal.dadname)
        cv.put(KEY_NAME, animal.name)

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

        var id: String
        var sex: String
        var mom: String
        var dad: String
        var dadname: String
        var name: String

        if(cursor.moveToFirst()){
            do{
                id = cursor.getString(cursor.getColumnIndex(KEY_ID))
                sex = cursor.getString(cursor.getColumnIndex(KEY_SEX))
                mom = cursor.getString(cursor.getColumnIndex(KEY_MOM))
                dad = cursor.getString(cursor.getColumnIndex(KEY_DAD))
                dadname = cursor.getString(cursor.getColumnIndex(KEY_DAD_NAME))
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME))

                val animal = AnimalClass(id,sex,mom,dad,dadname,name)
                 animalList.add(animal)
            }while(cursor.moveToNext())
        }
        return animalList
    }



    @Synchronized
    override fun close() {
        if (mDataBase != null) mDataBase!!.close()
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase) {}
    override fun onUpgrade(
            db: SQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
    ) {
        if (newVersion > oldVersion) mNeedUpdate = true
    }

    companion object {
        private const val DB_VERSION = 1
        private const val DB_NAME = "troupeau.db"
        private const val DATABASE_TABLE = "DB_troupeau"
        private var DB_PATH = "/data/data/com.example.pti_minautore/databases/"

        private const val KEY_ID = "id"
        private const val KEY_SEX = "sex"
        private const val KEY_MOM = "mom"
        private const val KEY_DAD = "dad"
        private const val KEY_DAD_NAME = "dadname"
        private const val KEY_NAME = "name"



    }

    init {
        DB_PATH =
                if (Build.VERSION.SDK_INT >= 17) context.applicationInfo.dataDir + "/databases/" else "/data/data/" + context.packageName + "/databases/"
        mContext = context
        this.readableDatabase
    }
}
package com.example.pti_minautore

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
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
            copyDataBase()
            mNeedUpdate = false
        }
    }

    private fun checkDataBase(): Boolean {
        val dbFile =
                File(DB_PATH + DB_NAME)
        return dbFile.exists()
    }

    fun IsInDB(id : Int) : Boolean{
        val select = "select id from DB_troupeau where id= ${id}"


        val selectcursor = mDataBase!!.rawQuery(select, null)

        //vérifier que detected est dans la db
        if (selectcursor.getCount() != 0){ return true}
        else{return false}

    }

    private fun copyDataBase() {
        if (!checkDataBase()) {
            this.readableDatabase
            close()
            try {
                // imaginons qu'on a identifié le numéro 3183
                // après avoir effectué la query
                val detected = 3183


                val select = "select sex, mom, dad, dadname from DB_troupeau where id= ${detected}"


                val selectcursor = mDataBase!!.rawQuery(select, null)

                //vérifier que detected est dans la db
                if (selectcursor.getCount() != 0){

                    if (selectcursor.moveToFirst()) {
                        val sex = selectcursor.getString(0)
                        val mom = selectcursor.getString(1)
                        val dad = selectcursor.getString(2)



                        // vérifier avec la daronne pour paufiner
                        val selectdaronne = "select mom, dad, dadname from DB_troupeau where id= ${mom}"
                        val daronnecursor = mDataBase!!.rawQuery(selectdaronne, null)

                        //daronne dans la db
                        if (daronnecursor.getCount() != 0){
                            println("daronne dans la db")
                            val darmom = selectcursor.getString(0)
                            val dardad = selectcursor.getString(1)

                            val query = "select id, sex, mom, dad from DB_troupeau where id!=${detected} AND id!=${mom} AND id!=${dad} AND id!=${darmom} AND id!=${dardad} AND sex!='${sex}' AND mom!=${detected} AND mom!=${mom} AND mom!=${darmom} AND dad!=${detected} AND dad!=${dad} AND dad!=${dardad}"
                            val cursor = mDataBase!!.rawQuery(query, null)

                            if (cursor.moveToFirst()) println(cursor.getString(0))
                            // et ici garâce à cursor.getString on peut récupérer les arguments id, sex, mom et dad pour les afficher


                        }

                        //daronne pas dans la db
                        else {
                            println("daronne pas dans la db")
                            val query = "select id, sex, mom, dad from DB_troupeau where id!=${detected} AND id!=${mom} AND id!=${dad} AND sex!='${sex}' AND mom!=${detected} AND mom!=${mom} AND dad!=${detected} AND dad!=${dad}"
                            val cursor = mDataBase!!.rawQuery(query, null)
                            if (cursor.moveToFirst()) println(cursor.getString(0))
                            // et ici garâce à cursor.getString on peut récupérer les arguments id, sex, mom et dad pour les afficher
                        }

                    }
                } else println("aucun animal ne correspond dans la base de données")


            } catch(e: Exception) { e.printStackTrace() }
        }
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
        private const val DB_NAME = "troupeau.db"

        //mettre le fichier disponible sur ma branche troupeau.db sur un vrai téléphone ou dans l'émulateur au path désiré

        private var DB_PATH = "/data/data/com.example.pti_minautore/databases/"

        private const val DB_VERSION = 1
    }

    init {
        DB_PATH =
                if (Build.VERSION.SDK_INT >= 17) context.applicationInfo.dataDir + "/databases/" else "/data/data/" + context.packageName + "/databases/"
        mContext = context
        copyDataBase()
        this.readableDatabase
    }
}
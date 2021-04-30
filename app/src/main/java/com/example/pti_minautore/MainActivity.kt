package com.example.pti_minautore

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.pti_minautore.DatabaseHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var mDBHelper: DatabaseHelper? = null
    private var mDb: SQLiteDatabase? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mDBHelper = DatabaseHelper(this)

        try {
            mDBHelper!!.updateDataBase()
        } catch (mIOException: IOException) {
            throw Error("UnableToUpdateDatabase")
        }

        mDb = try {
            mDBHelper!!.getWritableDatabase()
        } catch (mSQLException: SQLException) {
            throw mSQLException
        }
        try {
            // imaginons qu'on a identifié le numéro 3183
            // après avoir effectué la query
            var detected = 3183
            var select = "select mom, dad from DB_troupeau where id= ${detected}"

            // a terme il faut une collonne nom de l'animal si c'est un mâle car je ne peut pas vérifié si on sélectionne le pêre XD

            var selectcursor =  mDb!!.rawQuery(select, null)
            if (selectcursor.moveToFirst()) {
                var mom = selectcursor.getString(0)
                var dad = selectcursor.getString(1)
                println (mom)
                println (dad)
                var query = "select id from DB_troupeau where id!=${detected} AND mom!=${detected} AND mom!=${mom} AND dad!='${dad}'"
                var cursor =  mDb!!.rawQuery(query, null)
                if (cursor.moveToFirst()) println (cursor.getString(0)) ;
            } ;




        } catch(e: Exception) { e.printStackTrace() }

        setContentView(R.layout.activity_main)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        title=resources.getString(R.string.first_fragment_label)
        replaceFragment(NavHostFragment())

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.camera_ic-> {
                    title=resources.getString(R.string.first_fragment_label)
                    replaceFragment(FirstFragment())
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.add_ic-> {
                    title=resources.getString(R.string.add_fragment_label)
                    replaceFragment(AddFragment())
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.data_ic-> {
                    title=resources.getString(R.string.second_fragment_label)
                    replaceFragment(SecondFragment())
                    return@setOnNavigationItemSelectedListener true
                }

            }
            false

        }



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.data_ic -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, bundle:Bundle=Bundle()){


    val fragmentManager = supportFragmentManager
    val transaction = fragmentManager.beginTransaction()
    fragment.arguments=bundle
    transaction.replace(R.id.nav_host_fragment,fragment)
    transaction.addToBackStack(null)
    transaction.commit()
}


package com.example.pti_minautore

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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


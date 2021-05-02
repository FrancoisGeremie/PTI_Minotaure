package com.example.pti_minautore


import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {
    var pred = ""

    // Adapter class is initialized and list is passed in the param.
    var itemAdapter : ItemAdapter? = null


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment



        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pred = arguments?.getString("pred").toString()

        val sf = view?.findViewById<EditText>(R.id.search_field)
        setupListofDataIntoRecyclerView(pred)

        if (pred!=null && pred!="null") {
            sf?.setText(pred)

        }
        val searchB = view.findViewById<Button>(R.id.button2)
        searchB.setOnClickListener{

            itemAdapter?.updateAdapter(getItemsList(sf?.text.toString()))
        }



    }

    /**
     * Function is used to show the list on UI of inserted data.
     */
    private fun setupListofDataIntoRecyclerView(search : String) {

        if (getItemsList(search).size > 0) {

            requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvItemsList).visibility = View.VISIBLE
            requireView().findViewById<TextView>(R.id.tvNoRecordsAvailable).visibility = View.GONE

            // Set the LayoutManager that this RecyclerView will use.
            requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvItemsList).layoutManager = LinearLayoutManager(requireContext())

            itemAdapter = ItemAdapter(requireContext(), getItemsList(search))

            // adapter instance is set to the recyclerview to inflate the items.
            requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvItemsList).adapter = itemAdapter

        } else {

            requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvItemsList).visibility = View.GONE
            requireView().findViewById<TextView>(R.id.tvNoRecordsAvailable).visibility = View.VISIBLE
        }
    }

    /**
     * Function is used to get the Items List from the database table.
     */
    private fun getItemsList(search:String): ArrayList<AnimalClass> {
        val databaseHandler = DatabaseHelper(requireContext())
        var animalList: ArrayList<AnimalClass> ?= null
        if (search.equals("null") xor search.equals("")) {
            //calling the viewEmployee method of DatabaseHandler class to read the records
            animalList = databaseHandler.readData()
        }else{
            animalList = databaseHandler.getFromAnything(search)
        }
        animalList.add(0,AnimalClass("Id","     Sexe","Id_mere","Id_pere","Nom_pere","Nom"))
        return animalList
    }

    fun updateRecordDialog(animalClass: AnimalClass) {
        val updateDialog = Dialog(requireContext(), R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        /*Set the screen content from a layout resource.
         The resource will be inflated, adding all top-level views to the screen.*/
        updateDialog.setContentView(R.layout.dialog_update)

        //put values on hints
        updateDialog.findViewById<EditText>(R.id.etUpdateCode).setText(animalClass.id)
        updateDialog.findViewById<EditText>(R.id.etUpdateSexe).setText(animalClass.sex)
        updateDialog.findViewById<EditText>(R.id.etUpdateMere).setText(animalClass.mom)
        updateDialog.findViewById<EditText>(R.id.etUpdatePere).setText(animalClass.dad)

        updateDialog.findViewById<EditText>(R.id.tvUpdate).setOnClickListener(View.OnClickListener {

            val id = updateDialog.findViewById<EditText>(R.id.etUpdateCode).text.toString()
            val sex = updateDialog.findViewById<EditText>(R.id.etUpdateSexe).text.toString()
            val mom = updateDialog.findViewById<EditText>(R.id.etUpdateMere).text.toString()
            val dad = updateDialog.findViewById<EditText>(R.id.etUpdatePere).text.toString()


            if (!id.isEmpty() && !sex.isEmpty() && !mom.isEmpty() && !dad.isEmpty()) {
                val status =
                        //databaseHandler.updateData(AnimalClass(code.toInt(), sexe, mere.toInt(), pere))
                //if (status > -1) {

                    Toast.makeText(requireContext(), "Record Updated.", Toast.LENGTH_LONG).show()

                    setupListofDataIntoRecyclerView(" ")

                    updateDialog.dismiss() // Dialog will be dismissed
                //}
            } else {
                Toast.makeText(
                        requireContext(),
                        "Les champs ne peuvent pas être vides",
                        Toast.LENGTH_LONG
                ).show()
            }
        })
        updateDialog.findViewById<EditText>(R.id.tvCancel).setOnClickListener(View.OnClickListener {
            updateDialog.dismiss()
        })
        //Start the dialog and display it on screen.
        updateDialog.show()
    }
}



















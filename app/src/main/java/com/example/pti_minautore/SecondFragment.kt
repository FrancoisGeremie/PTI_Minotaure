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


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment



        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /*super.onViewCreated(view, savedInstanceState)
        val name = arguments?.getString("pred")
        name?.let {
            val tv = view?.findViewById<TextView>(R.id.tv1)
            tv?.text = name
            tv?.visibility=VISIBLE}*/

        setupListofDataIntoRecyclerView()

    }

    /**
     * Function is used to show the list on UI of inserted data.
     */
    private fun setupListofDataIntoRecyclerView() {

        if (getItemsList().size > 0) {

            requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvItemsList).visibility = View.VISIBLE
            requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.tvNoRecordsAvailable).visibility = View.GONE

            // Set the LayoutManager that this RecyclerView will use.
            requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvItemsList).layoutManager = LinearLayoutManager(requireContext())
            // Adapter class is initialized and list is passed in the param.
            val itemAdapter = ItemAdapter(requireContext(), getItemsList())
            // adapter instance is set to the recyclerview to inflate the items.
            requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvItemsList).adapter = itemAdapter
        } else {

            requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvItemsList).visibility = View.GONE
            requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.tvNoRecordsAvailable).visibility = View.VISIBLE
        }
    }

    /**
     * Function is used to get the Items List from the database table.
     */
    private fun getItemsList(): ArrayList<AnimalClass> {
        //creating the instance of DatabaseHandler class
        val databaseHandler: DBHandler = DBHandler(requireContext())
        //calling the viewEmployee method of DatabaseHandler class to read the records
        val animalList: ArrayList<AnimalClass> = databaseHandler.readData()

        return animalList
    }

    fun updateRecordDialog(animalClass: AnimalClass) {
        val updateDialog = Dialog(requireContext(), R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        /*Set the screen content from a layout resource.
         The resource will be inflated, adding all top-level views to the screen.*/
        updateDialog.setContentView(R.layout.dialog_update)

        //put vzlues on hints
        updateDialog.findViewById<EditText>(R.id.etUpdateCode).setText(animalClass.code)
        updateDialog.findViewById<EditText>(R.id.etUpdateSexe).setText(animalClass.sexe)
        updateDialog.findViewById<EditText>(R.id.etUpdateMere).setText(animalClass.mere)
        updateDialog.findViewById<EditText>(R.id.etUpdatePere).setText(animalClass.pere)

        updateDialog.findViewById<EditText>(R.id.tvUpdate).setOnClickListener(View.OnClickListener {

            val code = updateDialog.findViewById<EditText>(R.id.etUpdateCode).text.toString()
            val sexe = updateDialog.findViewById<EditText>(R.id.etUpdateSexe).text.toString()
            val mere = updateDialog.findViewById<EditText>(R.id.etUpdateMere).text.toString()
            val pere = updateDialog.findViewById<EditText>(R.id.etUpdatePere).text.toString()

            val databaseHandler: DBHandler = DBHandler(requireContext())

            if (!code.isEmpty() && !sexe.isEmpty() && !mere.isEmpty() && !pere.isEmpty()) {
                val status =
                        databaseHandler.updateData(AnimalClass(code.toInt(), sexe, mere.toInt(), pere))
                if (status > -1) {
                    Toast.makeText(requireContext(), "Record Updated.", Toast.LENGTH_LONG).show()

                    setupListofDataIntoRecyclerView()

                    updateDialog.dismiss() // Dialog will be dismissed
                }
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


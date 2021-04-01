 package com.example.pti_minautore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Handler
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast

 // TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val name = arguments?.getString("pred")
        name?.let {
            val pred = requireView().findViewById<EditText>(R.id.editTextNumber)
            pred.setText(name)
        }


        val button = view?.findViewById<Button>(R.id.button)


        var helper = DBHandler(requireContext())
        // instance of database
        var db = helper.readableDatabase
        var rs = db.rawQuery("SELECT * FROM BETAIL", null)

        if(rs.moveToNext()){
            Toast.makeText(requireContext(), rs.getString(1), Toast.LENGTH_LONG).show()
        }


        button?.setOnClickListener{
            view -> SaveInDB(view)
        }

    }

    private fun SaveInDB(view: View) {

        val code = requireView().findViewById<EditText>(R.id.editTextNumber).text.toString()

        val rg = requireView().findViewById<RadioGroup>(R.id.radioGroup)
        val checkedID: Int = rg.checkedRadioButtonId
        val sexe = radioSelected(checkedID)

        val mere = requireView().findViewById<EditText>(R.id.editTextMere).text.toString()
        val pere = requireView().findViewById<EditText>(R.id.editTextPere).text.toString()

        val dbHandler: DBHandler = DBHandler(requireContext())

        if (!code.isEmpty() && !sexe.isEmpty() && !mere.isEmpty() && !pere.isEmpty()) {
            val status = dbHandler.writeData(AnimalClass(code.toInt(), sexe, mere.toInt(), pere))
            if (status > -1) {
                Toast.makeText(requireContext(), "Record saved", Toast.LENGTH_LONG).show()
                requireView().findViewById<EditText>(R.id.editTextNumber).text.clear()
                requireView().findViewById<RadioGroup>(R.id.radioGroup).clearCheck()
                requireView().findViewById<EditText>(R.id.editTextMere).text.clear()
                requireView().findViewById<EditText>(R.id.editTextPere).text.clear()

            }
        } else {
            Toast.makeText(requireContext(), "Problem", Toast.LENGTH_LONG).show()
        }
    }

    private fun radioSelected(checkedID: Int): String {
        when(checkedID){
            R.id.radioButtonF -> return "Femelle"
            R.id.radioButtonM -> return "Male"
            else -> {return "pas specifie"}
        }
    }


}
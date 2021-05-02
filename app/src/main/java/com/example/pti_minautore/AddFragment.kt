 package com.example.pti_minautore

import android.os.Bundle
import androidx.fragment.app.Fragment
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


        button?.setOnClickListener{
            view -> SaveInDB(view)
        }

    }

    private fun SaveInDB(view: View) {

        val id = requireView().findViewById<EditText>(R.id.editTextNumber).text.toString()

        val rg = requireView().findViewById<RadioGroup>(R.id.radioGroup)
        val checkedID: Int = rg.checkedRadioButtonId
        val sex = radioSelected(checkedID)

        val mom = requireView().findViewById<EditText>(R.id.editTextMere).text.toString()
        val dad = requireView().findViewById<EditText>(R.id.editTextPere).text.toString()
        //val dadname = requireView().findViewById<EditText>(R.id.editTextPere).text.toString()
        //val name = requireView().findViewById<EditText>(R.id.editTextPere).text.toString()

        val dbHandler = DatabaseHelper(requireContext())

        if (!id.isEmpty() && !sex.isEmpty() && !mom.isEmpty() && !dad.isEmpty()) {
            val status = dbHandler.writeData(AnimalClass(id=id, sex=sex, mom=mom, dad =dbHandler.getIDFromName(dad),dadname=dad,name="_"))
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
            R.id.radioButtonF -> return "F"
            R.id.radioButtonM -> return "M"
            else -> {return "_"}
        }
    }


}
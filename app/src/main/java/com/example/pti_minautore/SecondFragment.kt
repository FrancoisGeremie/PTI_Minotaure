package com.example.pti_minautore

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import kotlin.system.measureTimeMillis


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
        super.onViewCreated(view, savedInstanceState)



        val timeInMillis = measureTimeMillis(){
            val iv = view.findViewById<ImageView>(R.id.imageView2)

            iv.visibility = View.VISIBLE
            val bitmap: Bitmap = iv.drawable.toBitmap()

            val image = InputImage.fromBitmap(bitmap, 0)

            val recognizer = TextRecognition.getClient()

            var list = mutableListOf<String>()
            val result = recognizer.process(image)

                .addOnSuccessListener { visionText ->
                    list.addAll(succes(visionText))
                    val et = view.findViewById<TextView>(R.id.textView)
                    var pred:String = ""
                    for (elem in list)
                        pred = pred.plus(elem+"\n")

                    et.text = pred
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }
        }
        println("(The operation took $timeInMillis ms)")
        val et2 = view.findViewById<TextView>(R.id.textView2)

        et2.text = timeInMillis.toString() + " ms"

        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    fun succes (text : Text): MutableList<String> {
        val list: MutableList<String> = mutableListOf()
        val resultText = text.text
        for (block in text.textBlocks) {
            val blockText = block.text
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox
            for (line in block.lines) {
                val lineText = line.text
                val lineCornerPoints = line.cornerPoints
                val lineFrame = line.boundingBox
                for (element in line.elements) {
                    val elementText = element.text
                    val elementCornerPoints = element.cornerPoints
                    val elementFrame = element.boundingBox
                }
            }
        list.add(blockText)
        }
    return list
    }

}
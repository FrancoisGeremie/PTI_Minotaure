package com.example.pti_minautore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.Contacts
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import io.fotoapparat.selector.front
import io.fotoapparat.selector.off
import io.fotoapparat.selector.torch
import io.fotoapparat.view.CameraView
import kotlinx.coroutines.*
import java.lang.Runnable
import kotlin.properties.Delegates


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@Suppress("DEPRECATION")
class FirstFragment : Fragment() {


    var fotoapparat: Fotoapparat? = null
    var fotoapparatState : FotoapparatState? = null
    var cameraStatus : CameraState? = null
    var flashState: FlashState? = null
    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val predlist:MutableList<String> = mutableListOf()
    var bms: Bitmap? = null
    var pred: String = ""
    var done = false
    val t = lifecycleScope.async(Dispatchers.Unconfined) {
        lifecycleScope.async(Dispatchers.Unconfined) {
            repeat()
        }.await()
    }


    private var pred1 by Delegates.observable(0) { property, oldValue, newValue ->

        if (predlist.size >= 2 && predlist.last() in predlist.dropLast(1)) {
            onpred(predlist.last())
            predlist.clear()

        }
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        createFotoapparat()
        cameraStatus = CameraState.BACK
        flashState = FlashState.OFF
        fotoapparatState = FotoapparatState.OFF


        val switch_camera = view.findViewById<ImageButton>(R.id.switchCam)
        val switch_light = view.findViewById<ImageButton>(R.id.switchLight)




        switch_camera.setOnClickListener {
            switchCamera()
        }

        switch_light.setOnClickListener {
            changeFlashState()
        }

    }


    private fun onpred(pred : String){
        val bundle = Bundle()
        done = true
        bundle.putString("pred",pred )
        predlist.clear()
        (activity as MainActivity).replaceFragment(AddFragment(),bundle)

    }

    private suspend fun repeat(){
        while (done==false) {
            if (fotoapparatState == FotoapparatState.ON) {
                //repeat()


                val photoResult = fotoapparat?.takePicture()
                photoResult

                        ?.toBitmap()
                        ?.whenAvailable { bitmapPhoto ->

                            val matrix = Matrix()
                            if (bitmapPhoto?.rotationDegrees != null) {
                                matrix.postRotate((360 - bitmapPhoto?.rotationDegrees!!).toFloat())
                            }
                            val bm = bitmapPhoto?.bitmap

                            val bmr = bm?.let { Bitmap.createBitmap(it, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true) }
                            val bmc = bmr?.let { Bitmap.createBitmap(it, bmr.getWidth() / 10, bmr.getHeight() / 4, 8 * bmr.getWidth() / 10, bmr.getHeight() / 2) }
                            bms = bmc?.let { Bitmap.createScaledBitmap(it, 250, 150, true) }

                        }
                if (bms !=null) {
                    val image = InputImage.fromBitmap(bms, 0)
                    bms = null
                    val recognizer = TextRecognition.getClient()
                    val result = recognizer.process(image)
                    result.addOnSuccessListener {
                        if (it.text !=""){
                            predlist.add(it.text)
                            pred1 += 1
                        }
                    }
                }

            }
            delay(200)
        }
    }

    private fun createFotoapparat() {
        val cameraView = view?.findViewById<CameraView>(R.id.camera_view)

        fotoapparat = context?.let {
            cameraView?.let { it1 ->
                Fotoapparat(
                        context = it,
                        view = it1,
                        scaleType = ScaleType.CenterCrop,
                        lensPosition = back(),
                        logger = loggers(
                                logcat()
                        ),
                        cameraErrorCallback = { error ->
                            println("Recorder errors: $error")
                        }
                )
            }
        }
    }


    private fun changeFlashState() {
        fotoapparat?.updateConfiguration(
                CameraConfiguration(
                        flashMode = if(flashState == FlashState.TORCH) off() else torch()
                )
        )

        if(flashState == FlashState.TORCH) flashState = FlashState.OFF
        else flashState = FlashState.TORCH
    }

    private fun switchCamera() {
        fotoapparat?.switchTo(
                lensPosition =  if (cameraStatus == CameraState.BACK) front() else back(),
                cameraConfiguration = CameraConfiguration()
        )

        if(cameraStatus == CameraState.BACK) cameraStatus = CameraState.FRONT
        else cameraStatus = CameraState.BACK
    }



    private fun succes (text : Text): MutableList<String> {
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


    override fun onStart() {
        super.onStart()
        if (hasNoPermissions()) {
            requestPermission()
        }else{
            if ( fotoapparatState == FotoapparatState.OFF){
                fotoapparat?.start()
                fotoapparatState = FotoapparatState.ON

            }

        }
    }



    private fun hasNoPermissions(): Boolean{
        return ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(){
        ActivityCompat.requestPermissions(requireActivity(), permissions,0)
    }

    override fun onStop() {
        super.onStop()
        //t.interrupt()
        FotoapparatState.OFF
        fotoapparat?.stop()


    }

    override fun onResume() {
        super.onResume()

        if(!hasNoPermissions() && fotoapparatState == FotoapparatState.OFF){
            //val intent = Intent(context, MainActivity::class.java)
            //startActivity(intent)
            //requireActivity().finish()
            fotoapparat?.start()
            fotoapparatState = FotoapparatState.ON

        }
    }

}



enum class CameraState{
    FRONT, BACK
}

enum class FlashState{
    TORCH, OFF
}

enum class FotoapparatState{
    ON, OFF
}
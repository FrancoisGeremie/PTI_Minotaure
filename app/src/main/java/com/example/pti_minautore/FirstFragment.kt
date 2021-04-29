package com.example.pti_minautore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color.red
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Contacts
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
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
import java.text.BreakIterator
import kotlin.properties.Delegates
import kotlin.system.measureTimeMillis


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
    val recognizer = TextRecognition.getClient()


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
        val scan = view.findViewById<ImageButton>(R.id.scan)

        switch_camera.setOnClickListener {
            switchCamera()
        }

        switch_light.setOnClickListener {
            changeFlashState()
        }

        scan.setOnClickListener {
            scanning()
        }
    }

    private fun scanning(){
        lifecycleScope.async(Dispatchers.Main){
                for (x in 1..10) {
                    lifecycleScope.async(Dispatchers.Main) {
                        repeat()
                    }
                    delay(50)
                }
        }
    }

    private fun onpred(pred : String){

        val bundle = Bundle()
        bundle.putString("pred",pred )
        predlist.clear()

        //val dbHandler: DBHandler = DBHandler(requireContext())
        //val status = dbHandler.readData()


        if(true){ (activity as MainActivity).replaceFragment(AddFragment(),bundle)}
        else{(activity as MainActivity).replaceFragment(SecondFragment(),bundle)}
    }

    private fun repeat(){

        if (fotoapparatState == FotoapparatState.ON) {

            val photoResult = fotoapparat?.takePicture()
            photoResult

                    ?.toBitmap()
                    ?.whenAvailable { bitmapPhoto ->

                        val matrix = Matrix()
                        if (bitmapPhoto?.rotationDegrees != null) {
                            matrix.postRotate((360 - bitmapPhoto?.rotationDegrees!!).toFloat())
                        }

                        var bms : Bitmap? = null
                        val bm = bitmapPhoto?.bitmap
                        val bmt = bm?.let { Bitmap.createScaledBitmap(it, (500*(bm.width.toFloat()/bm.height)).toInt(), 500, true) }
                        val bmr = bmt?.let { Bitmap.createBitmap(it, 0, 0, bmt.getWidth(), bmt.getHeight(), matrix, true)  }
                        bms = bmr?.let { Bitmap.createBitmap(it, bmr.getWidth()/6, bmr.getHeight() / 3,  4*bmr.getWidth()/6 , bmr.getHeight() / 3)}

                        if (bms != null) {
                            val result = recognizer.process(InputImage.fromBitmap(bms, 0))
                            result.addOnSuccessListener {
                                if (it.text!=""){
                                    predlist.add(it.text)
                                    if (predlist.size >= 2 && predlist.last() in predlist.dropLast(1)) {
                                        onpred(predlist.last())
                                    }
                                }
                            }
                        }
                    }
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
        FotoapparatState.OFF
        fotoapparat?.stop()
    }

    override fun onResume() {
        super.onResume()

        if(!hasNoPermissions() && fotoapparatState == FotoapparatState.OFF){
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
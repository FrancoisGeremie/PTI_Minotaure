package com.example.pti_minautore

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    var fotoapparat: Fotoapparat? = null
    var fotoapparatState : FotoapparatState? = null
    var cameraStatus : CameraState? = null
    var flashState: FlashState? = null
    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val predlist:MutableList<String> = mutableListOf()
    var bms: Bitmap? = null
    var pred: String = ""
    var job: Job? = null





    val t = Thread {
        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                repeat()
                mainHandler.postDelayed(this, 50000)
            }
        })
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
            findNavController().navigate(R.id.action_FirstFragment_to_AddFragment)
        }

        switch_light.setOnClickListener {
            changeFlashState()
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        Thread.sleep(1500)
        if (hasNoPermissions()) { requestPermission() } else {

            job = GlobalScope.launch (Dispatchers.Main){
            while(fotoapparatState == FotoapparatState.ON){
                repeat()
                delay(500)
            }
                cancel()
            }
        }
    }


    private fun repeat(){

        var bms = takepic()
        bms?.let { scanPic(it) }
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

    private fun takepic ():Bitmap? {


        if (fotoapparatState == FotoapparatState.ON) {
            val photoResult = fotoapparat?.takePicture()
            photoResult

                    ?.toBitmap()
                    ?.whenAvailable { bitmapPhoto ->

                        val matrix = Matrix()
                        if (bitmapPhoto?.rotationDegrees!=null) {
                        matrix.postRotate((360 - bitmapPhoto?.rotationDegrees!!).toFloat())}
                        val bm = bitmapPhoto?.bitmap

                        val bmr = bm?.let { Bitmap.createBitmap(it, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true) }
                        val bmc = bmr?.let { Bitmap.createBitmap(it, bmr.getWidth() / 10, bmr.getHeight() / 4, 8 * bmr.getWidth() / 10, bmr.getHeight() / 2) }
                        bms = bmc?.let { Bitmap.createScaledBitmap(it, 350, 300, true) }
                    }
        }

        return bms


    }


    private  fun scanPic(bms:Bitmap) {


        val image = InputImage.fromBitmap(bms, 0)
        val recognizer = TextRecognition.getClient()
        val result = recognizer.process(image)
        result
                .addOnSuccessListener {

                    pred = it.text
                    if (pred != "") {
                        predlist += pred
                    }

                    if (predlist.size >= 2 && predlist.last() in predlist.dropLast(1)) {


                        onpred(predlist.last())


                    }
                    pred = ""

                }


    }

    private fun onpred(pred : String){
        val bundle = Bundle()
        bundle.putString("pred",pred )
        predlist.clear()
        findNavController().navigate(R.id.action_FirstFragment_to_AddFragment, bundle)
        job?.cancel()
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
            fotoapparat?.start()
            fotoapparatState = FotoapparatState.ON

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
        fotoapparat?.stop()
        FotoapparatState.OFF
    }

    override fun onResume() {
        super.onResume()
        if(!hasNoPermissions() && fotoapparatState == FotoapparatState.OFF){
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
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
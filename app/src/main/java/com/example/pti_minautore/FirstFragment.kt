package com.example.pti_minautore

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.common.InputImage
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


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@Suppress("DEPRECATION")
class FirstFragment : Fragment() {

    //création des objects relatifs à l'appareil photo
    var fotoapparat: Fotoapparat? = null
    var fotoapparatState : FotoapparatState? = null
    var cameraStatus : CameraState? = null
    var flashState: FlashState? = null
    val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

    //liste utilisée pour stocker les prédictions sur chaque capture
    val predlist:MutableList<String> = mutableListOf()

    //instance de l'outil OCR de MLKit
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

        //déclaration et listener des boutons
        val switch_camera = view.findViewById<ImageButton>(R.id.switchCam)
        val switch_light = view.findViewById<ImageButton>(R.id.switchLight)
        val scan = view.findViewById<ImageButton>(R.id.scan)
        val scan_running = view.findViewById<ImageButton>(R.id.scan_running)

        switch_camera.setOnClickListener {
            switchCamera()
        }

        switch_light.setOnClickListener {
            changeFlashState()
        }

        scan.setOnClickListener {
            // on analyse un certains nombres d'images pour s'assurer que la prédiction soit fiable

            scan_running.visibility= VISIBLE // désactivation et animation du bouton pendant l'analyse
            scan.visibility= INVISIBLE
            for (x in 1..10) {
                repeat()
            }
            Handler().postDelayed({
                //réactivation du bouton une fois l'analyse terminée
                scan.visibility= VISIBLE
                scan_running.visibility= INVISIBLE
            }, 5000)

        }
    }

    private fun repeat(){

        if (fotoapparatState == FotoapparatState.ON) { // Vérification que l'appareil photo est actif

            val photoResult = fotoapparat?.takePicture() // capture d'une image
            photoResult

                    ?.toBitmap()
                    ?.whenAvailable { bitmapPhoto ->
                        //si une photo a été capturée
                        val matrix = Matrix()
                        if (bitmapPhoto?.rotationDegrees != null) {
                            //création de la matrice qui sera utilisée pour remettre l'image dans le bon sens
                            matrix.postRotate((360 - bitmapPhoto.rotationDegrees).toFloat())
                        }



                        val bm = bitmapPhoto?.bitmap

                        // rescale ,rotation et crop de l'image
                        val bmt = bm?.let { Bitmap.createScaledBitmap(it, (500*(bm.width.toFloat()/bm.height)).toInt(), 500, true) }
                        val bmr = bmt?.let { Bitmap.createBitmap(it, 0, 0, bmt.getWidth(), bmt.getHeight(), matrix, true)  }
                        val bms = bmr?.let { Bitmap.createBitmap(it, bmr.getWidth()/6, bmr.getHeight() / 3,  4*bmr.getWidth()/6 , bmr.getHeight() / 3)}

                        if (bms != null) {
                            val result = recognizer.process(InputImage.fromBitmap(bms, 0)) //analyse de la photo par MLKit OCR
                            result.addOnSuccessListener {
                                // si un texte est détecté
                                val poss_pred = it.text.replace("\\s".toRegex(), "").takeLast(4) // on enlève les charactères spéciaux et on guarde les 4 derniers chiffres ( ou tous si - de 4 éléments)
                                if (poss_pred.length.equals(4) && poss_pred.all { it in '0'..'9' }){ // vérification que l'on obtient le bon nombre de charactères et que chaque charactère est un integer

                                    if (predlist.size >= 1 && poss_pred in predlist) {
                                        //si la même prédiction a déjà été effectué
                                        onpred(poss_pred)
                                    }else {
                                        //ajout à la liste des prédictions
                                        predlist.add(poss_pred)
                                    }
                                }
                            }
                        }
                    }
        }
    }

    private fun onpred(pred : String){
        // fonction appelée quand une prédiction définitive à été trouvée

        //Check si l'id est déjà dans la base de donnée
        val DBh = DatabaseHelper(requireContext())
        DBh.openDataBase()
        val InDB = DBh.IsInDB(pred.toInt())

        //création du bundle pour passer la prédiction aux autres fragments et clear de la liste des prédictions
        val bundle = Bundle()
        bundle.putString("pred",pred )
        predlist.clear()

        //changement de fragment en fournissant la prédiction
        if(!InDB){ (activity as MainActivity).replaceFragment(AddFragment(),bundle)}
        else{(activity as MainActivity).replaceFragment(SecondFragment(),bundle)}


    }


    /*
        Les fonctions suivantes sont relatives à l'appareil photo
     */

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
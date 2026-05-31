package com.example.tuneify_final_project.ui.moodify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.tuneify_final_project.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * MoodCameraActivity uses the device front camera and ML Kit Face Detection
 * to analyze the user's facial expression in real time and determine their mood.
 * It then navigates to MoodResultActivity with the detected mood.
 */
class MoodCameraActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var tvStatus: TextView
    private var analysisTriggered = false

    @OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_camera)

        previewView = findViewById(R.id.camera_preview)
        tvStatus    = findViewById(R.id.tv_camera_status)

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        findViewById<ImageView>(R.id.btn_capture).setOnClickListener {
            if (!analysisTriggered) {
                analysisTriggered = true
                tvStatus.text = "Analyzing facial expression…"
            }
        }

        findViewById<Button>(R.id.btn_cancel_camera).setOnClickListener { finish() }
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
            val detector = FaceDetection.getClient(options)

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                if (!analysisTriggered) {
                    imageProxy.close()
                    return@setAnalyzer
                }

                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(
                        mediaImage, imageProxy.imageInfo.rotationDegrees
                    )
                    detector.process(image)
                        .addOnSuccessListener { faces ->
                            if (faces.isEmpty()) {
                                runOnUiThread {
                                    tvStatus.text = "No face detected — try again"
                                    analysisTriggered = false
                                }
                            } else {
                                val mood = detectMoodFromFace(faces[0])
                                runOnUiThread { navigateToResult(mood) }
                            }
                            imageProxy.close()
                        }
                        .addOnFailureListener {
                            Log.e("MoodCamera", "Detection failed", it)
                            imageProxy.close()
                            runOnUiThread {
                                analysisTriggered = false
                                tvStatus.text = "Detection failed — try again"
                            }
                        }
                } else {
                    imageProxy.close()
                }
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(this))
    }
    // Input: face (Face)
    // Output: String (detected mood)
    private fun detectMoodFromFace(face: Face): String {
        val smiling  = face.smilingProbability      ?: 0.5f
        val leftEye  = face.leftEyeOpenProbability  ?: 0.5f
        val rightEye = face.rightEyeOpenProbability ?: 0.5f
        val eyesOpen = (leftEye + rightEye) / 2f

        return when {
            smiling > 0.75f && eyesOpen > 0.75f -> "Energetic"
            smiling > 0.60f                      -> "Happy"
            smiling < 0.20f && eyesOpen < 0.40f -> "Sad"
            smiling < 0.20f && eyesOpen > 0.60f -> "Angry"
            else                                  -> "Calm"
        }
    }

    private fun navigateToResult(mood: String) {
        startActivity(Intent(this, MoodResultActivity::class.java).apply {
            putExtra("MOOD", mood)
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
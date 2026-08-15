package com.example.security

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.data.db.VaultDatabase
import com.example.data.model.IntruderLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class IntruderSelfieCapture(private val context: Context) {

    fun captureIntruderSelfie(
        lifecycleOwner: LifecycleOwner,
        attemptedPin: String,
        onCaptured: (String) -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)

                val outputDir = File(context.filesDir, "intruder_photos").apply {
                    if (!exists()) mkdirs()
                }
                val outputFile = File(outputDir, "intruder_${System.currentTimeMillis()}.jpg")

                imageCapture.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            try {
                                val buffer: ByteBuffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.remaining())
                                buffer.get(bytes)
                                val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                
                                // Rotate if needed
                                val matrix = Matrix().apply {
                                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                                }
                                val rotatedBitmap = Bitmap.createBitmap(
                                    originalBitmap,
                                    0, 0,
                                    originalBitmap.width, originalBitmap.height,
                                    matrix, true
                                )

                                FileOutputStream(outputFile).use { out ->
                                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                                }

                                CoroutineScope(Dispatchers.IO).launch {
                                    val db = VaultDatabase.getInstance(context)
                                    db.vaultDao().insertIntruderLog(
                                        IntruderLogEntity(
                                            photoPath = outputFile.absolutePath,
                                            attemptTimestamp = System.currentTimeMillis(),
                                            attemptedPin = attemptedPin
                                        )
                                    )
                                }
                                onCaptured(outputFile.absolutePath)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                image.close()
                                cameraProvider.unbindAll()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            exception.printStackTrace()
                            cameraProvider.unbindAll()
                        }
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

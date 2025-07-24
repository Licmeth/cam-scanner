package org.agera.camscanner

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.core.graphics.createBitmap
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class ImageProcessor(private val width: Int, private val height: Int) {

    private val rgbaMat: Mat = Mat.zeros(height, width, CvType.CV_8UC4)
    private val grayMat: Mat = Mat.zeros(height, width, CvType.CV_8UC1)
    /**
     * Converts a [Mat] object to a [Bitmap].
     *
     * @param mat The OpenCV Mat object to convert.
     * @return A Bitmap representation of the Mat.
     */
    fun matToBitmap(mat: Mat): Bitmap {
        if (mat.channels() == 1) {
            // If the Mat is greyscale, convert it to RGBA
            Imgproc.cvtColor(mat, rgbaMat, Imgproc.COLOR_GRAY2RGBA)
        } else if (mat.channels() == 3) {
            // If the Mat is BGR, convert it to RGBA
            Imgproc.cvtColor(mat, rgbaMat, Imgproc.COLOR_BGR2RGBA)
        }
        val bitmap = createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        org.opencv.android.Utils.matToBitmap(rgbaMat, bitmap)
        return bitmap
    }

    fun imageProxyToGreyscaleMat(imageProxy: ImageProxy): Mat {
        val image = imageProxy.image ?: throw IllegalArgumentException("Image is null")
        val yBuffer = image.planes[0].buffer

        // Y plane is already greyscale
        yBuffer.rewind()
        val ySize = yBuffer.remaining()
        val yBytes = ByteArray(ySize)
        yBuffer.get(yBytes, 0, ySize)

        grayMat.put(0, 0, yBytes)
        return grayMat
    }
}
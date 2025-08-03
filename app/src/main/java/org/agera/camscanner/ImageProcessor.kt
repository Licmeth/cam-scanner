package org.agera.camscanner

import android.graphics.Bitmap
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.core.graphics.createBitmap
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class ImageProcessor private constructor(val inputWidth: Int,
                                         val inputHeight: Int,
                                         val width: Int,
                                         val height: Int,
                                         val isNeedsScaling: Boolean,
                                         val isNeedsRotation: Boolean,
                                         val config: ImageProcessorConfig) {

    companion object {
        val TRANSPARENT_PIXEL: Scalar = Scalar(0.0, 0.0, 0.0, 0.0)
        val TWO_PERCENT: Double = 0.02

        // Names of the constants used for image processing
        const val SCALE_DIMENSIONS: Boolean = true
        const val KEEP_DIMENSIONS: Boolean = false
        const val ROTATE: Boolean = true
        const val KEEP_ORIENTATION: Boolean = false

        /**
         * Creates an [ImageProcessor] instance based on the provided width and height.
         *
         * @param width The width of the image.
         * @param height The height of the image.
         * @return An [ImageProcessor] instance configured for the given dimensions.
         */
        fun create(width: Int, height: Int, config: ImageProcessorConfig): ImageProcessor {
            return (if (height < width) {
                // Image is in landscape orientation and needs to be rotated
                if (width > config.maxImageHeight) {
                    // Scale down the image to fit within the maximum height
                    ImageProcessor(
                            height,
                            width,
                            height * config.maxImageHeight / width,
                            config.maxImageHeight,
                            SCALE_DIMENSIONS,
                            ROTATE,
                            config)
                } else {
                    // Keep the original dimensions
                    ImageProcessor(
                            height,
                            width,
                            height,
                            width,
                            KEEP_DIMENSIONS,
                            ROTATE,
                            config)
                }
            } else {
                // Image is in portrait orientation and does not need to be rotated
                if (width > config.maxImageHeight) {
                    // Scale down the image to fit within the maximum height
                    ImageProcessor(
                            width,
                            height,
                            width * config.maxImageHeight / height,
                            config.maxImageHeight,
                            SCALE_DIMENSIONS,
                            KEEP_ORIENTATION,
                            config)
                } else {
                    // Keep the original dimensions
                    ImageProcessor(
                            width,
                            height,
                            width,
                            height,
                            KEEP_DIMENSIONS,
                            KEEP_ORIENTATION,
                            config)
                }
            })
        }
    }

    private val morphKernel: Mat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(config.morphKernelSize, config.morphKernelSize))
    private val morphAnchor: Point = Point(-1.0, -1.0) // Anchor point for the morphological kernel
    private val dilateKernel: Mat = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(config.edgeDilateKernelSize, config.edgeDilateKernelSize))
    private val gaussianBlurKernelSize: Size = Size(config.gaussianBlurKernelSize, config.gaussianBlurKernelSize)

    private val outputMat: Mat = Mat.zeros(inputHeight, inputWidth, CvType.CV_8UC4)
    private val rgbaProcessingMat: Mat = Mat.zeros(height, width, CvType.CV_8UC4)
    private val processingMat: Mat = Mat.zeros(height, width, CvType.CV_8UC1)
    private val orientedInputMat: Mat = Mat.zeros(inputHeight, inputWidth, CvType.CV_8UC1)
    private val unrotatedInputMat: Mat = Mat.zeros(inputWidth, inputHeight, CvType.CV_8UC1)
    private val hierarchy: Mat = Mat()
    private val contours: MutableList<MatOfPoint> = mutableListOf()
    private val outputSize: Size = Size(inputWidth.toDouble(), inputHeight.toDouble())

    /**
     * Processes the camera image to detect a document and apply an overlay with it's borders.
     * The process is taken from https://learnopencv.com/automatic-document-scanner-using-opencv/
     */
    fun processImage(imageProxy: ImageProxy): Bitmap {
        preprocessInput(imageProxy)
        if (config.outputStage == DebugOutputStage.PREPROCESSED) { return createBitmap(processingMat) }

        removeDocumentContent()
        if (config.outputStage == DebugOutputStage.CONTENT_REMOVED) { return createBitmap(processingMat) }

        edgeDetection()
        if (config.outputStage == DebugOutputStage.EDGES_DETECTED) { return createBitmap(processingMat) }

        findAndDrawContour()
        scaleOutput()
        return createBitmap(outputMat)
    }

    /**
     * Converts a [Mat] object to a [Bitmap].
     *
     * @param mat The OpenCV Mat object to convert.
     * @return A Bitmap representation of the Mat.
     */
    fun createBitmap(mat: Mat): Bitmap {
//        if (mat.channels() == 1) {
//            // If the Mat is greyscale, convert it to RGBA
//            Imgproc.cvtColor(mat, rgbaMat, Imgproc.COLOR_GRAY2RGBA)
//        } else if (mat.channels() == 3) {
//            // If the Mat is BGR, convert it to RGBA
//            Imgproc.cvtColor(mat, rgbaMat, Imgproc.COLOR_BGR2RGBA)
//        }
        val bitmap = createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        org.opencv.android.Utils.matToBitmap(mat, bitmap)
        return bitmap
    }

    /**
     * Preprocesses the input image by extracting the Y plane (greyscale), rotating
     * if necessary, and resizing if needed.
     *
     * @param imageProxy The ImageProxy containing the image data.
     */
    @OptIn(ExperimentalGetImage::class)
    private fun preprocessInput(imageProxy: ImageProxy) {
        val image = imageProxy.image ?: throw IllegalArgumentException("Image is null")
        val yBuffer = image.planes[0].buffer

        // Y plane is already greyscale
        yBuffer.rewind()
        val ySize = yBuffer.remaining()
        val yBytes = ByteArray(ySize)
        yBuffer.get(yBytes, 0, ySize)

        if (isNeedsRotation) {
            unrotatedInputMat.put(0, 0, yBytes)
            Core.rotate(unrotatedInputMat, orientedInputMat, Core.ROTATE_90_CLOCKWISE)
        } else {
            orientedInputMat.put(0, 0, yBytes)
        }

        if (isNeedsScaling) {
            Imgproc.resize(
                    orientedInputMat,
                    processingMat,
                    Size(
                            width.toDouble(),
                            height.toDouble()),
                    0.0,
                    0.0,
                    Imgproc.INTER_AREA)
        } else {
            orientedInputMat.copyTo(processingMat)
        }
    }

    /**
     * Removes the content of the document by applying morphological closing.
     */
    private fun removeDocumentContent() {
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_CLOSE, morphKernel, morphAnchor, config.morphIterations)
    }

    private fun edgeDetection() {
        Imgproc.GaussianBlur(processingMat, processingMat, gaussianBlurKernelSize, config.gaussianBlurSigmaX)
        Imgproc.Canny(processingMat, processingMat, config.cannyLowerHysteresisThreshold, config.cannyUpperHysteresisThreshold)
        //Imgproc.dilate(processingMat, processingMat, DILATE_KERNEL)
    }

    private fun findAndDrawContour() {
        contours.clear()
        Imgproc.findContours(processingMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE)

        // clear rgbaProcessingMat
        rgbaProcessingMat.setTo(TRANSPARENT_PIXEL)

        if (!contours.isEmpty()) {
            contours.sortByDescending { Imgproc.contourArea(it) }

            if (config.outputStage == DebugOutputStage.CONTOURS_DETECTED) {
                drawContours(rgbaProcessingMat, contours, config.contourSelectionCount)
            } else if (config.outputStage == DebugOutputStage.CORNERS_DETECTED) {
                val documentCorners = detectDocumentCorners(contours, config.contourSelectionCount)
                if (documentCorners != null) {
                    Imgproc.polylines(rgbaProcessingMat, listOf(MatOfPoint(documentCorners)), true, config.contourColor, config.contourThickness)
                }
            }
        }
        Imgproc.dilate(rgbaProcessingMat, rgbaProcessingMat, dilateKernel)
    }

    /**
     * Draws the contours on the image.
     *
     * @param image The image on which to draw the contours.
     * @param contours The list of contours to draw.
     * @param limit The maximum number of contours to draw.
     */
    private fun drawContours(image: Mat, contours: List<MatOfPoint>, limit: Int) {
        var i: Int = 0
        while (i < contours.size && i < limit) {
            Imgproc.drawContours(image, contours, i, config.contourColor, config.contourThickness)
            i++
        }
    }

    /**
     * Scales the output image to fit the original input dimensions.
     */
    private fun scaleOutput() {
        if (isNeedsScaling) {
            Imgproc.resize(
                rgbaProcessingMat,
                outputMat,
                outputSize,
                0.0,
                0.0,
                Imgproc.INTER_LINEAR
            )
        } else {
            rgbaProcessingMat.copyTo(outputMat)
        }
    }

    private fun detectDocumentCorners(contours: List<MatOfPoint>, limit: Int): MatOfPoint2f? {
        var i:Int = 0
        while (i < contours.size && i < limit) {
            var contour = MatOfPoint2f()
            contours[i].convertTo(contour, CvType.CV_32F)

            val epsilon = TWO_PERCENT * Imgproc.arcLength(contour, true)
            Imgproc.approxPolyDP(contour, contour, epsilon, true)

            if (contour.total() == 4L) {
                return contour
            }

            i++
        }
        return null
    }
}
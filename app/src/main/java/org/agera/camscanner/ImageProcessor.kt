package org.agera.camscanner

import android.graphics.Bitmap
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.core.graphics.createBitmap
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class ImageProcessor private constructor(val inputWidth: Int,
                                         val inputHeight: Int,
                                         val width: Int,
                                         val height: Int,
                                         val isNeedsScaling: Boolean,
                                         val isNeedsRotation: Boolean) {

    companion object {
        const val MAX_IMAGE_HEIGHT: Int = 1080 // Maximum pixel height for the image to be processed
        const val MORPH_KERNEL_SIZE: Double = 10.0 // Size of the morphological kernel
        const val MORPH_ITERATIONS: Int = 3 // Number of iterations for morphological operations
        const val GAUSSIAN_BLUR_SIGMA_X: Double = 0.0 // Sigma value for Gaussian blur, 0 means it is calculated from kernel size
        const val GAUSSIAN_BLUR_KERNEL_SIZE: Double = 11.0 // Size of the kernel for Gaussian blur
        const val EDGE_DILATE_KERNEL_SIZE: Double = 5.0 // Size of the kernel for dilation
        const val CANNY_LOWER_HYSTERESIS_THRESHOLD: Double = 30.0 // First threshold for Canny edge detection
        const val CANNY_UPPER_HYSTERESIS_THRESHOLD: Double = 150.0 // Second threshold for Canny edge detection
        const val GRAB_CUT_ITERATIONS: Int = 5 // Number of iterations for GrabCut algorithm
        const val GRAB_CUT_RECT_X_SIZE: Int = 200 // X size of the rectangle for GrabCut
        const val GRAB_CUT_RECT_Y_SIZE: Int = 200 // Y size of the rectangle for GrabCut

        val MORPH_KERNEL: Mat = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(MORPH_KERNEL_SIZE, MORPH_KERNEL_SIZE))
        val MORPH_ANCHOR: Point = Point(-1.0, -1.0) // Anchor point for the morphological kernel
        val DILATE_KERNEL: Mat = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(EDGE_DILATE_KERNEL_SIZE, EDGE_DILATE_KERNEL_SIZE))

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
        fun create(width: Int, height: Int): ImageProcessor {
            return (if (height < width) {
                // Image is in landscape orientation and needs to be rotated
                if (width > MAX_IMAGE_HEIGHT) {
                    // Scale down the image to fit within the maximum height
                    ImageProcessor(
                            height,
                            width,
                            height * MAX_IMAGE_HEIGHT / width,
                            MAX_IMAGE_HEIGHT,
                            SCALE_DIMENSIONS,
                            ROTATE)
                } else {
                    // Keep the original dimensions
                    ImageProcessor(
                            height,
                            width,
                            height,
                            width,
                            KEEP_DIMENSIONS,
                            ROTATE)
                }
            } else {
                // Image is in portrait orientation and does not need to be rotated
                if (width > MAX_IMAGE_HEIGHT) {
                    // Scale down the image to fit within the maximum height
                    ImageProcessor(
                            width,
                            height,
                            width * MAX_IMAGE_HEIGHT / height,
                            MAX_IMAGE_HEIGHT,
                            SCALE_DIMENSIONS,
                            KEEP_ORIENTATION)
                } else {
                    // Keep the original dimensions
                    ImageProcessor(
                            width,
                            height,
                            width,
                            height,
                            KEEP_DIMENSIONS,
                            KEEP_ORIENTATION)
                }
            })
        }
    }

    private val rgbaMat: Mat = Mat.zeros(inputHeight, inputWidth, CvType.CV_8UC4)
    private val processingMat: Mat = Mat.zeros(height, width, CvType.CV_8UC1)
    private val orientedInputMat: Mat = Mat.zeros(inputHeight, inputWidth, CvType.CV_8UC1)
    private val unrotatedInputMat: Mat = Mat.zeros(inputWidth, inputHeight, CvType.CV_8UC1)

    /**
     * Processes the camera image to detect a document and apply an overlay with it's borders.
     * The process is taken from https://learnopencv.com/automatic-document-scanner-using-opencv/
     */
    fun processImage(imageProxy: ImageProxy): Bitmap {
        preprocessInput(imageProxy)
        removeDocumentContent()
        edgeDetection()
        return createBitmap(processingMat)
    }

    /**
     * Converts a [Mat] object to a [Bitmap].
     *
     * @param mat The OpenCV Mat object to convert.
     * @return A Bitmap representation of the Mat.
     */
    fun createBitmap(mat: Mat): Bitmap {
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
                    org.opencv.core.Size(
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
        Imgproc.morphologyEx(processingMat, processingMat, Imgproc.MORPH_CLOSE, MORPH_KERNEL, MORPH_ANCHOR, MORPH_ITERATIONS)
    }

    private fun edgeDetection() {
        Imgproc.GaussianBlur(processingMat, processingMat, Size(GAUSSIAN_BLUR_KERNEL_SIZE, GAUSSIAN_BLUR_KERNEL_SIZE), GAUSSIAN_BLUR_SIGMA_X)
        Imgproc.Canny(processingMat, processingMat, CANNY_LOWER_HYSTERESIS_THRESHOLD, CANNY_UPPER_HYSTERESIS_THRESHOLD)
        Imgproc.dilate(processingMat, processingMat, DILATE_KERNEL)
    }
}
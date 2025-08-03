package org.agera.camscanner

import kotlinx.coroutines.flow.first
import android.util.Log

/** * User configurable properties of the image processor.
 *
 * @property outputStage The stage at which to output debug information.
 * @property maxImageHeight The maximum height of the processed image.
 * @property morphKernelSize The size of the kernel used for morphological operations.
 * @property morphIterations The number of iterations for morphological operations.
 * @property gaussianBlurSigmaX The standard deviation in X direction for Gaussian blur.
 * @property gaussianBlurKernelSize The size of the kernel used for Gaussian blur.
 * @property edgeDilateKernelSize The size of the kernel used for edge dilation.
 * @property cannyLowerHysteresisThreshold Lower threshold for Canny edge detection.
 * @property cannyUpperHysteresisThreshold Upper threshold for Canny edge detection.
 * @property contourColor Color used for drawing contours.
 * @property contourThickness Thickness of the contour lines.
 */
class ImageProcessorConfig(
    val outputStage: DebugOutputStage = DebugOutputStage.CORNERS_DETECTED,
    val maxImageHeight: Int = 1080,
    val morphKernelSize: Double = 10.0,
    val morphIterations: Int = 3,
    val gaussianBlurSigmaX: Double = 0.0,
    val gaussianBlurKernelSize: Double = 11.0,
    val edgeDilateKernelSize: Double = 2.0,
    val cannyLowerHysteresisThreshold: Double = 30.0,
    val cannyUpperHysteresisThreshold: Double = 150.0,
    val contourSelectionCount: Int = 5,
    val contourColor: org.opencv.core.Scalar = org.opencv.core.Scalar(255.0, 0.0, 0.0, 255.0),
    val contourThickness: Int = 2
) {
    companion object {
        /** Default configuration for the image processor. */
        val DEFAULT = ImageProcessorConfig()

        /**
         * Creates a new instance of [ImageProcessorConfig] with values from the settings dictionary.
         */
        fun fromSettings(settings: Map<String, Any>): ImageProcessorConfig {
            try {
                val config = ImageProcessorConfig(
                    outputStage = DebugOutputStage.entries.find { it.value == settings[SettingKeys.OUTPUT_STAGE.name] as Int }
                        ?: throw IllegalArgumentException("Invalid output stage: ${settings[SettingKeys.OUTPUT_STAGE.name]}"),
                    maxImageHeight = settings[SettingKeys.MAX_IMAGE_HEIGHT.name] as Int,
                    morphKernelSize = settings[SettingKeys.MORPH_KERNEL_SIZE.name] as Double,
                    morphIterations = settings[SettingKeys.MORPH_ITERATIONS.name] as Int,
                    gaussianBlurSigmaX = settings[SettingKeys.GAUSSIAN_BLUR_SIGMA_X.name] as Double,
                    gaussianBlurKernelSize = settings[SettingKeys.GAUSSIAN_BLUR_KERNEL_SIZE.name] as Double,
                    edgeDilateKernelSize = settings[SettingKeys.EDGE_DILATE_KERNEL_SIZE.name] as Double,
                    cannyLowerHysteresisThreshold = settings[SettingKeys.CANNY_LOWER_HYSTERESIS_THRESHOLD.name] as Double,
                    cannyUpperHysteresisThreshold = settings[SettingKeys.CANNY_UPPER_HYSTERESIS_THRESHOLD.name] as Double,
                    contourSelectionCount = settings[SettingKeys.CONTOUR_SELECTION_COUNT.name] as Int,
                    contourColor = intToScalar(settings[SettingKeys.CONTOUR_COLOR.name] as Int),
                    contourThickness = settings[SettingKeys.CONTOUR_THICKNESS.name] as Int
                )
                return config
            } catch (e: Exception) {
                Log.e(this::class.java.getPackage().name, "Error retrieving settings: ${e.message}")
            }

            return DEFAULT
            }

        /**
         * Converts a color integer to an OpenCV scalar.
         *
         * @param color The color in ARGB format.
         * @return The corresponding OpenCV Scalar.
         */
        private fun intToScalar(color: Int): org.opencv.core.Scalar {
            return org.opencv.core.Scalar(
                (color shr 16 and 0xFF).toDouble(),
                (color shr 8 and 0xFF).toDouble(),
                (color and 0xFF).toDouble(),
                (color shr 24 and 0xFF).toDouble()
            )
        }
    }
}
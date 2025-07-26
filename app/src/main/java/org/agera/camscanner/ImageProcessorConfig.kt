package org.agera.camscanner

import kotlinx.coroutines.flow.first

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
    val outputStage: DebugOutputStage = DebugOutputStage.FINAL_OUTPUT,
    val maxImageHeight: Int = 1080,
    val morphKernelSize: Double = 10.0,
    val morphIterations: Int = 3,
    val gaussianBlurSigmaX: Double = 0.0,
    val gaussianBlurKernelSize: Double = 11.0,
    val edgeDilateKernelSize: Double = 2.0,
    val cannyLowerHysteresisThreshold: Double = 30.0,
    val cannyUpperHysteresisThreshold: Double = 150.0,
    val contourColor: org.opencv.core.Scalar = org.opencv.core.Scalar(255.0, 0.0, 0.0, 255.0),
    val contourThickness: Int = 2
) {
    companion object {
        /** Default configuration for the image processor. */
        val DEFAULT = ImageProcessorConfig()

        /**
         * Creates a new instance of [ImageProcessorConfig] with values from settings
         */
        suspend fun fromSettings(settings: SettingsDataStore): ImageProcessorConfig {
            return ImageProcessorConfig(
                outputStage = DebugOutputStage.entries.find { it.value == settings.outputStage.first() } ?: DebugOutputStage.FINAL_OUTPUT
            )
        }
    }
}
package org.agera.camscanner

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "settings")

object SettingKeys {
    val OUTPUT_STAGE = intPreferencesKey("image_processor.output_stage")
    val MAX_IMAGE_HEIGHT = intPreferencesKey("image_processor.max_image_height")
    val MORPH_KERNEL_SIZE = doublePreferencesKey("image_processor.morph_kernel_size")
    val MORPH_ITERATIONS = intPreferencesKey("image_processor.morph_iterations")
    val GAUSSIAN_BLUR_SIGMA_X = doublePreferencesKey("image_processor.gaussian_blur_sigma_x")
    val GAUSSIAN_BLUR_KERNEL_SIZE = doublePreferencesKey("image_processor.gaussian_blur_kernel_size")
    val EDGE_DILATE_KERNEL_SIZE = doublePreferencesKey("image_processor.edge_dilate_kernel_size")
    val CANNY_LOWER_HYSTERESIS_THRESHOLD = doublePreferencesKey("image_processor.canny_lower_hysteresis_threshold")
    val CANNY_UPPER_HYSTERESIS_THRESHOLD = doublePreferencesKey("image_processor.canny_upper_hysteresis_threshold")
    val CONTOUR_COLOR = intPreferencesKey("image_processor.contour_color")
    val CONTOUR_THICKNESS = intPreferencesKey("image_processor.contour_thickness")
}

class SettingsDataStore(private val context: Context) {

    companion object {
        private val DEFAULT_VALUES = DefaultValues()
    }

    private data class DefaultValues(
        val outputStage: Int = DebugOutputStage.FINAL_OUTPUT.value,
        val maxImageHeight: Int = 1080,
        val morphKernelSize: Double = 10.0,
        val morphIterations: Int = 3,
        val gaussianBlurSigmaX: Double = 0.0,
        val gaussianBlurKernelSize: Double = 11.0,
        val edgeDilateKernelSize: Double = 2.0,
        val cannyLowerHysteresisThreshold: Double = 30.0,
        val cannyUpperHysteresisThreshold: Double = 150.0,
        val contourColor: Int = 0xFF00FF00.toInt(),
        val contourThickness: Int = 2
    )

    // method to return all settings as a dictionary in a single flow
    val allSettings: Flow<Map<String, Any>> = context.settingsDataStore.data
        .map { preferences ->
            mapOf(
                SettingKeys.OUTPUT_STAGE.name to (preferences[SettingKeys.OUTPUT_STAGE] ?: DEFAULT_VALUES.outputStage),
                SettingKeys.MAX_IMAGE_HEIGHT.name to (preferences[SettingKeys.MAX_IMAGE_HEIGHT] ?: DEFAULT_VALUES.maxImageHeight),
                SettingKeys.MORPH_KERNEL_SIZE.name to (preferences[SettingKeys.MORPH_KERNEL_SIZE] ?: DEFAULT_VALUES.morphKernelSize),
                SettingKeys.MORPH_ITERATIONS.name to (preferences[SettingKeys.MORPH_ITERATIONS] ?: DEFAULT_VALUES.morphIterations),
                SettingKeys.GAUSSIAN_BLUR_SIGMA_X.name to (preferences[SettingKeys.GAUSSIAN_BLUR_SIGMA_X] ?: DEFAULT_VALUES.gaussianBlurSigmaX),
                SettingKeys.GAUSSIAN_BLUR_KERNEL_SIZE.name to (preferences[SettingKeys.GAUSSIAN_BLUR_KERNEL_SIZE] ?: DEFAULT_VALUES.gaussianBlurKernelSize),
                SettingKeys.EDGE_DILATE_KERNEL_SIZE.name to (preferences[SettingKeys.EDGE_DILATE_KERNEL_SIZE] ?: DEFAULT_VALUES.edgeDilateKernelSize),
                SettingKeys.CANNY_LOWER_HYSTERESIS_THRESHOLD.name to (preferences[SettingKeys.CANNY_LOWER_HYSTERESIS_THRESHOLD] ?: DEFAULT_VALUES.cannyLowerHysteresisThreshold),
                SettingKeys.CANNY_UPPER_HYSTERESIS_THRESHOLD.name to (preferences[SettingKeys.CANNY_UPPER_HYSTERESIS_THRESHOLD] ?: DEFAULT_VALUES.cannyUpperHysteresisThreshold),
                SettingKeys.CONTOUR_COLOR.name to (preferences[SettingKeys.CONTOUR_COLOR] ?: DEFAULT_VALUES.contourColor),
                SettingKeys.CONTOUR_THICKNESS.name to (preferences[SettingKeys.CONTOUR_THICKNESS] ?: DEFAULT_VALUES.contourThickness)
            )
        }

    val outputStage: Flow<Int> = context.settingsDataStore.data
        .map { it[SettingKeys.OUTPUT_STAGE] ?: DEFAULT_VALUES.outputStage }

    suspend fun setOutputStage(value: Int) {
        context.settingsDataStore.edit { it[SettingKeys.OUTPUT_STAGE] = value
        }
    }

    val maxImageHeight: Flow<Int> = context.settingsDataStore.data
        .map { it[SettingKeys.MAX_IMAGE_HEIGHT] ?: DEFAULT_VALUES.maxImageHeight }

    suspend fun setMaxImageHeight(value: Int) {
        context.settingsDataStore.edit { it[SettingKeys.MAX_IMAGE_HEIGHT] = value }
    }

    val morphKernelSize: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.MORPH_KERNEL_SIZE] ?: DEFAULT_VALUES.morphKernelSize }

    suspend fun setMorphKernelSize(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.MORPH_KERNEL_SIZE] = value }
    }

    val morphIterations: Flow<Int> = context.settingsDataStore.data
        .map { it[SettingKeys.MORPH_ITERATIONS] ?: DEFAULT_VALUES.morphIterations }

    suspend fun setMorphIterations(value: Int) {
        context.settingsDataStore.edit { it[SettingKeys.MORPH_ITERATIONS] = value }
    }

    val gaussianBlurSigmaX: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.GAUSSIAN_BLUR_SIGMA_X] ?: DEFAULT_VALUES.gaussianBlurSigmaX }

    suspend fun setGaussianBlurSigmaX(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.GAUSSIAN_BLUR_SIGMA_X] = value }
    }

    val gaussianBlurKernelSize: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.GAUSSIAN_BLUR_KERNEL_SIZE] ?: DEFAULT_VALUES.gaussianBlurKernelSize }

    suspend fun setGaussianBlurKernelSize(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.GAUSSIAN_BLUR_KERNEL_SIZE] = value }
    }

    val edgeDilateKernelSize: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.EDGE_DILATE_KERNEL_SIZE] ?: DEFAULT_VALUES.edgeDilateKernelSize }

    suspend fun setEdgeDilateKernelSize(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.EDGE_DILATE_KERNEL_SIZE] = value }
    }

    val cannyLowerHysteresisThreshold: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.CANNY_LOWER_HYSTERESIS_THRESHOLD] ?: DEFAULT_VALUES.cannyLowerHysteresisThreshold }

    suspend fun setCannyLowerHysteresisThreshold(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.CANNY_LOWER_HYSTERESIS_THRESHOLD] = value }
    }

    val cannyUpperHysteresisThreshold: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.CANNY_UPPER_HYSTERESIS_THRESHOLD] ?: DEFAULT_VALUES.cannyUpperHysteresisThreshold }

    suspend fun setCannyUpperHysteresisThreshold(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.CANNY_UPPER_HYSTERESIS_THRESHOLD] = value }
    }

    val contourColor: Flow<Int> = context.settingsDataStore.data
        .map { it[SettingKeys.CONTOUR_COLOR] ?: DEFAULT_VALUES.contourColor }

    suspend fun setContourColor(value: Int) {
        context.settingsDataStore.edit { it[SettingKeys.CONTOUR_COLOR] = value }
    }

    val contourThickness: Flow<Int> = context.settingsDataStore.data
        .map { it[SettingKeys.CONTOUR_THICKNESS] ?: DEFAULT_VALUES.contourThickness }

    suspend fun setContourThickness(value: Int) {
        context.settingsDataStore.edit { it[SettingKeys.CONTOUR_THICKNESS] = value }
    }
}
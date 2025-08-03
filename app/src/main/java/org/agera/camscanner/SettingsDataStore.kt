package org.agera.camscanner

import android.content.Context
import androidx.core.graphics.toColorInt
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
    val CONTOUR_SELECTION_COUNT = intPreferencesKey("image_processor.contour_selection_count")
    val CONTOUR_COLOR = intPreferencesKey("image_processor.contour_color")
    val CONTOUR_THICKNESS = intPreferencesKey("image_processor.contour_thickness")
}

class SettingsDataStore(private val context: Context) {

    companion object {
        private val DEFAULT_VALUES = DefaultValues()
    }

    private data class DefaultValues(
        val outputStage: Int = DebugOutputStage.CORNERS_DETECTED.value,
        val maxImageHeight: Int = 1080,
        val morphKernelSize: Double = 10.0,
        val morphIterations: Int = 3,
        val gaussianBlurSigmaX: Double = 0.0,
        val gaussianBlurKernelSize: Double = 11.0,
        val edgeDilateKernelSize: Double = 2.0,
        val cannyLowerHysteresisThreshold: Double = 30.0,
        val cannyUpperHysteresisThreshold: Double = 150.0,
        val contourSelectionCount: Int = 5,
        val contourColor: Int = 0xFF00FF00.toInt(),
        val contourThickness: Int = 2
    )

    /**
     * Flow that emits a map of all settings.
     * The keys are the names of the settings, and the values are their corresponding values.
     */
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
                SettingKeys.CONTOUR_SELECTION_COUNT.name to (preferences[SettingKeys.CONTOUR_SELECTION_COUNT] ?: DEFAULT_VALUES.contourSelectionCount),
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

    suspend fun setOutputStage(value: String?) {
        var intValue = value?.trim()?.toIntOrNull()
        if (intValue == null || intValue !in DebugOutputStage.entries.map { it.value }) {
            intValue = DEFAULT_VALUES.outputStage
        }
        context.settingsDataStore.edit { it[SettingKeys.OUTPUT_STAGE] = intValue }
    }

    val maxImageHeight: Flow<Int> = context.settingsDataStore.data
        .map { it[SettingKeys.MAX_IMAGE_HEIGHT] ?: DEFAULT_VALUES.maxImageHeight }

    suspend fun setMaxImageHeight(value: Int) {
        context.settingsDataStore.edit { it[SettingKeys.MAX_IMAGE_HEIGHT] = value }
    }

    suspend fun setMaxImageHeight(value: String?) {
        val intValue = toIntOrDefault(value, DEFAULT_VALUES.maxImageHeight)
        context.settingsDataStore.edit { it[SettingKeys.MAX_IMAGE_HEIGHT] = intValue }
    }

    val morphKernelSize: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.MORPH_KERNEL_SIZE] ?: DEFAULT_VALUES.morphKernelSize }

    suspend fun setMorphKernelSize(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.MORPH_KERNEL_SIZE] = value }
    }

    suspend fun setMorphKernelSize(value: String?) {
        val doubleValue = toDoubleOrDefault(value, DEFAULT_VALUES.morphKernelSize)
        context.settingsDataStore.edit { it[SettingKeys.MORPH_KERNEL_SIZE] = doubleValue }
    }

    val morphIterations: Flow<Int> = context.settingsDataStore.data
        .map { it[SettingKeys.MORPH_ITERATIONS] ?: DEFAULT_VALUES.morphIterations }

    suspend fun setMorphIterations(value: Int) {
        context.settingsDataStore.edit { it[SettingKeys.MORPH_ITERATIONS] = value }
    }

    suspend fun setMorphIterations(value: String?) {
        val intValue = toIntOrDefault(value, DEFAULT_VALUES.morphIterations)
        context.settingsDataStore.edit { it[SettingKeys.MORPH_ITERATIONS] = intValue }
    }

    val gaussianBlurSigmaX: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.GAUSSIAN_BLUR_SIGMA_X] ?: DEFAULT_VALUES.gaussianBlurSigmaX }

    suspend fun setGaussianBlurSigmaX(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.GAUSSIAN_BLUR_SIGMA_X] = value }
    }

    suspend fun setGaussianBlurSigmaX(value: String?) {
        val doubleValue = toDoubleOrDefault(value, DEFAULT_VALUES.gaussianBlurSigmaX)
        context.settingsDataStore.edit { it[SettingKeys.GAUSSIAN_BLUR_SIGMA_X] = doubleValue }
    }

    val gaussianBlurKernelSize: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.GAUSSIAN_BLUR_KERNEL_SIZE] ?: DEFAULT_VALUES.gaussianBlurKernelSize }

    suspend fun setGaussianBlurKernelSize(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.GAUSSIAN_BLUR_KERNEL_SIZE] = value }
    }

    suspend fun setGaussianBlurKernelSize(value: String?) {
        val doubleValue = toDoubleOrDefault(value, DEFAULT_VALUES.gaussianBlurKernelSize)
        context.settingsDataStore.edit { it[SettingKeys.GAUSSIAN_BLUR_KERNEL_SIZE] = doubleValue }
    }

    val edgeDilateKernelSize: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.EDGE_DILATE_KERNEL_SIZE] ?: DEFAULT_VALUES.edgeDilateKernelSize }

    suspend fun setEdgeDilateKernelSize(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.EDGE_DILATE_KERNEL_SIZE] = value }
    }

    suspend fun setEdgeDilateKernelSize(value: String?) {
        val doubleValue = toDoubleOrDefault(value, DEFAULT_VALUES.edgeDilateKernelSize)
        context.settingsDataStore.edit { it[SettingKeys.EDGE_DILATE_KERNEL_SIZE] = doubleValue }
    }

    val cannyLowerHysteresisThreshold: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.CANNY_LOWER_HYSTERESIS_THRESHOLD] ?: DEFAULT_VALUES.cannyLowerHysteresisThreshold }

    suspend fun setCannyLowerHysteresisThreshold(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.CANNY_LOWER_HYSTERESIS_THRESHOLD] = value }
    }

    suspend fun setCannyLowerHysteresisThreshold(value: String?) {
        val doubleValue = toDoubleOrDefault(value, DEFAULT_VALUES.cannyLowerHysteresisThreshold)
        context.settingsDataStore.edit { it[SettingKeys.CANNY_LOWER_HYSTERESIS_THRESHOLD] = doubleValue }
    }

    val cannyUpperHysteresisThreshold: Flow<Double> = context.settingsDataStore.data
        .map { it[SettingKeys.CANNY_UPPER_HYSTERESIS_THRESHOLD] ?: DEFAULT_VALUES.cannyUpperHysteresisThreshold }

    suspend fun setCannyUpperHysteresisThreshold(value: Double) {
        context.settingsDataStore.edit { it[SettingKeys.CANNY_UPPER_HYSTERESIS_THRESHOLD] = value }
    }

    suspend fun setCannyUpperHysteresisThreshold(value: String?) {
        val doubleValue = toDoubleOrDefault(value, DEFAULT_VALUES.cannyUpperHysteresisThreshold)
        context.settingsDataStore.edit { it[SettingKeys.CANNY_UPPER_HYSTERESIS_THRESHOLD] = doubleValue }
    }

    val contourSelectionCount: Flow<Int> = context.settingsDataStore.data
        .map { it[SettingKeys.CONTOUR_SELECTION_COUNT] ?: DEFAULT_VALUES.contourSelectionCount }

    suspend fun setContourSelectionCount(value: Int) {
        context.settingsDataStore.edit { it[SettingKeys.CONTOUR_SELECTION_COUNT] = value }
    }

    suspend fun setContourSelectionCount(value: String?) {
        val intValue = toIntOrDefault(value, DEFAULT_VALUES.contourSelectionCount)
        context.settingsDataStore.edit { it[SettingKeys.CONTOUR_SELECTION_COUNT] = intValue }
    }

    val contourColor: Flow<Int> = context.settingsDataStore.data
        .map { it[SettingKeys.CONTOUR_COLOR] ?: DEFAULT_VALUES.contourColor }

    suspend fun setContourColor(value: Int) {
        context.settingsDataStore.edit { it[SettingKeys.CONTOUR_COLOR] = value }
    }

    suspend fun setContourColor(value: String?) {
        var intValue: Int?

        try {
            intValue = value?.trim()?.toColorInt()
        } catch (e: IllegalArgumentException) {
            intValue = null
        }

        if (intValue == null) {
            intValue = DEFAULT_VALUES.contourColor
        }
        context.settingsDataStore.edit { it[SettingKeys.CONTOUR_COLOR] = intValue }
    }

    val contourThickness: Flow<Int> = context.settingsDataStore.data
        .map { it[SettingKeys.CONTOUR_THICKNESS] ?: DEFAULT_VALUES.contourThickness }

    suspend fun setContourThickness(value: Int) {
        context.settingsDataStore.edit { it[SettingKeys.CONTOUR_THICKNESS] = value }
    }

    suspend fun setContourThickness(value: String?) {
        val intValue = toIntOrDefault(value, DEFAULT_VALUES.contourThickness)
        context.settingsDataStore.edit { it[SettingKeys.CONTOUR_THICKNESS] = intValue }
    }

    private fun toIntOrDefault(value: String?, default: Int): Int {
        return value?.trim()?.toIntOrNull() ?: default
    }

    private fun toDoubleOrDefault(value: String?, default: Double): Double {
        return value?.trim()?.toDoubleOrNull() ?: default
    }
}
package org.agera.camscanner

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settings = SettingsDataStore(this)

        val outputStageEdit = findViewById<EditText>(R.id.editOutputStage)
        val maxImageHeightEdit = findViewById<EditText>(R.id.editMaxProcessingImageHeight)
        val morphKernelSizeEdit = findViewById<EditText>(R.id.editMorphologyKernelSize)
        val morphIterationsEdit = findViewById<EditText>(R.id.editMorphologyIterations)
        val gaussianBlurSigmaXEdit = findViewById<EditText>(R.id.editGaussianBlurSigmaX)
        val gaussianBlurKernelSizeEdit = findViewById<EditText>(R.id.editGaussianBlurKernelSize)
        val edgeDilateKernelSizeEdit = findViewById<EditText>(R.id.editDilationKernelSize)
        val cannyLowerHysteresisThresholdEdit = findViewById<EditText>(R.id.editCannyLowerHysteresisThreshold)
        val cannyUpperHysteresisThresholdEdit = findViewById<EditText>(R.id.editCannyUpperHysteresisThreshold)
        val contourSelectionCountEdit = findViewById<EditText>(R.id.editContourSelectionCount)
        val contourColorEdit = findViewById<EditText>(R.id.editContourColor)
        val contourThicknessEdit = findViewById<EditText>(R.id.editContourThickness)
        val saveButton = findViewById<Button>(R.id.saveSettingsButton)

        saveButton.setOnClickListener {
            finish()
        }

        // React on a changed value by user input and update the settings
        outputStageEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setOutputStage(outputStageEdit.text.toString()) }
            }
        }

        maxImageHeightEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setMaxImageHeight(maxImageHeightEdit.text.toString()) }
            }
        }

        morphKernelSizeEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setMorphKernelSize(morphKernelSizeEdit.text.toString()) }
            }
        }

        morphIterationsEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setMorphIterations(morphIterationsEdit.text.toString()) }
            }
        }

        gaussianBlurSigmaXEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setGaussianBlurSigmaX(gaussianBlurSigmaXEdit.text.toString()) }
            }
        }

        gaussianBlurKernelSizeEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setGaussianBlurKernelSize(gaussianBlurKernelSizeEdit.text.toString()) }
            }
        }

        edgeDilateKernelSizeEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setEdgeDilateKernelSize(edgeDilateKernelSizeEdit.text.toString()) }
            }
        }

        cannyLowerHysteresisThresholdEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setCannyLowerHysteresisThreshold(cannyLowerHysteresisThresholdEdit.text.toString()) }
            }
        }

        cannyUpperHysteresisThresholdEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setCannyUpperHysteresisThreshold(cannyUpperHysteresisThresholdEdit.text.toString()) }
            }
        }

        contourSelectionCountEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setContourSelectionCount(contourSelectionCountEdit.text.toString()) }
            }
        }

        contourColorEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setContourColor(contourColorEdit.text.toString()) }
            }
        }

        contourThicknessEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                lifecycleScope.launch { settings.setContourThickness(contourThicknessEdit.text.toString()) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settings.allSettings.collectLatest { settingsMap ->
                    settingsMap[SettingKeys.OUTPUT_STAGE.name]?.let { outputStageEdit.setText(it.toString()) }
                    settingsMap[SettingKeys.MAX_IMAGE_HEIGHT.name]?.let { maxImageHeightEdit.setText(it.toString()) }
                    settingsMap[SettingKeys.MORPH_KERNEL_SIZE.name]?.let { morphKernelSizeEdit.setText(it.toString()) }
                    settingsMap[SettingKeys.MORPH_ITERATIONS.name]?.let { morphIterationsEdit.setText(it.toString()) }
                    settingsMap[SettingKeys.GAUSSIAN_BLUR_SIGMA_X.name]?.let { gaussianBlurSigmaXEdit.setText(it.toString()) }
                    settingsMap[SettingKeys.GAUSSIAN_BLUR_KERNEL_SIZE.name]?.let { gaussianBlurKernelSizeEdit.setText(it.toString()) }
                    settingsMap[SettingKeys.EDGE_DILATE_KERNEL_SIZE.name]?.let { edgeDilateKernelSizeEdit.setText(it.toString()) }
                    settingsMap[SettingKeys.CANNY_LOWER_HYSTERESIS_THRESHOLD.name]?.let { cannyLowerHysteresisThresholdEdit.setText(it.toString()) }
                    settingsMap[SettingKeys.CANNY_UPPER_HYSTERESIS_THRESHOLD.name]?.let { cannyUpperHysteresisThresholdEdit.setText(it.toString()) }
                    settingsMap[SettingKeys.CONTOUR_SELECTION_COUNT.name]?.let { contourSelectionCountEdit.setText(it.toString()) }
                    settingsMap[SettingKeys.CONTOUR_COLOR.name]?.let { contourColorEdit.setText(intToArgbString(it as Int)) }
                    settingsMap[SettingKeys.CONTOUR_THICKNESS.name]?.let { contourThicknessEdit.setText(it.toString()) }
                }
            }
        }
    }

    private fun intToArgbString(value: Int): String {
        return String.format("#%08X", value)
    }
}
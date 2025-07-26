package org.agera.camscanner

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.zip
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
        val contourColorEdit = findViewById<EditText>(R.id.editContourColor)
        val contourThicknessEdit = findViewById<EditText>(R.id.editContourThickness)

        lifecycleScope.launch {
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
                settingsMap[SettingKeys.CONTOUR_COLOR.name]?.let { contourColorEdit.setText(intToArgbString(it as Int)) }
                settingsMap[SettingKeys.CONTOUR_THICKNESS.name]?.let { contourThicknessEdit.setText(it.toString()) }
            }
        }

//        lifecycleScope.launch {
//            settings.outputStage.collectLatest { outputStageEdit.setText(it.toString()) }
//            settings.maxImageHeight.collectLatest { maxImageHeightEdit.setText(it.toString()) }
//            settings.morphKernelSize.collectLatest { morphKernelSizeEdit.setText(it.toString()) }
//            settings.morphIterations.collectLatest { morphIterationsEdit.setText(it.toString()) }
//            settings.gaussianBlurSigmaX.collectLatest { gaussianBlurSigmaXEdit.setText(it.toString()) }
//            settings.gaussianBlurKernelSize.collectLatest { gaussianBlurKernelSizeEdit.setText(it.toString()) }
//            settings.edgeDilateKernelSize.collectLatest { edgeDilateKernelSizeEdit.setText(it.toString()) }
//            settings.cannyLowerHysteresisThreshold.collectLatest { cannyLowerHysteresisThresholdEdit.setText(it.toString()) }
//            settings.cannyUpperHysteresisThreshold.collectLatest { cannyUpperHysteresisThresholdEdit.setText(it.toString()) }
//            settings.contourColor.collectLatest { contourColorEdit.setText(intToArgbString(it.toInt())) }
//            settings.contourThickness.collectLatest { contourThicknessEdit.setText(it.toString()) }
//        }

        val saveButton = findViewById<Button>(R.id.saveSettingsButton)
        saveButton.setOnClickListener {
            lifecycleScope.launch {
                settings.setOutputStage(outputStageEdit.text.toString().toIntOrNull() ?: DebugOutputStage.FINAL_OUTPUT.value)
            };
            finish()
        }
    }

    private fun intToArgbString(value: Int): String {
        return String.format("#%08X", value)
    }
}
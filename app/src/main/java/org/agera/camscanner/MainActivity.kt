package org.agera.camscanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {

    private lateinit var startCameraButton: Button;
    private lateinit var settingsButton: Button;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.startCameraButton = findViewById<Button>(R.id.openCameraButton);
        this.settingsButton = findViewById<Button>(R.id.openSettingsButton);

        this.startCameraButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
        this.settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }
}
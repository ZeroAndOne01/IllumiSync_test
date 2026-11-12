package com.example.illumisync_test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Switch

class LightActivity : AppCompatActivity() {

    private lateinit var masterLightSwitch: Switch
    private lateinit var lightSeekBar: SeekBar
    private lateinit var lightBrightnessText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the layout FIRST
        setContentView(R.layout.activity_lights)

        // Highlight Lights navigation
        findViewById<LinearLayout>(R.id.navLight).isSelected = true

        // Initialize views
        masterLightSwitch = findViewById(R.id.masterLightSwitch)
        lightSeekBar = findViewById(R.id.lightSeekBar)
        lightBrightnessText = findViewById(R.id.lightBrightnessText)

        setupLightControls()
        setupNavigation()
    }

    private fun setupLightControls() {
        lightSeekBar.max = 100
        lightSeekBar.progress = 75
        masterLightSwitch.text = "Master Light Control: ON"
        masterLightSwitch.isChecked = true
        lightBrightnessText.text = "Brightness: 75%"

        masterLightSwitch.setOnCheckedChangeListener { button, isChecked ->
            button.text = if (isChecked) {
                "Master Light Control: ON"
            } else {
                "Master Light Control: OFF"
            }

            lightBrightnessText.text = if (isChecked) {
                "Brightness: ${lightSeekBar.progress}%"
            } else {
                "Lights are OFF"
            }
        }

        lightSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    lightBrightnessText.text = if (masterLightSwitch.isChecked) {
                        "Brightness: $progress%"
                    } else {
                        "Lights are OFF"
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        findViewById<Button>(R.id.focusButton).setOnClickListener {
            setBrightness(80)
        }

        findViewById<Button>(R.id.relaxButton).setOnClickListener {
            setBrightness(50)
        }

        findViewById<Button>(R.id.movieButton).setOnClickListener {
            setBrightness(20)
        }

        findViewById<Button>(R.id.sleepButton).setOnClickListener {
            setBrightness(5)
        }

        findViewById<Button>(R.id.ambientScheduleButton)
            .setOnClickListener {
                lightBrightnessText.text = "Target Lux: 300"
            }

        findViewById<Button>(R.id.gradualDimmingButton)
            .setOnClickListener {
                lightBrightnessText.text = "Transition: 30 minutes"
            }
    }

    private fun setBrightness(value: Int) {
        masterLightSwitch.isChecked = true
        lightSeekBar.progress = value
        lightBrightnessText.text = "Brightness: $value%"
    }


    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            PageNavigation.open(this, MainActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.navControl).setOnClickListener {
            PageNavigation.open(this, ControlActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.navSchedule).setOnClickListener {
            PageNavigation.open(this, ScheduleActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.navLight).setOnClickListener {
            // Already on Lights
        }

        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            PageNavigation.open(this, SettingsActivity::class.java)
        }
    }


    private fun openPage(destination: Class<*>) {
        startActivity(Intent(this, destination))
        finish()
    }
}
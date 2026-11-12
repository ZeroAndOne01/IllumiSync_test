
package com.example.illumisync_test

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var motorSpeedSeekBar: SeekBar
    private lateinit var motorSpeedText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        motorSpeedSeekBar =
            findViewById(R.id.motorSpeedSeekBar)

        motorSpeedText =
            findViewById(R.id.motorSpeedText)

        // Highlight the Settings navigation item.
        findViewById<LinearLayout>(R.id.navSettings).isSelected = true

        setupSettings()
        setupNavigation()
    }

    private fun setupSettings() {
        motorSpeedSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    motorSpeedText.text = "Motor Speed: $progress%"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        findViewById<Button>(R.id.clearSchedulesButton)
            .setOnClickListener {
                // Clear schedules function
            }

        findViewById<Button>(R.id.timezoneButton)
            .setOnClickListener {
                // Timezone function
            }
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
            PageNavigation.open(this, LightActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            // Already on Settings.
        }
    }
}


package com.example.illumisync_test

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

data class LightSchedule(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var startTime: String = "07:00 AM",
    var endTime: String = "08:00 AM",
    var brightness: Int = 75,
    var days: MutableList<String> = mutableListOf(),
    var enabled: Boolean = true
)

class ScheduleActivity : AppCompatActivity() {

    private lateinit var scheduleContainer: LinearLayout
    private lateinit var emptySchedulesText: TextView

    private val schedules = mutableListOf<LightSchedule>()

    private val dayNames = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday",
        "Friday", "Saturday", "Sunday"
    )

    private val dayCheckBoxIds = listOf(
        R.id.dayMon,
        R.id.dayTue,
        R.id.dayWed,
        R.id.dayThu,
        R.id.dayFri,
        R.id.daySat,
        R.id.daySun
    )

    private val prefs by lazy {
        getSharedPreferences(
            "illumisync_schedules",
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        scheduleContainer = findViewById(R.id.scheduleContainer)
        emptySchedulesText = findViewById(R.id.emptySchedulesText)

        // Highlight the current bottom navigation item.
        findViewById<LinearLayout>(R.id.navSchedule).isSelected = true

        loadSchedules()
        renderSchedules()

        findViewById<Button>(R.id.addScheduleButton)
            .setOnClickListener {
                showScheduleDialog(null)
            }

        setupNavigation()
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome)
            .setOnClickListener {
                PageNavigation.open(this, MainActivity::class.java)
            }

        findViewById<LinearLayout>(R.id.navControl)
            .setOnClickListener {
                PageNavigation.open(this, ControlActivity::class.java)
            }

        findViewById<LinearLayout>(R.id.navSchedule)
            .setOnClickListener {
                // Already on the Schedule page.
            }

        findViewById<LinearLayout>(R.id.navSettings)
            .setOnClickListener {
                PageNavigation.open(this, SettingsActivity::class.java)
            }
    }

    // Load saved schedules from local storage.
    private fun loadSchedules() {
        schedules.clear()

        val savedJson =
            prefs.getString("schedule_list", "[]") ?: "[]"

        try {
            val array = JSONArray(savedJson)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val savedDays =
                    obj.optJSONArray("days") ?: JSONArray()

                val days = mutableListOf<String>()

                for (j in 0 until savedDays.length()) {
                    days.add(savedDays.getString(j))
                }

                schedules.add(
                    LightSchedule(
                        id = obj.optString(
                            "id",
                            UUID.randomUUID().toString()
                        ),
                        name = obj.optString("name", "My Schedule"),
                        startTime = obj.optString(
                            "startTime",
                            "07:00 AM"
                        ),
                        endTime = obj.optString(
                            "endTime",
                            "08:00 AM"
                        ),
                        brightness = obj.optInt(
                            "brightness",
                            75
                        ).coerceIn(0, 100),
                        days = days,
                        enabled = obj.optBoolean("enabled", true)
                    )
                )
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Could not read saved schedules.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Save every schedule so changes remain after closing the app.
    private fun saveSchedules() {
        val array = JSONArray()

        schedules.forEach { schedule ->
            val daysArray = JSONArray()
            schedule.days.forEach { daysArray.put(it) }

            val obj = JSONObject().apply {
                put("id", schedule.id)
                put("name", schedule.name)
                put("startTime", schedule.startTime)
                put("endTime", schedule.endTime)
                put("brightness", schedule.brightness)
                put("days", daysArray)
                put("enabled", schedule.enabled)
            }

            array.put(obj)
        }

        prefs.edit()
            .putString("schedule_list", array.toString())
            .apply()
    }

    // Display the schedule cards.
    private fun renderSchedules() {
        scheduleContainer.removeAllViews()

        emptySchedulesText.visibility =
            if (schedules.isEmpty()) View.VISIBLE else View.GONE

        schedules.forEach { schedule ->

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 14, 16, 14)

                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = 16f
                    setStroke(
                        1,
                        Color.rgb(230, 230, 230)
                    )
                }

                isClickable = true
                isFocusable = true
            }

            val title = TextView(this).apply {
                text = schedule.name
                textSize = 18f
                setTextColor(Color.rgb(32, 33, 36))
                setTypeface(null, Typeface.BOLD)
            }

            val repeatText = if (schedule.days.isEmpty()) {
                "No days selected"
            } else {
                schedule.days.joinToString(", ")
            }

            val details = TextView(this).apply {
                text = "${schedule.startTime} – ${schedule.endTime}\n" +
                        "Brightness: ${schedule.brightness}%\n" +
                        "Repeat: $repeatText"

                textSize = 14f
                setTextColor(Color.rgb(110, 110, 110))
                setPadding(0, 8, 0, 8)
            }

            val enabledCheckBox = CheckBox(this).apply {
                text = if (schedule.enabled) {
                    "Schedule enabled"
                } else {
                    "Schedule disabled"
                }

                setTextColor(Color.rgb(32, 33, 36))
                isChecked = schedule.enabled

                setOnCheckedChangeListener { _, checked ->
                    schedule.enabled = checked
                    saveSchedules()
                    renderSchedules()
                }
            }

            val buttons = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.END
            }

            val editButton = Button(this).apply {
                text = "Edit"
                setOnClickListener {
                    showScheduleDialog(schedule)
                }
            }

            val deleteButton = Button(this).apply {
                text = "Delete"
                setOnClickListener {
                    confirmDelete(schedule)
                }
            }

            buttons.addView(editButton)
            buttons.addView(deleteButton)

            card.addView(title)
            card.addView(details)
            card.addView(enabledCheckBox)
            card.addView(buttons)

            // Tapping the card also opens its editor.
            card.setOnClickListener {
                showScheduleDialog(schedule)
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 14)
            }

            scheduleContainer.addView(card, params)
        }
    }

    // Show the same popup for creating or editing a schedule.
    private fun showScheduleDialog(existing: LightSchedule?) {
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_schedule,
            null
        )

        val nameInput =
            dialogView.findViewById<EditText>(
                R.id.scheduleNameInput
            )

        val startInput =
            dialogView.findViewById<TextView>(
                R.id.startTimeInput
            )

        val endInput =
            dialogView.findViewById<TextView>(
                R.id.endTimeInput
            )

        val brightnessSeekBar =
            dialogView.findViewById<SeekBar>(
                R.id.brightnessSeekBar
            )

        val brightnessValue =
            dialogView.findViewById<TextView>(
                R.id.brightnessValue
            )

        val dayCheckBoxes = dayCheckBoxIds.map { id ->
            dialogView.findViewById<CheckBox>(id)
        }

        // Populate the fields with existing values when editing.
        nameInput.setText(existing?.name ?: "")
        startInput.text = existing?.startTime ?: "07:00 AM"
        endInput.text = existing?.endTime ?: "08:00 AM"

        brightnessSeekBar.progress =
            existing?.brightness ?: 75

        brightnessValue.text =
            "Brightness: ${brightnessSeekBar.progress}%"

        dayCheckBoxes.forEachIndexed { index, checkBox ->
            checkBox.isChecked =
                existing?.days?.contains(dayNames[index]) ?: false
        }

        brightnessSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    brightnessValue.text =
                        "Brightness: $progress%"
                }

                override fun onStartTrackingTouch(
                    seekBar: SeekBar?
                ) {}

                override fun onStopTrackingTouch(
                    seekBar: SeekBar?
                ) {}
            }
        )

        startInput.setOnClickListener {
            showTimePicker(startInput)
        }

        endInput.setOnClickListener {
            showTimePicker(endInput)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(
                if (existing == null) {
                    "Add Schedule"
                } else {
                    "Edit Schedule"
                }
            )
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val name = nameInput.text.toString().trim()

                if (name.isEmpty()) {
                    nameInput.error = "Enter a schedule name"
                    return@setOnClickListener
                }

                val selectedDays =
                    dayCheckBoxes.mapIndexedNotNull { index, checkBox ->
                        if (checkBox.isChecked) {
                            dayNames[index]
                        } else {
                            null
                        }
                    }.toMutableList()

                if (existing == null) {
                    // Create a new schedule.
                    schedules.add(
                        LightSchedule(
                            name = name,
                            startTime = startInput.text.toString(),
                            endTime = endInput.text.toString(),
                            brightness = brightnessSeekBar.progress,
                            days = selectedDays
                        )
                    )
                } else {
                    // Update the existing schedule, preserving its ID.
                    val index = schedules.indexOfFirst {
                        it.id == existing.id
                    }

                    if (index != -1) {
                        schedules[index].apply {
                            this.name = name
                            startTime = startInput.text.toString()
                            endTime = endInput.text.toString()
                            brightness = brightnessSeekBar.progress
                            days = selectedDays
                        }
                    }
                }

                saveSchedules()
                renderSchedules()
                dialog.dismiss()

                Toast.makeText(
                    this,
                    "Schedule saved",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialog.show()
    }

    private fun showTimePicker(target: TextView) {
        val calendar = Calendar.getInstance()

        val parsed = try {
            SimpleDateFormat(
                "hh:mm a",
                Locale.US
            ).parse(target.text.toString())
        } catch (e: Exception) {
            null
        }

        if (parsed != null) {
            calendar.time = parsed
        }

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val displayTime = String.format(
                    Locale.US,
                    "%02d:%02d %s",
                    if (hourOfDay % 12 == 0) {
                        12
                    } else {
                        hourOfDay % 12
                    },
                    minute,
                    if (hourOfDay < 12) "AM" else "PM"
                )

                target.text = displayTime
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun confirmDelete(schedule: LightSchedule) {
        AlertDialog.Builder(this)
            .setTitle("Delete schedule?")
            .setMessage("Delete \"${schedule.name}\"?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                schedules.removeAll {
                    it.id == schedule.id
                }

                saveSchedules()
                renderSchedules()

                Toast.makeText(
                    this,
                    "Schedule deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }
}

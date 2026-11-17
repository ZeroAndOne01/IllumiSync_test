package com.example.illumisync_test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dateText = findViewById<TextView>(R.id.dateText)
        dateText.text = SimpleDateFormat(
            "EEEE, MMMM d, yyyy",
            Locale.getDefault()
        ).format(Date())

        setupNavigation()
        animatePageContent()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        animatePageContent()
    }

    override fun onResume() {
        super.onResume()

        // Replays the entrance animation when returning to Home.
        if (isTaskRoot) {
            animatePageContent()
        }
    }

    private fun animatePageContent() {
        val page = findViewById<View>(R.id.pageContent)

        page.animate().cancel()
        page.alpha = 0f
        page.translationX = 70f

        page.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(400L)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).isSelected = true

        findViewById<LinearLayout>(R.id.navHome)
            .setOnClickListener { }

        findViewById<LinearLayout>(R.id.navControl).setOnClickListener {
            navigateTo(ControlActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.navSchedule).setOnClickListener {
            navigateTo(ScheduleActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            navigateTo(SettingsActivity::class.java)
        }
    }

    private fun navigateTo(destination: Class<*>) {
        startActivity(Intent(this, destination))
        overridePendingTransition(0, 0)
    }
}
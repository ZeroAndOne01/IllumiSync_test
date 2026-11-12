
package com.example.illumisync_test

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ControlActivity : AppCompatActivity() {

    private lateinit var upButton: Button
    private lateinit var downButton: Button
    private lateinit var pwmSeekBar: SeekBar
    private lateinit var pwmValueText: TextView
    private lateinit var setPwmButton: Button
    private lateinit var blindSeekBar: SeekBar
    private lateinit var blindValueText: TextView
    private lateinit var statusText: TextView

    private lateinit var autoButton: Button
    private lateinit var manualButton: Button
    private lateinit var autoControls: LinearLayout
    private lateinit var manualControls: LinearLayout
    private lateinit var modeIndicator: View

    private lateinit var blindPositionRing: ProgressBar
    private lateinit var blindPercentText: TextView

    private val orange = Color.rgb(255, 75, 25)
    private val white = Color.WHITE
    private val darkText = Color.rgb(65, 70, 80)
    private val animationDuration = 220L
    private val pageTransitionDuration = 400L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        findViewById<LinearLayout>(R.id.navControl).isSelected = true

        upButton = findViewById(R.id.upButton)
        downButton = findViewById(R.id.downButton)
        pwmSeekBar = findViewById(R.id.pwmSeekBar)
        pwmValueText = findViewById(R.id.pwmValueText)
        setPwmButton = findViewById(R.id.setPwmButton)
        blindSeekBar = findViewById(R.id.blindSeekBar)
        blindValueText = findViewById(R.id.blindValueText)
        statusText = findViewById(R.id.statusText)

        autoButton = findViewById(R.id.autoButton)
        manualButton = findViewById(R.id.manualButton)
        autoControls = findViewById(R.id.autoControls)
        manualControls = findViewById(R.id.manualControls)

        modeIndicator = findViewById(R.id.modeIndicator)
        blindPositionRing = findViewById(R.id.blindPositionRing)
        blindPercentText = findViewById(R.id.blindPercentText)

        setupStepperButtons()
        setupPwmControls()
        setupBlindControls()
        setupModeButtons()
        setupButtonPressAnimations()
        setupNavigation()

        selectMode(isAuto = false, animate = false)

        // Animate only the page content, not the bottom navigation.
        val pageContent = findViewById<View>(R.id.pageContent)

        pageContent.alpha = 0f
        pageContent.translationX = 70f

        pageContent.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(pageTransitionDuration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun setupStepperButtons() {
        upButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animatePress(view, true)
                    statusText.text = "Raising blinds..."
                    statusText.setTextColor(Color.rgb(46, 173, 114))
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    animatePress(view, false)
                    statusText.text = "Blind movement stopped"
                    statusText.setTextColor(Color.GRAY)
                }
            }
            true
        }

        downButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animatePress(view, true)
                    statusText.text = "Lowering blinds..."
                    statusText.setTextColor(Color.rgb(46, 173, 114))
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    animatePress(view, false)
                    statusText.text = "Blind movement stopped"
                    statusText.setTextColor(Color.GRAY)
                }
            }
            true
        }
    }

    private fun setupPwmControls() {
        pwmSeekBar.max = 255
        pwmSeekBar.progress = 128
        pwmValueText.text = "PWM: 128"

        pwmSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    pwmValueText.text = "PWM: $progress"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )

        setPwmButton.setOnClickListener {
            statusText.text = "PWM set to ${pwmSeekBar.progress}"
            statusText.setTextColor(Color.rgb(46, 173, 114))
            animatePress(setPwmButton, false)
        }
    }

    private fun setupBlindControls() {
        blindSeekBar.max = 100
        blindSeekBar.progress = 50
        blindValueText.text = "Blind Position: 50%"
        blindPercentText.text = "50%"
        blindPositionRing.max = 100
        blindPositionRing.progress = 50

        blindSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    blindValueText.text = "Blind Position: $progress%"
                    blindPercentText.text = "$progress%"
                    blindPositionRing.setProgress(progress, true)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    statusText.text =
                        "Position selected: ${blindSeekBar.progress}%"
                }
            }
        )
    }

    private fun setupModeButtons() {
        autoControls.visibility = View.GONE
        manualControls.visibility = View.VISIBLE

        autoButton.setOnClickListener {
            selectMode(isAuto = true, animate = true)
        }

        manualButton.setOnClickListener {
            selectMode(isAuto = false, animate = true)
        }

        findViewById<Button>(R.id.presentationButton).setOnClickListener {
            statusText.text = "Presentation mode selected"
            statusText.setTextColor(orange)
        }

        findViewById<Button>(R.id.sunsetButton).setOnClickListener {
            statusText.text = "Sunset mode selected"
            statusText.setTextColor(orange)
        }
    }

    private fun selectMode(isAuto: Boolean, animate: Boolean) {
        val selectedButton = if (isAuto) autoButton else manualButton
        val unselectedButton = if (isAuto) manualButton else autoButton
        val switchTrack = modeIndicator.parent as? FrameLayout

        switchTrack?.post {
            val availableWidth =
                switchTrack.width -
                        switchTrack.paddingLeft -
                        switchTrack.paddingRight

            val indicatorWidth = availableWidth / 2
            val params = modeIndicator.layoutParams

            if (params.width != indicatorWidth) {
                params.width = indicatorWidth
                modeIndicator.layoutParams = params
            }

            val targetX = if (isAuto) 0f else indicatorWidth.toFloat()

            if (animate) {
                modeIndicator.animate()
                    .translationX(targetX)
                    .setDuration(animationDuration)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            } else {
                modeIndicator.translationX = targetX
            }
        }

        animateTextColor(selectedButton, if (animate) darkText else white, white)
        animateTextColor(unselectedButton, if (animate) white else darkText, darkText)

        if (isAuto) {
            slidePanelOut(manualControls) {
                slidePanelIn(autoControls)
            }
            statusText.text = "Automatic mode selected"
        } else {
            slidePanelOut(autoControls) {
                slidePanelIn(manualControls)
            }
            statusText.text = "Manual mode selected"
        }

        statusText.setTextColor(Color.rgb(46, 173, 114))
    }

    private fun slidePanelOut(panel: View, afterAnimation: () -> Unit) {
        if (panel.visibility != View.VISIBLE) {
            afterAnimation()
            return
        }

        panel.animate().cancel()
        panel.animate()
            .alpha(0f)
            .translationX(-30f)
            .setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                panel.visibility = View.GONE
                panel.alpha = 1f
                panel.translationX = 0f
                afterAnimation()
            }
            .start()
    }

    private fun slidePanelIn(panel: View) {
        panel.animate().cancel()
        panel.visibility = View.VISIBLE
        panel.alpha = 0f
        panel.translationX = 30f

        panel.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(230)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun animateTextColor(
        button: Button,
        startColor: Int,
        endColor: Int
    ) {
        val animator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            startColor,
            endColor
        )
        animator.duration = animationDuration
        animator.addUpdateListener {
            button.setTextColor(it.animatedValue as Int)
        }
        animator.start()
    }

    private fun setupButtonPressAnimations() {
        val buttonIds = intArrayOf(
            R.id.autoButton,
            R.id.manualButton,
            R.id.presentationButton,
            R.id.sunsetButton,
            R.id.setPwmButton
        )

        buttonIds.forEach { id ->
            val button = findViewById<Button>(id)

            button.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> animatePress(view, true)
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> animatePress(view, false)
                }
                false
            }
        }
    }

    private fun animatePress(view: View, pressed: Boolean) {
        view.animate().cancel()
        view.animate()
            .scaleX(if (pressed) 0.96f else 1f)
            .scaleY(if (pressed) 0.96f else 1f)
            .alpha(if (pressed) 0.78f else 1f)
            .setDuration(if (pressed) 80L else 150L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            navigateWithTransition(MainActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.navControl).setOnClickListener {
            statusText.text = "Control screen"
        }

        findViewById<LinearLayout>(R.id.navSchedule).setOnClickListener {
            navigateWithTransition(ScheduleActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.navLight).setOnClickListener {
            navigateWithTransition(LightActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            navigateWithTransition(SettingsActivity::class.java)
        }
    }

    private fun navigateWithTransition(destination: Class<*>) {
        val pageContent = findViewById<View>(R.id.pageContent)

        // Fade and slide the current page away before navigating.
        pageContent.animate().cancel()
        pageContent.animate()
            .alpha(0f)
            .translationX(-70f)
            .setDuration(300L)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                val intent = Intent(this, destination)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
            .start()
    }
}

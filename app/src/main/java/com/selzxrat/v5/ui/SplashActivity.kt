package com.selzxrat.v5.ui

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.selzxrat.v5.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.splashLogo)
        val title = findViewById<TextView>(R.id.splashTitle)
        val subtitle = findViewById<TextView>(R.id.splashSubtitle)

        // Fade in animation
        logo.alpha = 0f
        title.alpha = 0f
        subtitle.alpha = 0f

        logo.animate().alpha(1f).duration = 1000
        title.animate().alpha(1f).duration = 1500
        subtitle.animate().alpha(1f).duration = 2000

        // Pulse animation on logo
        val pulse = ValueAnimator.ofFloat(1f, 1.1f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                logo.scaleX = it.animatedValue as Float
                logo.scaleY = it.animatedValue as Float
            }
        }
        pulse.start()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }
}
package com.example.fc_006

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fc_006.data.Asteroid

class MainActivity : AppCompatActivity() {

    private lateinit var scanButton: Button
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var asteroidDataText: TextView
    
    private val viewModel: AsteroidViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        scanButton = findViewById(R.id.scanButton)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        asteroidDataText = findViewById(R.id.asteroidDataText)

        scanButton.setOnClickListener {
            viewModel.scanForAsteroids()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is AsteroidUiState.Loading -> {
                    loadingOverlay.visibility = View.VISIBLE
                    scanButton.isEnabled = false
                }
                is AsteroidUiState.Success -> {
                    loadingOverlay.visibility = View.GONE
                    scanButton.isEnabled = true
                    displayAsteroids(state.asteroids)
                }
                is AsteroidUiState.Error -> {
                    loadingOverlay.visibility = View.GONE
                    scanButton.isEnabled = true
                    showErrorMessage(state.message)
                }
            }
        }
    }

    private fun showErrorMessage(message: String) {
        val builder = SpannableStringBuilder()
        builder.append("⚠️ ALERT ⚠️\n\n", ForegroundColorSpan(ContextCompat.getColor(this, R.color.hazard_red)), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(message)
        asteroidDataText.text = builder
    }

    private fun displayAsteroids(asteroids: List<Asteroid>) {
        if (asteroids.isEmpty()) {
            asteroidDataText.text = "Sector clear. No asteroids detected."
            return
        }

        val builder = SpannableStringBuilder()
        
        for (asteroid in asteroids) {
            val start = builder.length
            builder.append("NAME: ${asteroid.name}\n")
            builder.setSpan(StyleSpan(Typeface.BOLD), start, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.star_gold)), start, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            builder.append("HAZARD: ")
            val hazardText = if (asteroid.isPotentiallyHazardous) "⚠️ CRITICAL" else "SAFE"
            val hazardColor = if (asteroid.isPotentiallyHazardous) R.color.hazard_red else R.color.safe_green
            val hazardStart = builder.length
            builder.append("$hazardText\n")
            builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, hazardColor)), hazardStart, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(StyleSpan(Typeface.BOLD), hazardStart, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val missDistance = asteroid.closeApproachData.firstOrNull()?.missDistance?.kilometers ?: "UNKNOWN"
            builder.append("DISTANCE: ")
            val distanceStart = builder.length
            builder.append("$missDistance km\n")
            builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.text_secondary)), distanceStart, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            builder.append("--------------------------------\n\n")
        }
        
        asteroidDataText.text = builder
    }

    private fun SpannableStringBuilder.append(text: CharSequence, span: Any, flags: Int): SpannableStringBuilder {
        val start = length
        append(text)
        setSpan(span, start, length, flags)
        return this
    }
}

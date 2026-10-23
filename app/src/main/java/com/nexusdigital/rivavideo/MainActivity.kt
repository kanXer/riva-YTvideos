package com.nexusdigital.rivavideo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nexusdigital.rivavideo.ui.RivaVideoApp
import com.nexusdigital.rivavideo.ui.RivaVideoTheme
import com.nexusdigital.rivavideo.ui.RivaVideoViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: RivaVideoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            RivaVideoTheme {
                RivaVideoApp(viewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppBackgrounded()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppForegrounded()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            viewModel.pauseForBackground()
        }
    }
}

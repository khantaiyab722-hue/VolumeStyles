package com.example.volumestyles

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.media.AudioManager
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.SeekBar

class VolumeAccessibilityService : AccessibilityService() {

    private lateinit var audio: AudioManager
    private lateinit var windowManager: WindowManager
    private var panel: SeekBar? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val hideRunnable = Runnable { hidePanel() }

    override fun onServiceConnected() {
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val up = event.keyCode == KeyEvent.KEYCODE_VOLUME_UP
        val down = event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (!up && !down) return false
        if (event.action == KeyEvent.ACTION_DOWN) {
            audio.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER,
                0
            )
            showPanel()
        }
        return true
    }

    private fun showPanel() {
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (panel == null) {
            val bar = SeekBar(this)
            bar.max = max
            bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) audio.setStreamVolume(AudioManager.STREAM_MUSIC, p, 0)
                }
                override fun onStartTrackingTouch(s: SeekBar?) {
                    handler.removeCallbacks(hideRunnable)
                }
                override fun onStopTrackingTouch(s: SeekBar?) {
                    handler.postDelayed(hideRunnable, 2500)
                }
            })
            val params = WindowManager.LayoutParams(
                600,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.END
            params.x = 40
            params.y = 300
            windowManager.addView(bar, params)
            panel = bar
        }
        panel?.progress = current
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, 2500)
    }

    private fun hidePanel() {
        panel?.let {
            windowManager.removeView(it)
            panel = null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
}

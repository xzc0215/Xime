package com.kingzcheung.kime.service

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.kingzcheung.kime.settings.SettingsPreferences

class FeedbackManager(private val context: Context) {
    
    private val audioManager: AudioManager by lazy { 
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager 
    }
    
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    fun playKeySound(keyType: String = "standard") {
        if (!SettingsPreferences.isSoundEnabled(context)) return
        
        val volume = SettingsPreferences.getSoundVolume(context) / 100f
        val soundVolume = (volume * 100).toInt()
        
        val effectType = when (keyType) {
            "delete" -> AudioManager.FX_KEYPRESS_DELETE
            "enter" -> AudioManager.FX_KEYPRESS_RETURN
            "space" -> AudioManager.FX_KEYPRESS_SPACEBAR
            else -> AudioManager.FX_KEYPRESS_STANDARD
        }
        
        audioManager.playSoundEffect(effectType, soundVolume / 100f)
    }
    
    fun performVibration() {
        if (!SettingsPreferences.isVibrationEnabled(context)) return
        if (!vibrator.hasVibrator()) return
        
        val intensity = SettingsPreferences.getVibrationIntensity(context)
        val duration = 10L + (intensity * 0.4).toLong()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitude = (intensity * 2.55).toInt().coerceIn(1, 255)
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
    
    fun performKeyPressEffect(keyType: String = "standard") {
        playKeySound(keyType)
        performVibration()
    }
    
    fun performKeyPressDownEffect(key: String) {
        val keyType = when (key) {
            "delete", "clear_composition" -> "delete"
            "enter" -> "enter"
            "space" -> "space"
            else -> "standard"
        }
        playKeySound(keyType)
        performVibration()
    }
}
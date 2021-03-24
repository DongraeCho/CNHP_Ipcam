package com.deepmedi.cnhp_ipcam.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

@Suppress("DEPRECATION")
class VibList (private val context: Context){

    @SuppressLint("MissingPermission")
    fun vibMeausreInit(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createWaveform(LongArray(1){1000L}, IntArray(1){255}, -1))
        else
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(1000)
    }

    @SuppressLint("MissingPermission")
    fun vibMeausreEnd(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).
            vibrate(VibrationEffect.createWaveform(LongArray(1){2000L}, IntArray(1){255}, -1))
        else
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(2000)
    }

    @SuppressLint("MissingPermission")
    fun vibMeausreInterrupt(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).
            vibrate(VibrationEffect.createWaveform(longArrayOf(250L,250L,250L,250L), intArrayOf(255, 0, 255, 0), -1))
    }
}
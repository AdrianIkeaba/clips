package com.ghostdev.video.clips.util

import android.content.Context

class SharedPreferencesHelper(val context: Context) {

    companion object {
        private const val CLIPS_PREFS_KEY = "CLIPS_PREFS_KEY"
    }

    fun saveStringData(key: String, data: String) {
        val sharedPreferences = context.getSharedPreferences(CLIPS_PREFS_KEY, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(key, data).apply()
    }

    fun getStringData(key:String): String?{
        val sharedPreferences = context.getSharedPreferences(CLIPS_PREFS_KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, null)
    }

    fun deleteStringData(key: String) {
        val sharedPreferences = context.getSharedPreferences(CLIPS_PREFS_KEY, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(key).apply()
    }
}
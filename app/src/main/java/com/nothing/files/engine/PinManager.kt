package com.nothing.files.engine

import android.content.Context
import android.content.SharedPreferences

object PinManager {
    private const val PREF_NAME = "nothing_files_pins"
    private const val KEY_PINS = "pinned_file_paths"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getPinnedPaths(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_PINS, emptySet()) ?: emptySet()
    }

    fun togglePin(context: Context, path: String): Boolean {
        val current = getPinnedPaths(context).toMutableSet()
        val isNowPinned = if (current.contains(path)) {
            current.remove(path)
            false
        } else {
            current.add(path)
            true
        }
        getPrefs(context).edit().putStringSet(KEY_PINS, current).apply()
        return isNowPinned
    }

    fun isPinned(context: Context, path: String): Boolean {
        return getPinnedPaths(context).contains(path)
    }
}

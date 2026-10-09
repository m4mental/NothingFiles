package com.nothing.files.engine

import android.content.Context
import android.content.SharedPreferences
import com.nothing.files.ui.components.DrawerBookmark
import org.json.JSONArray
import org.json.JSONObject

object BookmarkManager {
    private const val PREF_NAME = "nothing_files_bookmarks"
    private const val KEY_BOOKMARKS = "saved_bookmarks"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getBookmarks(context: Context): List<DrawerBookmark> {
        val prefs = getPrefs(context)
        val jsonStr = prefs.getString(KEY_BOOKMARKS, null) ?: return defaultBookmarks()
        val list = mutableListOf<DrawerBookmark>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DrawerBookmark(
                        name = obj.getString("name"),
                        path = obj.getString("path")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return defaultBookmarks()
        }
        return list
    }

    fun addBookmark(context: Context, name: String, path: String): Boolean {
        val current = getBookmarks(context).toMutableList()
        if (current.any { it.path == path }) return false

        current.add(DrawerBookmark(name, path))
        saveBookmarks(context, current)
        return true
    }

    fun removeBookmark(context: Context, path: String): Boolean {
        val current = getBookmarks(context).toMutableList()
        val removed = current.removeAll { it.path == path }
        if (removed) {
            saveBookmarks(context, current)
        }
        return removed
    }

    fun isBookmarked(context: Context, path: String): Boolean {
        return getBookmarks(context).any { it.path == path }
    }

    private fun saveBookmarks(context: Context, list: List<DrawerBookmark>) {
        val array = JSONArray()
        list.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("path", it.path)
            array.put(obj)
        }
        getPrefs(context).edit().putString(KEY_BOOKMARKS, array.toString()).apply()
    }

    private fun defaultBookmarks(): List<DrawerBookmark> {
        val sd = android.os.Environment.getExternalStorageDirectory().absolutePath
        return listOf(
            DrawerBookmark("Download", "$sd/Download"),
            DrawerBookmark("DCIM", "$sd/DCIM"),
            DrawerBookmark("Documents", "$sd/Documents"),
            DrawerBookmark("Pictures", "$sd/Pictures"),
            DrawerBookmark("Movies", "$sd/Movies"),
            DrawerBookmark("Music", "$sd/Music")
        )
    }
}

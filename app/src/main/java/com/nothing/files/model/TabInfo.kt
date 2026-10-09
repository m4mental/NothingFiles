package com.nothing.files.model

import java.util.UUID

data class TabInfo(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "STORAGE",
    val path: String,
    val history: List<String> = listOf(path),
    val historyIndex: Int = 0
) {
    val canGoBack: Boolean get() = historyIndex > 0
    val canGoForward: Boolean get() = historyIndex < history.size - 1

    fun navigateTo(newPath: String): TabInfo {
        if (newPath == path) return this
        val newHistory = history.take(historyIndex + 1) + newPath
        return copy(
            path = newPath,
            title = if (newPath == "/") "ROOT" else newPath.substringAfterLast('/', "STORAGE").ifEmpty { "STORAGE" },
            history = newHistory,
            historyIndex = newHistory.size - 1
        )
    }

    fun goBack(): TabInfo {
        if (!canGoBack) return this
        val newIndex = historyIndex - 1
        val newPath = history[newIndex]
        return copy(
            path = newPath,
            title = if (newPath == "/") "ROOT" else newPath.substringAfterLast('/', "STORAGE").ifEmpty { "STORAGE" },
            historyIndex = newIndex
        )
    }

    fun goForward(): TabInfo {
        if (!canGoForward) return this
        val newIndex = historyIndex + 1
        val newPath = history[newIndex]
        return copy(
            path = newPath,
            title = if (newPath == "/") "ROOT" else newPath.substringAfterLast('/', "STORAGE").ifEmpty { "STORAGE" },
            historyIndex = newIndex
        )
    }
}

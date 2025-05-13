package com.example.artsyproj4

import android.content.SharedPreferences
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(private val prefs: SharedPreferences) : CookieJar {
    private val cookieKey = "cookies"
    private var cache: MutableList<Cookie> = mutableListOf()

    init {
        loadCookies()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cache.removeAll { it.domain == url.host }
        cache.addAll(cookies)
        persistCookies()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        return cache.filter { it.expiresAt > now && it.matches(url) }
    }

    fun clearCookies() {
        cache.clear()
        prefs.edit().remove(cookieKey).apply()
    }

    private fun persistCookies() {
        val serializable = cache.map { SerializableCookie.from(it) }
        prefs.edit().putString(cookieKey, Json.encodeToString(serializable)).apply()
    }

    private fun loadCookies() {
        val json = prefs.getString(cookieKey, null) ?: return
        runCatching {
            val serializable = Json.decodeFromString<List<SerializableCookie>>(json)
            cache = serializable.map { it.toOkHttpCookie() }
                .filter { it.expiresAt > System.currentTimeMillis() }
                .toMutableList()
        }
    }

    private fun Cookie.matches(url: HttpUrl): Boolean {
        return domain == url.host || url.host.endsWith(".$domain")
    }
}

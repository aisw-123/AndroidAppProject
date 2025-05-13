package com.example.artsyproj4
import kotlinx.serialization.Serializable
import okhttp3.Cookie

@Serializable
data class SerializableCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val expiresAt: Long,
    val secure: Boolean,
    val httpOnly: Boolean
) {
    fun toOkHttpCookie(): Cookie = Cookie.Builder()
        .name(name)
        .value(value)
        .domain(domain)
        .path(path)
        .expiresAt(expiresAt)
        .apply {
            if (secure) secure()
            if (httpOnly) httpOnly()
        }
        .build()

    companion object {
        fun from(cookie: Cookie): SerializableCookie = SerializableCookie(
            name = cookie.name,
            value = cookie.value,
            domain = cookie.domain,
            path = cookie.path,
            expiresAt = cookie.expiresAt,
            secure = cookie.secure,
            httpOnly = cookie.httpOnly
        )
    }
}

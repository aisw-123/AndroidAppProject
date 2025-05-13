package com.example.artsyproj4

import android.content.Context
import okhttp3.OkHttpClient

object NetworkClient {
    private var client: OkHttpClient? = null

    fun getClient(context: Context): OkHttpClient {
        if (client == null) {
            val prefs = context.getSharedPreferences("cookies", Context.MODE_PRIVATE)
            val cookieJar = PersistentCookieJar(prefs)

            client = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build()
        }
        return client!!
    }
}

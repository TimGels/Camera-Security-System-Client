package com.camerasecuritysystem.client

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

@SuppressLint("StaticFieldLeak")
object CSSApplication : Application() {
    private lateinit var _context: Context
    var context: Context
        get() = _context
        set(value) {
            _context = value
        }
}

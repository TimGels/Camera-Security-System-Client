package com.camerasecuritysystem.client

inline fun cassert(value: Boolean) {
    if (BuildConfig.DEBUG) {
        assert(value)
    }
}

inline fun cassert(value: Boolean, message: String) {
    if (BuildConfig.DEBUG) {
        assert(value) { message }
    }
}

package com.example.proyecto.data.storage

import platform.Foundation.NSUserDefaults

actual object LocalStorage {

    actual fun putString(key: String, value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
        NSUserDefaults.standardUserDefaults.synchronize()
    }

    actual fun getString(key: String): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(key)

    actual fun remove(key: String) {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
        NSUserDefaults.standardUserDefaults.synchronize()
    }
}

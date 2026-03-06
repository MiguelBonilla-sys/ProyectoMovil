package com.example.proyecto.data.storage

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL

actual class FileStorage actual constructor() {
    private val fileUrl: NSURL?
        get() {
            val docs = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null
            )
            return docs?.URLByAppendingPathComponent("users.json")
        }

    actual fun readUsers(): String? {
        val url = fileUrl ?: return null
        return NSString.stringWithContentsOfURL(
            url = url,
            encoding = NSUTF8StringEncoding,
            error = null
        ) as? String
    }

    actual fun writeUsers(content: String) {
        val url = fileUrl ?: return
        (content as NSString).writeToURL(
            url = url,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )
    }
}

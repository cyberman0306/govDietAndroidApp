package com.hyunjun.govdietandroid

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.URL

class SettingDataStore {
    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    }
}
val bootingGbdTitleAndValue_key = stringPreferencesKey("bootingGbdTitleAndValue_key")
val buttonIndex_key = intPreferencesKey("buttonIndex_key")

suspend fun downloadAndSaveImage(context: Context, imageUrl: String, filename: String, onSaved: (Boolean) -> Unit) {
    withContext(Dispatchers.IO) {
        val file = File(context.filesDir, filename)
        if (!file.exists()) {
            val url = URL("https://gbmo.go.kr" + imageUrl)
            val connection = url.openConnection()
            connection.connect()
            val inputStream: InputStream = connection.getInputStream()
            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            saveImageToInternalStorage(context, bitmap, filename) {
                onSaved(true)
            }
        }
    }
}
fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, filename: String, onSaved: (Boolean) -> Unit) {
    var result: Boolean = false
    context.openFileOutput("$filename.png", Context.MODE_PRIVATE).use { fos ->
        result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        if (result) {
            onSaved(true)
        }
    }
}

fun loadImageFromInternalStorage(context: Context, filename: String): Bitmap? {
    return try {
        val fileInputStream = context.openFileInput("$filename.png")
        BitmapFactory.decodeStream(fileInputStream)
    } catch (e: Exception) {
        null
    }
}


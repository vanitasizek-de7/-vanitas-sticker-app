package com.vanitas.stickers

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException

/** Serves the read-only sticker-pack metadata and WebP files requested by WhatsApp. */
class StickerContentProvider : ContentProvider() {
    override fun onCreate() = true

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val columns = projection?.toTypedArray() ?: if (uri.pathSegments.firstOrNull() == "metadata") META_COLUMNS else STICKER_COLUMNS
        val cursor = MatrixCursor(columns)
        val ctx = context ?: return cursor
        val packs = JSONArray(ctx.getSharedPreferences("sticker_packs", Context.MODE_PRIVATE).getString("packs", "[]") ?: "[]")
        when (uri.pathSegments.firstOrNull()) {
            "metadata" -> for (i in 0 until packs.length()) {
                val pack = packs.optJSONObject(i) ?: continue
                cursor.addRow(columns.map { column -> when (column) {
                    "identifier" -> pack.optString("id")
                    "name" -> pack.optString("name")
                    "publisher" -> pack.optString("publisher")
                    "tray_image" -> pack.optString("tray")
                    "image_data_version" -> "1"
                    "avoid_cache" -> 1
                    "animated_sticker_pack" -> 0
                    "publisher_email", "publisher_website", "privacy_policy_website", "license_agreement_website", "android_play_store_link", "ios_app_store_link" -> ""
                    else -> ""
                } })
            }
            "stickers" -> {
                val packId = uri.pathSegments.getOrNull(1)
                val pack = (0 until packs.length()).mapNotNull { packs.optJSONObject(it) }.firstOrNull { it.optString("id") == packId }
                if (pack != null) {
                    val dir = File(ctx.filesDir, "sticker_packs/$packId")
                    val metadata = File(dir, "metadata.json").takeIf { it.isFile }?.readText()?.let(::JSONObject)
                    val stickers = metadata?.optJSONArray("stickers")
                    if (stickers != null) for (i in 0 until stickers.length()) {
                        val sticker = stickers.optJSONObject(i) ?: continue
                        cursor.addRow(columns.map { column -> when (column) {
                            "image" -> sticker.optString("image_file")
                            "emojis" -> sticker.optJSONArray("emojis")?.toString() ?: "[\"✨\"]"
                            else -> ""
                        } })
                    }
                }
            }
        }
        return cursor
    }

    override fun getType(uri: Uri): String = when {
        uri.pathSegments.firstOrNull() == "metadata" -> "vnd.android.cursor.dir/vnd.${context?.packageName}.sticker_pack"
        uri.pathSegments.firstOrNull() == "stickers" && uri.pathSegments.size == 2 -> "vnd.android.cursor.dir/vnd.${context?.packageName}.sticker"
        else -> "image/webp"
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw FileNotFoundException("Read-only provider")
        val parts = uri.pathSegments
        if (parts.size != 3 || parts[0] != "stickers") throw FileNotFoundException("Unknown sticker URI")
        val packId = parts[1]
        val fileName = parts[2]
        if (!packId.matches(Regex("[A-Za-z0-9_]+")) || !fileName.matches(Regex("[A-Za-z0-9_.-]+"))) throw FileNotFoundException("Invalid sticker path")
        val file = File(context?.filesDir, "sticker_packs/$packId/$fileName").canonicalFile
        val root = File(context?.filesDir, "sticker_packs/$packId").canonicalFile
        if (!file.path.startsWith(root.path + File.separator) || !file.isFile) throw FileNotFoundException("Sticker not found")
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    companion object {
        private val META_COLUMNS = arrayOf("identifier", "name", "publisher", "tray_image", "image_data_version", "avoid_cache", "animated_sticker_pack", "publisher_email", "publisher_website", "privacy_policy_website", "license_agreement_website", "android_play_store_link", "ios_app_store_link")
        private val STICKER_COLUMNS = arrayOf("image", "emojis")
    }
}

package com.vanitas.stickers

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.WhatsApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

private val Ink = Color(0xFF090B10)
private val Panel = Color(0xFF141821)
private val Violet = Color(0xFF9B7BFF)
private val Mint = Color(0xFF8BF0D0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VanitasApp() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun VanitasApp() {
        val selected = remember { mutableStateListOf<Uri>() }
        var packName by remember { mutableStateOf("") }
        var publisher by remember { mutableStateOf("VANITAS STUDIO") }
        var busy by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            selected.clear()
            selected.addAll(uris.take(30))
            if (uris.size > 30) toast("تم اختيار أول 30 صورة فقط")
        }

        MaterialTheme(colorScheme = darkColorScheme(
            primary = Violet, secondary = Mint, background = Ink, surface = Panel,
            onPrimary = Ink, onBackground = Color.White, onSurface = Color.White
        )) {
            Surface(Modifier.fillMaxSize(), color = Ink) {
                Column(
                    Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF171426), Ink, Ink))).padding(horizontal = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("VANITAS", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                            Text("STICKER ATELIER  •  WHATSAPP", color = Mint, fontSize = 10.sp, letterSpacing = 2.sp)
                        }
                        Box(Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF252033)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Violet)
                        }
                    }

                    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Panel), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("حوّل لحظاتك إلى ملصقات", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("أنشئ حزمة أنيقة، جهّزها تلقائيًا بصيغة WhatsApp، وأضفها بنقرة واحدة.", color = Color(0xFFAFB5C4), fontSize = 14.sp, lineHeight = 21.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Tag("WEBP 512×512")
                                Tag("حتى 30 ملصقًا")
                                Tag("خصوصية محلية")
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("حزمتك الجديدة", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("أضف صورًا من جهازك وابدأ التصميم", color = Color(0xFF9299AA), fontSize = 12.sp)
                        }
                        TextButton(onClick = { selected.clear() }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFB7A8E9), modifier = Modifier.size(17.dp))
                            Text(" مسح", color = Color(0xFFB7A8E9))
                        }
                    }

                    OutlinedTextField(value = packName, onValueChange = { packName = it.take(32) }, label = { Text("اسم الحزمة") }, placeholder = { Text("مثال: لحظات فانيـتاس") }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = publisher, onValueChange = { publisher = it.take(32) }, label = { Text("اسم الناشر") }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth())

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("معاينة الملصقات  ·  ${selected.size}/30", color = Color(0xFFDCE0EA), fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { picker.launch("image/*") }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Mint)
                            Text(" إضافة صور", color = Mint)
                        }
                    }

                    if (selected.isEmpty()) {
                        Card(onClick = { picker.launch("image/*") }, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Panel), modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.size(70.dp).clip(CircleShape).background(Color(0xFF252033)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = Violet, modifier = Modifier.size(34.dp))
                                }
                                Spacer(Modifier.height(14.dp))
                                Text("معرضك الإبداعي يبدأ هنا", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("اضغط لاختيار الصور التي تريد تحويلها", color = Color(0xFF9299AA), fontSize = 13.sp)
                            }
                        }
                    } else {
                        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            itemsIndexed(selected) { index, uri ->
                                StickerPreview(uri, index) { selected.remove(uri) }
                            }
                            item {
                                Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(20.dp)).background(Panel).border(1.dp, Violet.copy(alpha = .35f), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                                    TextButton(onClick = { picker.launch("image/*") }) { Icon(Icons.Default.Add, null, tint = Mint) }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (selected.isEmpty()) { toast("أضف صورة واحدة على الأقل"); return@Button }
                            if (packName.isBlank()) { toast("اكتب اسم الحزمة أولاً"); return@Button }
                            busy = true
                            val images = selected.toList()
                            scope.launch {
                                val result = withContext(Dispatchers.IO) { createPack(images, packName.trim(), publisher.trim().ifBlank { "VANITAS STUDIO" }) }
                                busy = false
                                result.fold(::addPackToWhatsApp) { toast("تعذر تجهيز الحزمة: ${it.message ?: "خطأ غير معروف"}") }
                            }
                        },
                        enabled = !busy,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Violet, disabledContainerColor = Violet.copy(alpha = .45f)),
                        modifier = Modifier.fillMaxWidth().height(58.dp)
                    ) {
                        if (busy) CircularProgressIndicator(Modifier.size(22.dp), color = Ink, strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.WhatsApp, contentDescription = null)
                            Text("  إضافة الحزمة إلى WhatsApp", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Text("صورك تبقى على جهازك ولا يتم رفعها إلى أي خادم.", color = Color(0xFF777E8D), fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 10.dp))
                }
            }
        }
    }

    @Composable
    private fun Tag(label: String) {
        Text(label, color = Mint, fontSize = 9.sp, modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFF202C30)).padding(horizontal = 9.dp, vertical = 6.dp))
    }

    @Composable
    private fun StickerPreview(uri: Uri, index: Int, onRemove: () -> Unit) {
        val bitmap = remember(uri) {
            runCatching { contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }.getOrNull()
        }
        Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(20.dp)).background(Panel)) {
            if (bitmap != null) androidx.compose.foundation.Image(bitmap.asImageBitmap(), null, Modifier.fillMaxSize().padding(8.dp), contentScale = ContentScale.Fit)
            else Icon(Icons.Default.Image, null, tint = Color.Gray, modifier = Modifier.align(Alignment.Center))
            Text("${index + 1}", color = Color.White, fontSize = 11.sp, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).clip(CircleShape).background(Color(0xBB090B10)).padding(horizontal = 8.dp, vertical = 4.dp))
            TextButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).size(34.dp)) { Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(17.dp)) }
        }
    }

    private fun createPack(uris: List<Uri>, name: String, publisher: String): Result<String> = runCatching {
        val id = "vanitas_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val packDir = File(filesDir, "sticker_packs/$id").apply { mkdirs() }
        val stickers = JSONArray()
        uris.forEachIndexed { index, uri ->
            val source = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?: error("تعذرت قراءة الصورة ${index + 1}")
            val stickerFile = File(packDir, "sticker_$index.webp")
            writeSticker(source, stickerFile, 512)
            source.recycle()
            stickers.put(JSONObject().put("image_file", stickerFile.name).put("emojis", JSONArray().put("✨")))
        }
        val firstBitmap = contentResolver.openInputStream(uris.first())?.use { BitmapFactory.decodeStream(it) } ?: error("تعذرت قراءة صورة الغلاف")
        writeSticker(firstBitmap, File(packDir, "tray.webp"), 96)
        firstBitmap.recycle()
        val metadata = JSONObject()
            .put("identifier", id)
            .put("name", name)
            .put("publisher", publisher)
            .put("tray_image", "tray.webp")
            .put("image_data_version", "1")
            .put("avoid_cache", true)
            .put("animated_sticker_pack", false)
            .put("stickers", stickers)
        File(packDir, "metadata.json").writeText(metadata.toString())
        val prefs = getSharedPreferences("sticker_packs", MODE_PRIVATE)
        val packs = JSONArray(prefs.getString("packs", "[]"))
        packs.put(JSONObject().put("id", id).put("name", name).put("publisher", publisher).put("count", uris.size).put("tray", "tray.webp"))
        prefs.edit().putString("packs", packs.toString()).apply()
        id
    }

    private fun writeSticker(source: Bitmap, destination: File, size: Int) {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
        val scale = minOf((size * 0.88f) / source.width, (size * 0.88f) / source.height)
        val width = source.width * scale
        val height = source.height * scale
        val left = (size - width) / 2f
        val top = (size - height) / 2f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(source, null, RectF(left, top, left + width, top + height), paint)
        FileOutputStream(destination).use { stream ->
            @Suppress("DEPRECATION")
            output.compress(Bitmap.CompressFormat.WEBP, 90, stream)
        }
        output.recycle()
    }

    private fun addPackToWhatsApp(packId: String) {
        val info = JSONObject(getSharedPreferences("sticker_packs", MODE_PRIVATE).getString("packs", "[]")!!)
        val pack = (0 until info.length()).map { info.getJSONObject(it) }.firstOrNull { it.getString("id") == packId }
        if (pack == null) { toast("تعذر العثور على الحزمة"); return }
        val intent = Intent("com.whatsapp.intent.action.ENABLE_STICKER_PACK").apply {
            putExtra("sticker_pack_id", packId)
            putExtra("sticker_pack_name", pack.getString("name"))
            putExtra("sticker_pack_authority", "$packageName.stickerprovider")
        }
        val target = listOf("com.whatsapp", "com.whatsapp.w4b").firstOrNull { packageName ->
            intent.setPackage(packageName)
            intent.resolveActivity(packageManager) != null
        }
        if (target == null) {
            toast("ثبّت WhatsApp أو WhatsApp Business ثم أعد المحاولة")
            return
        }
        intent.setPackage(target)
        runCatching { startActivity(intent) }.onFailure { toast("لم يتمكن WhatsApp من فتح الحزمة: ${it.message}") }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

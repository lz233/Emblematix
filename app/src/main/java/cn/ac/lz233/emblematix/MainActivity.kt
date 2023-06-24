package cn.ac.lz233.emblematix

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.get
import androidx.core.graphics.set
import androidx.exifinterface.media.ExifInterface
import cn.ac.lz233.emblematix.logic.dao.ConfigDao
import cn.ac.lz233.emblematix.ui.theme.EmblematixTheme
import cn.ac.lz233.emblematix.util.LogUtil
import cn.ac.lz233.emblematix.util.ktx.getCopyRight
import cn.ac.lz233.emblematix.util.ktx.getDevice
import cn.ac.lz233.emblematix.util.ktx.getISO
import cn.ac.lz233.emblematix.util.ktx.getPhotoInfo
import cn.ac.lz233.emblematix.util.ktx.getString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.min


class MainActivity : ComponentActivity() {

    lateinit var exif: ExifInterface

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EmblematixTheme {
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                var isProcessing by remember { mutableStateOf(false) }
                var bitmap by remember { mutableStateOf(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)) }
                var watermarkedBitmap by remember { mutableStateOf(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)) }
                LaunchedEffect(bitmap) {
                    withContext(Dispatchers.IO) {
                        if (bitmap.width != 1 && bitmap.height != 1) {
                            isProcessing = true
                            val watermark = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(watermark)
                            val paint = Paint().apply {
                                color = Color.BLACK
                                textSize = min(bitmap.width, bitmap.height) * 0.03f
                                textAlign = Paint.Align.CENTER
                                typeface = ResourcesCompat.getFont(App.context, R.font.googlesansregular)
                            }
                            var drawStartHeight = bitmap.height * 0.9f
                            listOf("${exif.getDevice()}  ${exif.getPhotoInfo()}", exif.getCopyRight()).forEach {
                                this@withContext.ensureActive()
                                canvas.drawText(
                                    it,
                                    bitmap.width / 2f,
                                    drawStartHeight,
                                    paint
                                )
                                drawStartHeight += paint.descent() - paint.ascent()
                            }
                            watermarkedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true).apply {
                                val overlayHeight = watermark.height * 0.9.toInt()
                                for (x in 0 until watermark.width) {
                                    for (y in overlayHeight until watermark.height) {
                                        this@withContext.ensureActive()
                                        if (watermark[x, y] == Color.BLACK) {
                                            this[x, y] = this[x, y].run {
                                                val gain = (20..105).random()
                                                val red = Color.red(this) + gain
                                                val green = Color.green(this) + gain
                                                val blue = Color.blue(this) + gain
                                                Color.argb(
                                                    255,
                                                    if (red in 0..255) red else 255,
                                                    if (green in 0..255) green else 255,
                                                    if (blue in 0..255) blue else 255
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            isProcessing = false
                        }
                    }
                }
                val pickMedia = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
                    it?.let {
                        val inputStream = contentResolver.openInputStream(it)!!
                        exif = ExifInterface(inputStream)
                        watermarkedBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                        bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, it))
                        inputStream.close()
                    }
                }
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        LargeTopAppBar(
                            title = {
                                Text(
                                    R.string.app_name.getString(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            scrollBehavior = scrollBehavior,
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                ) { innerPadding ->
                    Column(
                        Modifier
                            .padding(innerPadding)
                            .padding(start = 25.dp, end = 25.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            onClick = {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 41.dp, bottom = 0.dp)
                                .defaultMinSize(minHeight = 200.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Image(
                                    painter = BitmapPainter(if (watermarkedBitmap.height == 1) bitmap.asImageBitmap() else watermarkedBitmap.asImageBitmap()),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (isProcessing) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 25.dp, end = 25.dp)
                                .defaultMinSize(minHeight = 50.dp),
                            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                            enabled = !isProcessing && watermarkedBitmap.height != 1,
                            onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        isProcessing = true
                                        runCatching {
                                            val imageFilePath = "${Environment.DIRECTORY_PICTURES}${File.separator}Emblematix${File.separator}"
                                            val imageFileName = "Emblematix_${exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)?.replace(' ', '-') ?: System.currentTimeMillis()}.webp"
                                            val values = ContentValues().apply {
                                                put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName)
                                                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
                                                put(MediaStore.Images.Media.RELATIVE_PATH, imageFilePath)
                                            }
                                            val writeUri = contentResolver.insert(
                                                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                                                values
                                            )
                                            val outputStream = contentResolver.openOutputStream(writeUri!!)
                                            watermarkedBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, outputStream!!)
                                            outputStream.close()
                                        }.onFailure {
                                            LogUtil.e(it)
                                        }
                                        isProcessing = false
                                    }
                                }
                            }
                        ) {
                            Text(text = "Save")
                        }
                        OutlinedTextField(
                            modifier = Modifier
                                .padding(top = 25.dp)
                                .fillMaxWidth(),
                            value = ConfigDao.copyright,
                            label = {
                                    Text(text = "Copyright")
                            },
                            onValueChange = {
                                ConfigDao.copyright = it
                                bitmap = bitmap.copy(bitmap.config, false)
                            },
                        )
                    }
                }
            }
        }
    }
}
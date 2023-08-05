package cn.ac.lz233.emblematix

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.core.util.toRange
import androidx.exifinterface.media.ExifInterface
import cn.ac.lz233.emblematix.logic.dao.ConfigDao
import cn.ac.lz233.emblematix.ui.theme.EmblematixTheme
import cn.ac.lz233.emblematix.ui.widget.ConfigChip
import cn.ac.lz233.emblematix.ui.widget.SingleChoiceConfigChipGroup
import cn.ac.lz233.emblematix.util.LogUtil
import cn.ac.lz233.emblematix.util.ktx.getAuthor
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
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class MainActivity : ComponentActivity() {

    lateinit var exif: ExifInterface

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalEncodingApi::class)
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
                            val drawText = if (ConfigDao.watermarkType == "normal") {
                                listOf("${exif.getDevice()}  ${exif.getPhotoInfo()}", exif.getCopyRight())
                            } else {
                                listOf("${exif.getDevice()}  ${exif.getPhotoInfo()}  ${exif.getCopyRight()}")
                            }
                            if (ConfigDao.randomization == "randomize") {
                                val watermark = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(watermark)
                                val paint = Paint().apply {
                                    color = Color.BLACK
                                    textSize = min(bitmap.width, bitmap.height) * if (ConfigDao.watermarkType == "normal") 0.03f else 0.02f
                                    textAlign = if (ConfigDao.watermarkType == "normal") Paint.Align.CENTER else Paint.Align.LEFT
                                    typeface = ResourcesCompat.getFont(App.context, R.font.googlesansregular)
                                }
                                val drawStartWidth = if (ConfigDao.watermarkType == "normal") bitmap.width / 2f else max(bitmap.width, bitmap.height) * 0.01f
                                val drawStartHeight = if (ConfigDao.watermarkType == "normal") bitmap.height * 0.9f else bitmap.height - (paint.descent() - paint.ascent())
                                var drawStartHeightDynamic = drawStartHeight
                                drawText.forEach {
                                    this@withContext.ensureActive()
                                    canvas.drawText(
                                        it,
                                        drawStartWidth,
                                        drawStartHeightDynamic,
                                        paint
                                    )
                                    drawStartHeightDynamic += paint.descent() - paint.ascent()
                                }
                                watermarkedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true).apply {
                                    val overlayHeight = (drawStartHeight - (paint.descent() - paint.ascent())).toInt()
                                    val addOrSub = if (ConfigDao.alterBrightness == "dim") -1 else 1
                                    var gain = (0..100).random() * addOrSub
                                    for (x in 0 until watermark.width) {
                                        for (y in overlayHeight until watermark.height) {
                                            this@withContext.ensureActive()
                                            if (watermark[x, y] == Color.BLACK) {
                                                this[x, y] = this[x, y].run {
                                                    val red = Color.red(this) + gain
                                                    val green = Color.green(this) + gain
                                                    val blue = Color.blue(this) + gain
                                                    if ((red in 0..255) && (green in 0..255) && (blue in 0..255)) {
                                                        Color.argb(255, red, green, blue)
                                                    } else {
                                                        this
                                                        //Color.argb(255, red - gain, green - gain, blue - gain)
                                                    }
                                                }
                                                gain = (0..100).random() * addOrSub
                                            }
                                        }
                                    }
                                }
                            } else {
                                watermarkedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                                val canvas = Canvas(watermarkedBitmap)
                                val paint = Paint().apply {
                                    color = if (ConfigDao.alterBrightness == "dim") Color.argb(80, 0, 0, 0) else Color.argb(80, 255, 255, 255)
                                    textSize = min(bitmap.width, bitmap.height) * if (ConfigDao.watermarkType == "normal") 0.03f else 0.02f
                                    textAlign = if (ConfigDao.watermarkType == "normal") Paint.Align.CENTER else Paint.Align.LEFT
                                    typeface = ResourcesCompat.getFont(App.context, R.font.googlesansregular)
                                }
                                val drawStartWidth = if (ConfigDao.watermarkType == "normal") bitmap.width / 2f else max(bitmap.width, bitmap.height) * 0.01f
                                var drawStartHeight = if (ConfigDao.watermarkType == "normal") bitmap.height * 0.9f else bitmap.height - (paint.descent() - paint.ascent())
                                drawText.forEach {
                                    this@withContext.ensureActive()
                                    canvas.drawText(
                                        it,
                                        drawStartWidth,
                                        drawStartHeight,
                                        paint
                                    )
                                    drawStartHeight += paint.descent() - paint.ascent()
                                }
                            }
                            val canvas = Canvas(watermarkedBitmap)
                            val paint = Paint().apply {
                                color = if (ConfigDao.alterBrightness == "dim") Color.argb(5, 0, 0, 0) else Color.argb(5, 255, 255, 255)
                                textSize = min(bitmap.width, bitmap.height) * 0.05f
                                textAlign = Paint.Align.LEFT
                                typeface = ResourcesCompat.getFont(App.context, R.font.googlesansregular)
                            }
                            var drawStartHeight = 0f
                            val author = exif.getAuthor()
                            val authorWidth = paint.measureText(author)
                            while (drawStartHeight < bitmap.height + paint.textSize) {
                                StringBuilder().apply {
                                    while (paint.measureText(this.toString()) < bitmap.width + authorWidth) {
                                        repeat((1..20).random()) {
                                            append(' ')
                                        }
                                        append(author)
                                    }
                                }.toString().run {
                                    canvas.drawText(
                                        this,
                                        0f,
                                        drawStartHeight,
                                        paint
                                    )
                                }
                                drawStartHeight += (paint.textSize.toInt()..(paint.textSize * 3).toInt()).random()
                            }
                            isProcessing = false
                        }
                    }
                }
                val resultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                    if (activityResult.resultCode == Activity.RESULT_OK) {
                        activityResult.data?.data?.let {
                            val inputStream = contentResolver.openInputStream(it)!!
                            exif = ExifInterface(inputStream)
                            watermarkedBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, it))
                            inputStream.close()
                        }
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
                            //.padding(start = 25.dp, end = 25.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            onClick = {
                                val intent = Intent.createChooser(
                                    Intent(Intent.ACTION_PICK).apply {
                                        type = "image/*"
                                    },
                                    "Choose one"
                                )
                                intent.putExtra(
                                    Intent.EXTRA_INITIAL_INTENTS,
                                    arrayOf(
                                        Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                                            type = "image/*"
                                        },
                                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                            addCategory(Intent.CATEGORY_OPENABLE)
                                            type = "image/*"
                                        },
                                    )
                                )
                                resultLauncher.launch(intent)
                            },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 0.dp, start = 25.dp, end = 25.dp)
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
                        Row(
                            modifier = Modifier.padding(start = 25.dp, end = 25.dp)
                        ) {
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 25.dp, end = 12.5.dp)
                                    .defaultMinSize(minHeight = 50.dp),
                                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                                enabled = !isProcessing && watermarkedBitmap.height != 1,
                                onClick = {
                                    scope.launch {
                                        isProcessing = true
                                        saveImage(watermarkedBitmap, CompressFormat.WEBP_LOSSLESS)
                                        isProcessing = false
                                    }
                                }
                            ) {
                                Text(text = "WebP")
                            }
                            Button(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.5.dp, end = 25.dp)
                                    .defaultMinSize(minHeight = 50.dp),
                                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                                enabled = !isProcessing && watermarkedBitmap.height != 1,
                                onClick = {
                                    scope.launch {
                                        isProcessing = true
                                        saveImage(watermarkedBitmap, CompressFormat.JPEG)
                                        isProcessing = false
                                    }
                                }
                            ) {
                                Text(text = "JPG")
                            }
                        }
                        Row(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.width(20.dp))
                            ConfigChip("Manufacturer", "showManufacturer", true) {
                                bitmap = bitmap.copy(bitmap.config, false)
                            }
                            ConfigChip("Model", "showModel", true) {
                                bitmap = bitmap.copy(bitmap.config, false)
                            }
                            ConfigChip("F Number", "showFNumber", true) {
                                bitmap = bitmap.copy(bitmap.config, false)
                            }
                            ConfigChip("Shutter Speed", "showShutterSpeed", true) {
                                bitmap = bitmap.copy(bitmap.config, false)
                            }
                            ConfigChip("Focal Length", "showFocalLength", true) {
                                bitmap = bitmap.copy(bitmap.config, false)
                            }
                            ConfigChip("ISO", "showISO", true) {
                                bitmap = bitmap.copy(bitmap.config, false)
                            }
                            ConfigChip("Date & Time", "showDateTime", true) {
                                bitmap = bitmap.copy(bitmap.config, false)
                            }
                            ConfigChip("Copyright", "showCopyright", true) {
                                bitmap = bitmap.copy(bitmap.config, false)
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                        }
                        SingleChoiceConfigChipGroup(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp, end = 20.dp),
                            key = "watermarkType",
                            defaultValue = "Normal",
                            "Normal" to { bitmap = bitmap.copy(bitmap.config, false) },
                            "Compact" to { bitmap = bitmap.copy(bitmap.config, false) }
                        )
                        SingleChoiceConfigChipGroup(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp, end = 20.dp),
                            key = "randomization",
                            defaultValue = "Randomize",
                            "Randomize" to { bitmap = bitmap.copy(bitmap.config, false) },
                            "Static" to { bitmap = bitmap.copy(bitmap.config, false) }
                        )
                        SingleChoiceConfigChipGroup(
                            modifier = Modifier.padding(top = 10.dp, start = 20.dp, end = 20.dp),
                            key = "alterBrightness",
                            defaultValue = "Brighten",
                            "Dim" to { bitmap = bitmap.copy(bitmap.config, false) },
                            "Brighten" to { bitmap = bitmap.copy(bitmap.config, false) }
                        )
                        OutlinedTextField(
                            modifier = Modifier
                                .padding(top = 10.dp, start = 25.dp, end = 25.dp)
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

    private suspend fun saveImage(bitmap: Bitmap, compressFormat: CompressFormat) {
        withContext(Dispatchers.IO) {
            //isProcessing = true
            runCatching {
                val imageFilePath = "${Environment.DIRECTORY_PICTURES}${File.separator}Emblematix${File.separator}"
                val imageFileName = "Emblematix_${System.currentTimeMillis()}.${
                    when (compressFormat) {
                        CompressFormat.JPEG -> "jpg"
                        CompressFormat.WEBP_LOSSLESS -> "webp"
                        else -> "jpg"
                    }
                }"
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
                bitmap.compress(compressFormat, 100, outputStream!!)
                outputStream.close()
            }.onFailure {
                LogUtil.e(it)
            }
            //isProcessing = false
        }
    }
}
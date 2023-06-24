package cn.ac.lz233.emblematix.ui.theme

import android.graphics.Typeface
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.ui.text.font.SystemFontFamily
import androidx.compose.ui.unit.sp
import cn.ac.lz233.emblematix.R

val fonts = FontFamily(
    Font(R.font.googlesansregular, weight = FontWeight.Normal),
    Font(R.font.googlesansmedium, weight = FontWeight.Medium)
)

// Set of Material typography styles to start with
val Typography = with(Typography()) {
    copy(
        displayLarge = displayLarge.copy(fontFamily = fonts),
        displayMedium = displayMedium.copy(fontFamily = fonts),
        displaySmall = displaySmall.copy(fontFamily = fonts),
        headlineLarge = headlineLarge.copy(fontFamily = fonts),
        headlineMedium = headlineMedium.copy(fontFamily = fonts),
        headlineSmall = headlineSmall.copy(fontFamily = fonts),
        titleLarge = titleLarge.copy(fontFamily = fonts),
        titleMedium = titleMedium.copy(fontFamily = fonts),
        titleSmall = titleSmall.copy(fontFamily = fonts),
        bodyLarge = bodyLarge.copy(fontFamily = fonts),
        bodyMedium = bodyMedium.copy(fontFamily = fonts),
        bodySmall = bodySmall.copy(fontFamily = fonts),
        labelLarge = labelLarge.copy(fontFamily = fonts),
        labelMedium = labelMedium.copy(fontFamily = fonts),
        labelSmall = labelSmall.copy(fontFamily = fonts)
    )
}
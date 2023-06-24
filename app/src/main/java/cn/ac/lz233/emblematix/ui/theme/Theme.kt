package cn.ac.lz233.emblematix.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun EmblematixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    val colorScheme = if (darkTheme) {
        dynamicDarkColorScheme(LocalContext.current).copy(
            surface = colorResource(id = android.R.color.system_neutral1_900),
            background = colorResource(id = android.R.color.system_neutral1_900)
        )
    } else {
        dynamicLightColorScheme(LocalContext.current).copy(
            surface = colorResource(android.R.color.system_neutral1_50),
            background = colorResource(id = android.R.color.system_neutral1_50)
        )
    }

    WindowCompat.setDecorFitsSystemWindows((LocalView.current.context as Activity).window, false)
    systemUiController.setSystemBarsColor(Color.Transparent)
    systemUiController.systemBarsDarkContentEnabled = !isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SapphirePrimary,
    secondary = ElectricIce,
    tertiary = SapphireLight,
    background = Color(0xFF0C1013), // رمادي هندسي داكن جداً متطابق مع هوية التوازن
    surface = Color(0xFF1D2226),    // أسطح داكنة متسقة
    onPrimary = Color.White,
    onSecondary = Color(0xFF001F2A),
    onBackground = Color(0xFFE3E8EC),
    onSurface = Color(0xFFF1F5F9)
)

private val LightColorScheme = lightColorScheme(
    primary = SapphirePrimary,
    secondary = ElectricIce,
    tertiary = SapphireLight,
    background = SoftGrayBag,
    surface = SolidWhite,
    onPrimary = Color.White,
    onSecondary = Color(0xFF001F2A),
    onBackground = SapphireDark, // رمادي داكن ناصع ومريح
    onSurface = SapphireDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // نقوم بتعطيل الألوان الديناميكية للحفاظ على الهوية البصرية الجذابة لمعمل العلوي
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

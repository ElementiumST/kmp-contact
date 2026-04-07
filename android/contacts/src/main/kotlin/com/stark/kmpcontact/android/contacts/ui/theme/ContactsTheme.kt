package com.stark.kmpcontact.android.contacts.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ContactsLightColorScheme = lightColorScheme(
    primary = MainTabBarIcons,
    onPrimary = Color.White,
    primaryContainer = MainTabBarIcons.copy(alpha = 0.14f),
    onPrimaryContainer = ContTextEmailColor,
    secondary = ContBackButton,
    onSecondary = Color.White,
    secondaryContainer = ContTagBackground,
    onSecondaryContainer = ContTextEmailColor,
    tertiary = ContTextButtonExtraInfo,
    background = Color.White,
    onBackground = ContTextEmailColor,
    surface = Color.White,
    onSurface = ContTextEmailColor,
    surfaceVariant = ContTagBackground,
    onSurfaceVariant = ContFieldContactInfo,
    outline = ContEditFieldContact,
    error = ContTextButtonDeleteContact,
)

@Composable
fun ContactsTheme(
    content: @Composable () -> Unit,
) {
    val view = LocalView.current

    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    // TODO: The source project used Open Sans from bundled font resources.
    // Keep the system sans-serif typography until those assets are added here.
    MaterialTheme(
        colorScheme = ContactsLightColorScheme,
        typography = ContactsTypography,
        content = content,
    )
}

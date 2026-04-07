package com.stark.kmpcontact.android.contacts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stark.kmpcontact.android.contacts.ui.theme.MainTabBarIcons

@Composable
internal fun ContactAvatar(
    name: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = avatarColorFor(name))
            .size(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsFor(name),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun initialsFor(name: String): String {
    val parts = name
        .trim()
        .split(' ')
        .filter(String::isNotBlank)
        .take(2)

    if (parts.isEmpty()) {
        return "?"
    }

    return parts.joinToString(separator = "") { it.take(1) }.uppercase()
}

private fun avatarColorFor(name: String): Color {
    val palette = listOf(
        MainTabBarIcons,
        Color(0xFF5CB85C),
        Color(0xFFF0AD4E),
        Color(0xFF9B59B6),
        Color(0xFFEC7063),
        Color(0xFF16A085),
    )

    return palette[name.hashCode().mod(palette.size)]
}

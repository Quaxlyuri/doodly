package com.doodly.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.doodly.app.data.local.Mood

@Composable
fun MoodIcon(
    mood: Mood,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp
) {
    val color = Color(android.graphics.Color.parseColor(mood.colorHex))
    Box(
        modifier = modifier
            .size(size)
            .background(color.copy(alpha = 0.22f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = mood.icon(),
            contentDescription = mood.label,
            tint = color.darkened(),
            modifier = Modifier.size(size * 0.58f)
        )
    }
}

private fun Mood.icon(): ImageVector = when (this) {
    Mood.HAPPY -> Icons.Outlined.SentimentSatisfiedAlt
    Mood.EXCITED -> Icons.Outlined.FavoriteBorder
    Mood.CALM -> Icons.Outlined.Spa
    Mood.TIRED -> Icons.Outlined.Bedtime
    Mood.SAD -> Icons.Outlined.SentimentDissatisfied
    Mood.ANGRY -> Icons.Outlined.SentimentVeryDissatisfied
}

private fun Color.darkened(): Color = Color(
    red = red * 0.68f,
    green = green * 0.68f,
    blue = blue * 0.68f,
    alpha = 1f
)

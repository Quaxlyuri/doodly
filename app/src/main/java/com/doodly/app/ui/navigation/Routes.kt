package com.doodly.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val SPLASH = "splash"
    const val GALLERY = "gallery"
    const val CALENDAR = "calendar"
    const val WRITE = "write"
    const val WRITE_ROUTE = "write?date={date}"
    const val SETTINGS = "settings"
    const val DETAIL = "diary/{id}"

    fun write(date: Long? = null): String =
        if (date == null) WRITE else "write?date=$date"

    fun detail(id: Int): String = "diary/$id"
}

data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomDestinations = listOf(
    BottomDestination(Routes.GALLERY, "갤러리", Icons.Outlined.Collections),
    BottomDestination(Routes.CALENDAR, "캘린더", Icons.Outlined.CalendarMonth),
    BottomDestination(Routes.WRITE, "작성", Icons.Outlined.AddCircle),
    BottomDestination(Routes.SETTINGS, "설정", Icons.Outlined.Settings)
)

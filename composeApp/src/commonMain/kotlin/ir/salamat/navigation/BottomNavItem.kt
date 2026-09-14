package ir.salamat.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: Route,
    val titleFa: String,
    val titleEn: String,
    val icon: ImageVector
) {
    HOME(
        route = Route.Home,
        titleFa = "خانه",
        titleEn = "Home",
        icon = Icons.Default.Home
    ),
    FAMILY(
        route = Route.Family,
        titleFa = "خانواده",
        titleEn = "Family",
        icon = Icons.Default.Person
    ),
    QUICK_TOOLS(
        route = Route.QuickTools,
        titleFa = "ابزارها",
        titleEn = "Tools",
        icon = Icons.Default.Favorite
    ),
    SETTINGS(
        route = Route.Settings,
        titleFa = "تنظیمات",
        titleEn = "Settings",
        icon = Icons.Default.Settings
    )
}

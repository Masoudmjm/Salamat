package ir.salamat.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object Family : Route

    @Serializable
    data class MemberDetail(val profileId: String) : Route

    @Serializable
    data object QuickTools : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Onboarding : Route

    @Serializable
    data object AddProfile : Route
}

package ir.salamat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ir.salamat.navigation.BottomNavItem
import ir.salamat.navigation.Route
import ir.salamat.ui.components.ProfileAvatar
import ir.salamat.ui.screens.AddProfileScreen
import ir.salamat.ui.screens.FamilyScreen
import ir.salamat.ui.screens.HomeScreen
import ir.salamat.ui.screens.OnboardingScreen
import ir.salamat.ui.screens.QuickToolsScreen
import ir.salamat.ui.screens.SettingsScreen
import ir.salamat.ui.screens.member.MemberDetailScreen
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    viewModel: AppViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var hasDeterminedStart by remember { mutableStateOf(false) }

    // Refresh notification permission state on every resume from OS settings
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.checkNotificationPermission()
    }

    // First-launch onboarding check
    LaunchedEffect(state.isLoading, state.profiles) {
        if (!state.isLoading && !hasDeterminedStart) {
            hasDeterminedStart = true
            if (state.profiles.isEmpty()) {
                navController.navigate(Route.Onboarding) {
                    popUpTo(Route.Home) { inclusive = true }
                }
            }
        }
    }

    if (state.isLoading && !hasDeterminedStart) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val isBottomBarVisible = BottomNavItem.entries.any { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }

    Scaffold(
        topBar = {
            if (isBottomBarVisible) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (state.isPersian) "سلامت" else "Salamat",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        val active = state.activeProfile
                        if (active != null) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clickable {
                                        navController.navigate(Route.Family) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ProfileAvatar(
                                        name = active.name,
                                        avatarColor = active.avatarColor,
                                        avatarPhoto = active.avatarPhoto,
                                        size = 24.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = active.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    BottomNavItem.entries.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
                        val title = if (state.isPersian) item.titleFa else item.titleEn
                        NavigationBarItem(
                            icon = { Icon(imageVector = item.icon, contentDescription = title) },
                            label = { Text(text = title, style = MaterialTheme.typography.labelSmall) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable<Route.Onboarding> {
                OnboardingScreen(
                    isPersian = state.isPersian,
                    onGetStarted = {
                        navController.navigate(Route.AddProfile)
                    },
                    onSkip = {
                        navController.navigate(Route.Home) {
                            popUpTo(Route.Onboarding) { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.Home> {
                HomeScreen(
                    state = state,
                    onNavigateToFamily = {
                        navController.navigate(Route.Family) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToQuickTools = {
                        navController.navigate(Route.QuickTools) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToAddProfile = {
                        navController.navigate(Route.AddProfile)
                    },
                    onNavigateToMemberDetail = { profileId ->
                        navController.navigate(Route.MemberDetail(profileId))
                    }
                )
            }

            composable<Route.Family> {
                FamilyScreen(
                    state = state,
                    onSelectProfile = { profileId ->
                        viewModel.selectProfile(profileId)
                    },
                    onNavigateToAddProfile = {
                        navController.navigate(Route.AddProfile)
                    },
                    onNavigateToMemberDetail = { profileId ->
                        navController.navigate(Route.MemberDetail(profileId))
                    },
                    onNavigateToEditProfile = { profileId ->
                        navController.navigate(Route.EditProfile(profileId))
                    },
                    onDeleteProfile = { profileId ->
                        viewModel.deleteProfile(profileId)
                    }
                )
            }

            composable<Route.QuickTools> {
                QuickToolsScreen(state = state)
            }

            composable<Route.Settings> {
                SettingsScreen(
                    state = state,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onSetLanguage = { viewModel.setLanguage(it) },
                    onUpdateNotificationPreferences = { viewModel.updateNotificationPreferences(it) },
                    onCheckNotificationPermission = { viewModel.checkNotificationPermission() },
                    onRequestNotificationPermission = { viewModel.requestNotificationPermission() },
                    onSendTestNotification = { viewModel.sendTestNotification() },
                    onClearTestNotificationMessage = { viewModel.clearTestNotificationMessage() }
                )
            }

            composable<Route.AddProfile> {
                AddProfileScreen(
                    isPersian = state.isPersian,
                    onBack = { navController.popBackStack() },
                    onSaveProfile = { name, birthDate, gender, type, avatarColor, avatarPhoto ->
                        viewModel.addProfile(name, birthDate, gender, type, avatarColor, avatarPhoto) {
                            navController.navigate(Route.Home) {
                                popUpTo(Route.Home) { inclusive = false }
                            }
                        }
                    }
                )
            }

            composable<Route.EditProfile> { backStackEntry ->
                val editRoute = backStackEntry.toRoute<Route.EditProfile>()
                val profile = state.profiles.find { it.id == editRoute.profileId }
                if (profile != null) {
                    AddProfileScreen(
                        isPersian = state.isPersian,
                        initialProfile = profile,
                        onBack = { navController.popBackStack() },
                        onSaveProfile = { name, birthDate, gender, type, avatarColor, avatarPhoto ->
                            viewModel.updateProfile(profile.id, name, birthDate, gender, type, avatarColor, avatarPhoto) {
                                navController.popBackStack()
                            }
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }

            composable<Route.MemberDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.MemberDetail>()
                MemberDetailScreen(
                    profileId = route.profileId,
                    isPersian = state.isPersian,
                    onBack = { navController.popBackStack() },
                    onNavigateToEditProfile = { profileId ->
                        navController.navigate(Route.EditProfile(profileId))
                    },
                    onDeleteProfile = { profileId ->
                        viewModel.deleteProfile(profileId)
                    }
                )
            }
        }
    }
}

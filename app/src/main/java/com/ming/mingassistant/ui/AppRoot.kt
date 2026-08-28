package com.ming.mingassistant.ui

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ming.mingassistant.data.Session
import com.ming.mingassistant.ui.auth.AuthScreen
import com.ming.mingassistant.ui.gifts.GiftsScreen
import com.ming.mingassistant.ui.home.HomeScreen
import com.ming.mingassistant.ui.profile.ProfileScreen
import com.ming.mingassistant.ui.songs.SongsScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.ming.mingassistant.data.AuthRepository
import com.ming.mingassistant.data.GiftRepository
import com.ming.mingassistant.data.LiveRepository
import com.ming.mingassistant.data.SessionStore
import com.ming.mingassistant.data.SongRepository
import com.ming.mingassistant.ui.auth.AuthViewModel
import com.ming.mingassistant.ui.gifts.GiftsViewModel
import com.ming.mingassistant.ui.home.HomeViewModel
import com.ming.mingassistant.ui.profile.ProfileViewModel
import com.ming.mingassistant.ui.songs.SongsViewModel

/** Top-level entry: shows login/register when no session, otherwise the 4-tab main scaffold. */
@Composable
fun AppRoot(session: Session?) {
    val context = LocalContext.current
    val sessionStore = SessionStore(context.applicationContext)

    val authFactory = viewModelFactory {
        initializer { AuthViewModel(AuthRepository(sessionStore)) }
    }
    val homeFactory = viewModelFactory {
        initializer { HomeViewModel(LiveRepository()) }
    }
    val songsFactory = viewModelFactory {
        initializer { SongsViewModel(SongRepository()) }
    }
    val giftsFactory = viewModelFactory {
        initializer { GiftsViewModel(GiftRepository()) }
    }
    val profileFactory = viewModelFactory {
        initializer { ProfileViewModel(sessionStore) }
    }

    if (session == null) {
        AuthScreen(factory = authFactory, onLoggedIn = { /* session flow flips switch */ })
    } else {
        MainScaffold(
            session = session,
            homeFactory = homeFactory,
            songsFactory = songsFactory,
            giftsFactory = giftsFactory,
            profileFactory = profileFactory,
        )
    }
}

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

// 底部导航局部配色（不改全局主题）
private val NavAccent = Color(0xFF7C4DFF)
private val NavIndicator = Color(0xFFF3EDFF)
private val NavMuted = Color(0xFF6F6B78)

private val tabs = listOf(
    TabItem("home", "首页", Icons.Filled.Home),
    TabItem("songs", "直播歌单", Icons.Filled.PlayArrow),
    TabItem("gifts", "舰礼收集", Icons.Filled.Star),
    TabItem("profile", "个人中心", Icons.Filled.Person),
)

@Composable
private fun MainScaffold(
    session: Session,
    homeFactory: ViewModelProvider.Factory,
    songsFactory: ViewModelProvider.Factory,
    giftsFactory: ViewModelProvider.Factory,
    profileFactory: ViewModelProvider.Factory,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier.defaultMinSize(minHeight = 68.dp),
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavAccent,
                            selectedTextColor = NavAccent,
                            indicatorColor = NavIndicator,
                            unselectedIconColor = NavMuted,
                            unselectedTextColor = NavMuted,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding),
        ) {
            composable("home") { HomeScreen(factory = homeFactory) }
            composable("songs") { SongsScreen(factory = songsFactory) }
            composable("gifts") { GiftsScreen(factory = giftsFactory, ownUid = session.bilibiliUid) }
            composable("profile") { ProfileScreen(session = session, factory = profileFactory) }
        }
    }
}
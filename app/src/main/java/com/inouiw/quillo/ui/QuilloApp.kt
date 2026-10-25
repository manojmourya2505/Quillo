package com.inouiw.quillo.ui

import androidx.compose.ui.res.stringResource
import com.inouiw.quillo.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.inouiw.quillo.data.AppData
import com.inouiw.quillo.data.AppPrefs
import com.inouiw.quillo.ui.screens.UnlockScreen
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.inouiw.quillo.ui.screens.AddCardScreen
import com.inouiw.quillo.ui.screens.ActivityScreen
import com.inouiw.quillo.ui.screens.AskScreen
import com.inouiw.quillo.ui.screens.CardsScreen
import com.inouiw.quillo.ui.screens.HomeScreen
import com.inouiw.quillo.ui.screens.IntroScreen
import com.inouiw.quillo.ui.screens.LoginScreen
import com.inouiw.quillo.ui.screens.NotificationsScreen
import com.inouiw.quillo.ui.screens.PayBillScreen
import com.inouiw.quillo.ui.screens.ProfileScreen
import com.inouiw.quillo.ui.screens.RequestScreen
import com.inouiw.quillo.ui.screens.SavingsScreen
import com.inouiw.quillo.ui.screens.SendScreen
import com.inouiw.quillo.ui.screens.SplashScreen
import com.inouiw.quillo.ui.screens.TopUpScreen
import com.inouiw.quillo.ui.theme.QCard
import com.inouiw.quillo.ui.theme.QGreenInk
import com.inouiw.quillo.ui.theme.QInk
import com.inouiw.quillo.ui.theme.QMint
import com.inouiw.quillo.ui.theme.QMuted
import com.inouiw.quillo.ui.theme.QSurface

object Routes {
    const val SPLASH = "splash"
    const val INTRO = "intro"
    const val LOGIN = "login"
    const val HOME = "home"
    const val CARDS = "cards"
    const val ADD_CARD = "add_card"
    const val SAVINGS = "savings"
    const val ACTIVITY = "activity"
    const val ASK = "ask"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val SEND = "send"
    const val REQUEST = "request"
    const val TOP_UP = "top_up"
    const val PAY_BILL = "pay_bill"
    const val UNLOCK = "unlock"
}

private val tabRoutes = setOf(Routes.HOME, Routes.CARDS, Routes.SAVINGS, Routes.ACTIVITY)

@Composable
fun QuilloApp() {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    LaunchedEffect(Unit) { AppData.refresh() }
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route

    fun go(target: String) {
        nav.navigate(target) {
            if (target in tabRoutes) {
                popUpTo(Routes.HOME) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        containerColor = QSurface,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom),
        bottomBar = {
            if (route != null && route in tabRoutes) {
                QuilloBottomBar(
                    selected = route,
                    onTab = { go(it) },
                    onAsk = { go(Routes.ASK) },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
        NavHost(
            navController = nav,
            startDestination = Routes.SPLASH,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onDone = {
                    val target = when {
                        !AppPrefs.introSeen(ctx) -> Routes.INTRO
                        Firebase.auth.currentUser == null -> Routes.LOGIN
                        AppPrefs.biometricEnabled(ctx) -> Routes.UNLOCK
                        else -> Routes.HOME
                    }
                    nav.navigate(target) { popUpTo(Routes.SPLASH) { inclusive = true } }
                })
            }
            composable(Routes.INTRO) {
                IntroScreen(onFinish = {
                    AppPrefs.setIntroSeen(ctx)
                    nav.navigate(Routes.LOGIN) { popUpTo(Routes.INTRO) { inclusive = true } }
                })
            }
            composable(Routes.UNLOCK) {
                UnlockScreen(
                    onUnlocked = {
                        scope.launch { AppData.refresh() }
                        nav.navigate(Routes.HOME) { popUpTo(Routes.UNLOCK) { inclusive = true } }
                    },
                    onUsePassword = {
                        nav.navigate(Routes.LOGIN) { popUpTo(Routes.UNLOCK) { inclusive = true } }
                    },
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(onSignIn = {
                    nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                })
            }
            composable(Routes.HOME) { HomeScreen(onNavigate = { go(it) }) }
            composable(Routes.CARDS) { CardsScreen(onNavigate = { go(it) }) }
            composable(Routes.ADD_CARD) {
                AddCardScreen(onSaved = { nav.popBackStack() }, onBack = { nav.popBackStack() })
            }
            composable(Routes.SAVINGS) { SavingsScreen() }
            composable(Routes.ACTIVITY) { ActivityScreen() }
            composable(Routes.ASK) { AskScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onBack = { nav.popBackStack() },
                    onSignOut = {
                        Firebase.auth.signOut()
                        AppData.clear()
                        nav.navigate(Routes.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
                    },
                )
            }
            composable(Routes.SEND) { SendScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.REQUEST) { RequestScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.TOP_UP) { TopUpScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.PAY_BILL) { PayBillScreen(onBack = { nav.popBackStack() }) }
        }
        if (AppData.loadFailed) {
            LoadErrorBanner { scope.launch { AppData.refresh() } }
        }
        }
    }
}

@Composable
private fun QuilloBottomBar(
    selected: String,
    onTab: (String) -> Unit,
    onAsk: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = QCard,
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TabItem(stringResource(R.string.tab_home), Icons.Filled.Home, selected == Routes.HOME) { onTab(Routes.HOME) }
                TabItem(stringResource(R.string.tab_cards), Icons.Filled.CreditCard, selected == Routes.CARDS) { onTab(Routes.CARDS) }
                Spacer(Modifier.width(64.dp))
                TabItem(stringResource(R.string.tab_savings), Icons.Filled.Savings, selected == Routes.SAVINGS) { onTab(Routes.SAVINGS) }
                TabItem(stringResource(R.string.tab_activity), Icons.Filled.History, selected == Routes.ACTIVITY) { onTab(Routes.ACTIVITY) }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(64.dp)
                .clip(CircleShape)
                .background(QInk)
                .clickable(onClick = onAsk),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = stringResource(R.string.ask_quillo), tint = Color.White)
        }
    }
}

@Composable
private fun TabItem(label: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (active) QMint else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = if (active) QGreenInk else QMuted)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (active) QInk else QMuted,
        )
    }
}

@Composable
private fun BoxScope.LoadErrorBanner(onRetry: () -> Unit) {
    Text(
        stringResource(R.string.load_failed),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(QInk)
            .clickable(onClick = onRetry)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = Color.White,
        fontSize = 12.sp,
    )
}

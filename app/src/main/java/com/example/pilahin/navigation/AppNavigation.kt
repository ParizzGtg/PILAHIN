package com.example.pilahin.navigation

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pilahin.data.SessionManager
import com.example.pilahin.ui.auth.AuthViewModel
import com.example.pilahin.ui.auth.GoogleAccountDialog
import com.example.pilahin.ui.auth.GoogleAuthResult
import com.example.pilahin.ui.auth.LoginScreen
import com.example.pilahin.ui.auth.RegisterScreen
import com.example.pilahin.ui.main.MainShell
import com.example.pilahin.ui.onboarding.OnboardingScreen
import com.example.pilahin.ui.splash.SplashScreen
import com.example.pilahin.ui.theme.SplashBackground

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

private enum class GoogleMode { LOGIN, REGISTER }

private fun nameFrom(raw: String): String {
    val base = raw.substringBefore("@").trim()
    return if (base.isBlank() || base.all { it.isDigit() }) "Pengguna"
    else base.replaceFirstChar { it.uppercase() }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val session = remember { SessionManager(context) }

    var googleMode by remember { mutableStateOf<GoogleMode?>(null) }

    fun toast(message: String) =
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    fun goTo(route: String, from: String) {
        navController.navigate(route) {
            popUpTo(from) { inclusive = true }
        }
    }

    fun goHome(from: String, raw: String) {
        val name = nameFrom(raw)
        session.login(name, "@" + name.lowercase())
        goTo(Routes.HOME, from)
    }

    fun logout() {
        session.logout()
        navController.navigate(Routes.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            enterTransition = { fadeIn(tween(350)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(350)) },
            popExitTransition = { fadeOut(tween(200)) }
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onSplashFinished = {
                        if (session.isLoggedIn) goTo(Routes.HOME, Routes.SPLASH)
                        else goTo(Routes.ONBOARDING, Routes.SPLASH)
                    }
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = { goTo(Routes.LOGIN, Routes.ONBOARDING) }
                )
            }

            composable(
                route = Routes.LOGIN,
                enterTransition = {
                    slideInHorizontally(tween(400)) { -it / 4 } + fadeIn(tween(400))
                },
                exitTransition = {
                    slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(250))
                }
            ) {
                LoginScreen(
                    onLogin = { identifier -> goHome(Routes.LOGIN, identifier) },
                    onGoogleLogin = { googleMode = GoogleMode.LOGIN },
                    onNavigateToRegister = { goTo(Routes.REGISTER, Routes.LOGIN) }
                )
            }

            composable(
                route = Routes.REGISTER,
                enterTransition = {
                    slideInHorizontally(tween(400)) { it / 4 } + fadeIn(tween(400))
                },
                exitTransition = {
                    slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(250))
                }
            ) {
                RegisterScreen(
                    onRegister = { email ->
                        authViewModel.registerManual(email) { success ->
                            if (success) {
                                goHome(Routes.REGISTER, email)
                            } else {
                                toast("Email sudah terdaftar. Silakan login.")
                                goTo(Routes.LOGIN, Routes.REGISTER)
                            }
                        }
                    },
                    onGoogleRegister = { googleMode = GoogleMode.REGISTER },
                    onNavigateToLogin = { goTo(Routes.LOGIN, Routes.REGISTER) }
                )
            }

            composable(Routes.HOME) {
                MainShell(
                    userName = session.name,
                    username = session.username,
                    onLogout = { logout() }
                )
            }
        }

        googleMode?.let { mode ->
            GoogleAccountDialog(
                title = if (mode == GoogleMode.LOGIN) "Login dengan Google"
                else "Daftar dengan Google",
                onDismiss = { googleMode = null },
                onConfirm = { email ->
                    googleMode = null
                    when (mode) {
                        GoogleMode.LOGIN -> authViewModel.googleLogin(email) { result ->
                            when (result) {
                                GoogleAuthResult.SUCCESS -> goHome(Routes.LOGIN, email)
                                else -> {
                                    toast("Akun belum terdaftar. Silakan daftar terlebih dahulu.")
                                    goTo(Routes.REGISTER, Routes.LOGIN)
                                }
                            }
                        }

                        GoogleMode.REGISTER -> authViewModel.googleRegister(email) { result ->
                            when (result) {
                                GoogleAuthResult.SUCCESS -> goHome(Routes.REGISTER, email)
                                else -> {
                                    toast("Email sudah terdaftar. Silakan login.")
                                    goTo(Routes.LOGIN, Routes.REGISTER)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pilahin.data.SessionManager
import com.example.pilahin.ui.auth.AuthViewModel
import com.example.pilahin.ui.auth.GoogleMode
import com.example.pilahin.ui.auth.GoogleResult
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val session = remember { SessionManager(context) }

    fun toast(message: String) =
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    fun goTo(route: String, from: String) {
        navController.navigate(route) {
            popUpTo(from) { inclusive = true }
        }
    }

    /** Pilih akun Google, lalu login/daftar ke server. */
    fun google(mode: GoogleMode, from: String) {
        authViewModel.google(context, mode) { result ->
            when (result) {
                GoogleResult.Success -> goTo(Routes.HOME, from)
                GoogleResult.Cancelled -> Unit // pengguna menutup pemilih akun
                is GoogleResult.NotRegistered -> {
                    toast(result.message)
                    if (from == Routes.LOGIN) goTo(Routes.REGISTER, Routes.LOGIN)
                }
                is GoogleResult.Error -> toast(result.message)
            }
        }
    }

    fun logout() {
        session.logout()
        authViewModel.clearGoogleState()
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
                        if (session.isLoggedIn) session.markOnboardingSeen()
                        when {
                            session.isLoggedIn -> goTo(Routes.HOME, Routes.SPLASH)
                            session.onboardingSeen -> goTo(Routes.LOGIN, Routes.SPLASH)
                            else -> goTo(Routes.ONBOARDING, Routes.SPLASH)
                        }
                    }
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        session.markOnboardingSeen()
                        goTo(Routes.LOGIN, Routes.ONBOARDING)
                    }
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
                    onLogin = { identifier, password ->
                        authViewModel.login(identifier, password) { error ->
                            if (error == null) goTo(Routes.HOME, Routes.LOGIN)
                            else toast(error)
                        }
                    },
                    onGoogleLogin = { google(GoogleMode.LOGIN, Routes.LOGIN) },
                    onNavigateToRegister = { goTo(Routes.REGISTER, Routes.LOGIN) },
                    isLoading = authViewModel.isLoading
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
                    onRegister = { username, fullName, email, phone, password ->
                        authViewModel.register(username, fullName, email, phone, password) { error ->
                            if (error == null) goTo(Routes.HOME, Routes.REGISTER)
                            else toast(error)
                        }
                    },
                    onGoogleRegister = { google(GoogleMode.REGISTER, Routes.REGISTER) },
                    onNavigateToLogin = { goTo(Routes.LOGIN, Routes.REGISTER) },
                    isLoading = authViewModel.isLoading
                )
            }

            composable(Routes.HOME) {
                MainShell(onLogout = { logout() })
            }
        }
    }
}
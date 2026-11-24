package com.example.pilahin.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonAdd
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
import com.example.pilahin.ui.admin.AdminScreen
import com.example.pilahin.ui.auth.AuthResult
import com.example.pilahin.ui.auth.AuthViewModel
import com.example.pilahin.ui.auth.GoogleMode
import com.example.pilahin.ui.auth.LoginScreen
import com.example.pilahin.ui.auth.RegisterScreen
import com.example.pilahin.ui.common.AlertData
import com.example.pilahin.ui.common.AlertType
import com.example.pilahin.ui.common.AppAlert
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
    const val ADMIN = "admin"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val session = remember { SessionManager(context) }

    var alert by remember { mutableStateOf<AlertData?>(null) }

    fun goTo(route: String, from: String) {
        navController.navigate(route) {
            popUpTo(from) { inclusive = true }
        }
    }

    /** Masuk ke tampilan sesuai peran: admin atau pengguna biasa. */
    fun enter(from: String) {
        goTo(if (session.isAdmin) Routes.ADMIN else Routes.HOME, from)
    }

    /** Popup setelah daftar berhasil: wajib ke halaman login. */
    fun showRegistered() {
        alert = AlertData(
            type = AlertType.SUCCESS,
            title = "Pendaftaran berhasil",
            message = "Akunmu sudah dibuat. Silakan login untuk mulai memakai PILAHIN.",
            primaryText = "Masuk sekarang",
            onPrimary = { goTo(Routes.LOGIN, Routes.REGISTER) },
            cancelable = false
        )
    }

    /** Popup untuk semua kegagalan login / daftar. */
    fun showFailure(result: AuthResult, from: String) {
        alert = when (result) {
            is AuthResult.NotRegistered -> AlertData(
                type = AlertType.WARNING,
                icon = Icons.Rounded.PersonAdd,
                title = "Akun belum terdaftar",
                message = "Akun ini belum terdaftar. Silakan daftar terlebih dahulu sebelum login.",
                primaryText = "Daftar sekarang",
                onPrimary = { if (from == Routes.LOGIN) goTo(Routes.REGISTER, Routes.LOGIN) },
                secondaryText = "Coba lagi"
            )

            is AuthResult.AlreadyRegistered -> AlertData(
                type = AlertType.INFO,
                title = "Akun sudah terdaftar",
                message = "Akun ini sudah pernah didaftarkan. Silakan login untuk masuk.",
                primaryText = "Ke halaman login",
                onPrimary = { if (from == Routes.REGISTER) goTo(Routes.LOGIN, Routes.REGISTER) },
                secondaryText = "Tutup"
            )

            is AuthResult.Error -> AlertData(
                type = AlertType.ERROR,
                title = if (from == Routes.LOGIN) "Gagal masuk" else "Gagal mendaftar",
                message = result.message,
                primaryText = "Coba lagi"
            )

            else -> null
        }
    }

    /** Pilih akun Google, lalu login atau daftar ke server. */
    fun google(mode: GoogleMode, from: String) {
        authViewModel.google(context, mode) { result ->
            when (result) {
                AuthResult.Success ->
                    if (mode == GoogleMode.REGISTER) showRegistered()
                    else enter(from)
                AuthResult.Cancelled -> Unit // pengguna menutup pemilih akun
                else -> showFailure(result, from)
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
                            session.isLoggedIn -> enter(Routes.SPLASH)
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
                        authViewModel.login(identifier, password) { result ->
                            if (result == AuthResult.Success) enter(Routes.LOGIN)
                            else showFailure(result, Routes.LOGIN)
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
                        authViewModel.register(username, fullName, email, phone, password) { result ->
                            if (result == AuthResult.Success) showRegistered()
                            else showFailure(result, Routes.REGISTER)
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

            composable(Routes.ADMIN) {
                AdminScreen(onLogout = { logout() })
            }
        }

        alert?.let { data ->
            AppAlert(data = data, onDismiss = { alert = null })
        }
    }
}
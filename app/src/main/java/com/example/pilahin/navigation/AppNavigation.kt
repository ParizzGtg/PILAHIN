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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pilahin.ui.auth.LoginScreen
import com.example.pilahin.ui.auth.RegisterScreen
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

    val googleSoon = {
        Toast.makeText(context, "Login Google segera hadir", Toast.LENGTH_SHORT).show()
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
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
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
                    onLogin = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onGoogleLogin = googleSoon,
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
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
                    onRegister = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    },
                    onGoogleRegister = googleSoon,
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Beranda (segera dibuat)")
                }
            }
        }
    }
}
package com.example.pilahin.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.ui.theme.PilahinTheme
import com.example.pilahin.ui.theme.SplashBackground
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val focusManager = LocalFocusManager.current

    val identifierError =
        if (showErrors && identifier.isBlank()) "Isi username, email, atau no HP" else null
    val passwordError =
        if (showErrors && password.isBlank()) "Password tidak boleh kosong" else null

    fun submit() {
        showErrors = true
        if (identifier.isNotBlank() && password.isNotBlank()) {
            focusManager.clearFocus()
            onLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthLogo(modifier = Modifier.authEntrance(started, 0))

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.authEntrance(started, 100),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selamat datang kembali",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Masuk untuk mulai memilah sampah dengan benar.",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        AuthTextField(
            value = identifier,
            onValueChange = { identifier = it },
            label = "Username / Email / No HP",
            icon = Icons.Rounded.Person,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            error = identifierError,
            modifier = Modifier.authEntrance(started, 200)
        )

        Spacer(Modifier.height(8.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            icon = Icons.Rounded.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            error = passwordError,
            onDone = { submit() },
            modifier = Modifier.authEntrance(started, 280)
        )

        Spacer(Modifier.height(12.dp))

        PrimaryButton(
            text = "Login",
            onClick = { submit() },
            modifier = Modifier.authEntrance(started, 360)
        )

        Spacer(Modifier.height(10.dp))

        OrDivider(modifier = Modifier.authEntrance(started, 420))

        Spacer(Modifier.height(10.dp))

        GoogleButton(
            text = "Login dengan Google",
            onClick = onGoogleLogin,
            modifier = Modifier.authEntrance(started, 480)
        )

        Spacer(Modifier.height(4.dp))

        AuthSwitchText(
            question = "Belum punya akun?",
            action = "Daftar",
            onClick = onNavigateToRegister,
            modifier = Modifier.authEntrance(started, 540)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    PilahinTheme {
        LoginScreen(onLogin = {}, onGoogleLogin = {}, onNavigateToRegister = {})
    }
}
package com.example.pilahin.ui.auth

import android.util.Patterns
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
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun RegisterScreen(
    onRegister: () -> Unit,
    onGoogleRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val focusManager = LocalFocusManager.current

    val usernameError = when {
        !showErrors -> null
        username.isBlank() -> "Username tidak boleh kosong"
        username.length < 4 -> "Minimal 4 karakter"
        else -> null
    }
    val nameError =
        if (showErrors && fullName.isBlank()) "Nama lengkap tidak boleh kosong" else null
    val emailError = when {
        !showErrors -> null
        email.isBlank() -> "Email tidak boleh kosong"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Format email tidak valid"
        else -> null
    }
    val phoneError = when {
        !showErrors -> null
        phone.isBlank() -> "No HP tidak boleh kosong"
        phone.length !in 9..15 -> "No HP harus 9-15 angka"
        else -> null
    }
    val passwordError = when {
        !showErrors -> null
        password.isBlank() -> "Password tidak boleh kosong"
        password.length < 6 -> "Minimal 6 karakter"
        else -> null
    }
    val confirmError = when {
        !showErrors -> null
        confirmPassword.isBlank() -> "Ulangi password Anda"
        confirmPassword != password -> "Password tidak sama"
        else -> null
    }

    fun submit() {
        showErrors = true
        val valid = username.length >= 4 &&
                fullName.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                phone.length in 9..15 &&
                password.length >= 6 &&
                confirmPassword == password
        if (valid) {
            focusManager.clearFocus()
            onRegister()
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
                text = "Buat akun baru",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Daftar dan mulai kebiasaan baik untuk bumi.",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))

        AuthTextField(
            value = username,
            onValueChange = { username = it.replace(" ", "") },
            label = "Username",
            icon = Icons.Rounded.Person,
            error = usernameError,
            modifier = Modifier.authEntrance(started, 180)
        )
        Spacer(Modifier.height(8.dp))

        AuthTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = "Nama lengkap",
            icon = Icons.Rounded.Badge,
            error = nameError,
            modifier = Modifier.authEntrance(started, 230)
        )
        Spacer(Modifier.height(8.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = "Email",
            icon = Icons.Rounded.Email,
            keyboardType = KeyboardType.Email,
            error = emailError,
            modifier = Modifier.authEntrance(started, 280)
        )
        Spacer(Modifier.height(8.dp))

        AuthTextField(
            value = phone,
            onValueChange = { input -> phone = input.filter { it.isDigit() } },
            label = "No HP",
            icon = Icons.Rounded.Phone,
            keyboardType = KeyboardType.Phone,
            error = phoneError,
            modifier = Modifier.authEntrance(started, 330)
        )
        Spacer(Modifier.height(8.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            icon = Icons.Rounded.Lock,
            isPassword = true,
            error = passwordError,
            modifier = Modifier.authEntrance(started, 380)
        )
        Spacer(Modifier.height(8.dp))

        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Konfirmasi password",
            icon = Icons.Rounded.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            error = confirmError,
            onDone = { submit() },
            modifier = Modifier.authEntrance(started, 430)
        )

        Spacer(Modifier.height(16.dp))

        PrimaryButton(
            text = "Daftar",
            onClick = { submit() },
            modifier = Modifier.authEntrance(started, 480)
        )

        Spacer(Modifier.height(20.dp))

        OrDivider(modifier = Modifier.authEntrance(started, 520))

        Spacer(Modifier.height(20.dp))

        GoogleButton(
            text = "Daftar dengan Google",
            onClick = onGoogleRegister,
            modifier = Modifier.authEntrance(started, 560)
        )

        Spacer(Modifier.height(16.dp))

        AuthSwitchText(
            question = "Sudah punya akun?",
            action = "Login",
            onClick = onNavigateToLogin,
            modifier = Modifier.authEntrance(started, 600)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    PilahinTheme {
        RegisterScreen(onRegister = {}, onGoogleRegister = {}, onNavigateToLogin = {})
    }
}
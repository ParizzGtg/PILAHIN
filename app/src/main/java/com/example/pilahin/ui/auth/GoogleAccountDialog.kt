package com.example.pilahin.ui.auth

import android.util.Patterns
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.TextMuted

@Composable
fun GoogleAccountDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun submit() {
        if (isValid) onConfirm(email.trim()) else showError = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Masukkan email Google Anda (simulasi, akan diganti pemilih akun Google asli).",
                    color = TextMuted
                )
                Spacer(Modifier.height(8.dp))
                AuthTextField(
                    value = email,
                    onValueChange = { email = it.trim(); showError = false },
                    label = "Email Google",
                    icon = Icons.Rounded.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    error = if (showError) "Format email tidak valid" else null,
                    onDone = { submit() }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { submit() }) {
                Text("Lanjut", color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted)
            }
        }
    )
}
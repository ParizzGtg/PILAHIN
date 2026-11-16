package com.example.pilahin.ui.menu

import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pilahin.ui.auth.AuthTextField
import com.example.pilahin.ui.auth.PrimaryButton
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.ChipBorder
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

private val Danger = Color(0xFFC62828)

@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf(profile.name) }
    var username by remember { mutableStateOf(profile.username) }
    var email by remember { mutableStateOf(profile.email) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var stagedPhoto by remember { mutableStateOf<String?>(null) }
    var showErrors by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.stagePhoto(uri) { stagedPhoto = it }
    }

    val nameError = if (showErrors && name.isBlank()) "Nama tidak boleh kosong" else null
    val usernameError = when {
        !showErrors -> null
        username.isBlank() -> "Username tidak boleh kosong"
        username.length < 4 -> "Minimal 4 karakter"
        else -> null
    }
    val emailError = when {
        !showErrors -> null
        email.isBlank() -> "Email tidak boleh kosong"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Format email tidak valid"
        else -> null
    }
    val passwordError = when {
        !showErrors || newPassword.isEmpty() -> null
        newPassword.length < 6 -> "Minimal 6 karakter"
        else -> null
    }
    val confirmError = when {
        !showErrors -> null
        confirmPassword != newPassword -> "Password tidak sama"
        else -> null
    }

    fun submit() {
        showErrors = true
        val valid = name.isNotBlank() &&
                username.length >= 4 &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                (newPassword.isEmpty() || newPassword.length >= 6) &&
                confirmPassword == newPassword
        if (!valid || saving) return
        saving = true
        focusManager.clearFocus()
        viewModel.save(
            name = name,
            username = username,
            email = email,
            newPassword = newPassword.ifEmpty { null },
            stagedPhoto = stagedPhoto,
            onDone = {
                Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                onSaved()
            },
            onError = { message ->
                saving = false
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        )
    }

    val shownPhoto = stagedPhoto ?: profile.photoPath

    ScreenColumn(title = "Edit Profil", onBack = onBack) {
        // Foto profil
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .reveal(0),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clickable {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    if (shownPhoto != null) {
                        AsyncImage(
                            model = photoModel(shownPhoto),
                            contentDescription = "Foto profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(110.dp)
                        )
                    } else {
                        Text(
                            text = name.take(1).uppercase().ifEmpty { "?" },
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Lime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CameraAlt, "Ganti foto", tint = GreenDark, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Ketuk untuk mengganti foto", fontSize = 12.sp, color = TextMuted)
        }

        Spacer(Modifier.height(16.dp))

        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nama lengkap",
            icon = Icons.Rounded.Badge,
            error = nameError,
            modifier = Modifier.reveal(1)
        )
        Spacer(Modifier.height(8.dp))
        AuthTextField(
            value = username,
            onValueChange = { username = it.replace(" ", "") },
            label = "Username",
            icon = Icons.Rounded.Person,
            error = usernameError,
            modifier = Modifier.reveal(2)
        )
        Spacer(Modifier.height(8.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = "Email",
            icon = Icons.Rounded.Email,
            keyboardType = KeyboardType.Email,
            error = emailError,
            modifier = Modifier.reveal(3)
        )

        Spacer(Modifier.height(10.dp))
        Column(modifier = Modifier.reveal(4)) {
            Text("Ganti password", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(
                text = "Kosongkan jika tidak ingin mengganti password.",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        AuthTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = "Password baru",
            icon = Icons.Rounded.Lock,
            isPassword = true,
            error = passwordError,
            modifier = Modifier.reveal(5)
        )
        Spacer(Modifier.height(8.dp))
        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Konfirmasi password baru",
            icon = Icons.Rounded.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            error = confirmError,
            onDone = { submit() },
            modifier = Modifier.reveal(6)
        )

        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.reveal(7)) {
            PrimaryButton(
                text = if (saving) "Menyimpan..." else "Simpan Perubahan",
                onClick = { submit() }
            )
        }

        // ===== Hapus akun: terpisah di bagian paling bawah =====
        Spacer(Modifier.height(36.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .reveal(8)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Danger.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text("Hapus akun", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Menghapus akun akan menghilangkan profil, riwayat scan, dan request jemput secara permanen. Tindakan ini tidak dapat dibatalkan.",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, Danger),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.DeleteForever, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Hapus akun saya", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.imePadding().height(28.dp))
    }

    if (showDeleteDialog) {
        var confirmText by remember { mutableStateOf("") }
        val canDelete = confirmText.trim() == "HAPUS"
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            title = { Text("Hapus akun permanen?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Semua data akunmu akan dihapus dan tidak bisa dikembalikan.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Ketik HAPUS untuk melanjutkan.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Danger,
                            unfocusedBorderColor = ChipBorder,
                            cursorColor = Danger,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = canDelete,
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text(
                        "Hapus Akun",
                        color = if (canDelete) Danger else TextMuted.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            }
        )
    }
}
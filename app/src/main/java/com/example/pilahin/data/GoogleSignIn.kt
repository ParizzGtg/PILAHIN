package com.example.pilahin.data

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/** Dilempar saat pengguna menutup pemilih akun. Tidak perlu ditampilkan sebagai error. */
class GoogleCancelledException : Exception("Dibatalkan")

/**
 * Menampilkan pemilih akun Google asli, lalu mengambil ID token
 * untuk dicek oleh backend (/api/google).
 */
object GoogleSignIn {

    private const val TAG = "PilahinGoogle"

    // GANTI dengan WEB Client ID dari Google Cloud Console (bukan Android Client ID)
    const val WEB_CLIENT_ID = "ISI_WEB_CLIENT_ID.apps.googleusercontent.com"

    /** activityContext harus context Activity agar pemilih akun bisa tampil. */
    suspend fun getIdToken(activityContext: Context): Result<String> {
        val option = GetSignInWithGoogleOption.Builder(serverClientId = WEB_CLIENT_ID).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val result = CredentialManager.create(activityContext)
                .getCredential(context = activityContext, request = request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                Result.failure(Exception("Jenis akun tidak didukung"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Dibatalkan: ${e.message}", e)
            // Hanya diam kalau pengguna sendiri yang menutup pemilih akun.
            // Error lain (mis. "[16] Account reauth failed" karena SHA-1 belum
            // terdaftar) juga dilaporkan Google sebagai "dibatalkan".
            if (e.message.orEmpty().contains("cancelled by the user", ignoreCase = true)) {
                Result.failure(GoogleCancelledException())
            } else {
                Result.failure(Exception("Login Google gagal: ${e.message ?: "dibatalkan oleh sistem"}"))
            }
        } catch (e: NoCredentialException) {
            Log.w(TAG, "Tidak ada akun", e)
            Result.failure(Exception("Tidak ada akun Google di HP ini. Tambahkan akun di Pengaturan."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException type=${e.type}", e)
            Result.failure(Exception("Login Google gagal: ${e.message ?: e.type}"))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Token tidak valid", e)
            Result.failure(Exception("Respon Google tidak valid"))
        }
    }

    /** Dipanggil saat logout, supaya pilihan akun tidak diingat. */
    suspend fun clearState(context: Context) {
        runCatching {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        }
    }
}
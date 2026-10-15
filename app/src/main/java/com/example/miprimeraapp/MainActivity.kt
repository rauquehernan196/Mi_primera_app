package com.example.miprimeraapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.miprimeraapp.data.FirebaseRepository
import com.example.miprimeraapp.ui.components.AppTutorialDialog
import com.example.miprimeraapp.ui.components.ConsentAndSecurityDialog
import com.example.miprimeraapp.ui.components.PasswordRecoveryDialog
import com.example.miprimeraapp.ui.navigation.Screen
import com.example.miprimeraapp.ui.screens.AppThemeMode
import com.example.miprimeraapp.ui.screens.FontSizeScale
import com.example.miprimeraapp.ui.screens.HomeScreen
import com.example.miprimeraapp.ui.screens.LoginScreen
import com.example.miprimeraapp.ui.screens.ProfileScreen
import com.example.miprimeraapp.ui.screens.SettingsScreen
import com.example.miprimeraapp.ui.screens.SplashScreen
import com.example.miprimeraapp.ui.theme.MiPrimeraAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(AppThemeMode.AUTO) }
            var fontSizeScale by remember { mutableStateOf(FontSizeScale.MEDIUM) }

            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.AUTO -> systemDark
            }

            val fontScaleMultiplier = when (fontSizeScale) {
                FontSizeScale.SMALL -> 0.85f
                FontSizeScale.MEDIUM -> 1.0f
                FontSizeScale.LARGE -> 1.15f
            }

            MiPrimeraAppTheme(darkTheme = isDarkTheme) {
                val currentDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = currentDensity.density,
                        fontScale = currentDensity.fontScale * fontScaleMultiplier
                    )
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
                    var showConsentDialog by remember { mutableStateOf(true) }
                    var showTutorial by remember { mutableStateOf(false) }
                    var showRecoveryDialog by remember { mutableStateOf(false) }
                    val usersWhoSawTutorial = remember { mutableSetOf<String>() }

                    val permissionsToRequest = mutableListOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = { _ ->
                            showConsentDialog = false
                        }
                    )

                    // Persistent registered accounts map (Username -> Password)
                    val userAccounts = remember {
                        mutableStateMapOf(
                            "admin" to "1234",
                            "osorno" to "1234"
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            Crossfade(
                                targetState = currentScreen,
                                label = "ScreenTransition",
                                modifier = Modifier.padding(innerPadding)
                            ) { screen ->
                                when (screen) {
                                    is Screen.Splash -> {
                                        SplashScreen(
                                            onTimeout = {
                                                currentScreen = Screen.Login
                                            }
                                        )
                                    }
                                    is Screen.Login -> {
                                        LoginScreen(
                                            onLoginUser = { inputUser, inputPass ->
                                                val lowerUser = inputUser.lowercase()
                                                val storedPass = userAccounts[lowerUser]
                                                val isValidLocal = (storedPass != null && storedPass == inputPass) || (storedPass == null && inputPass.length >= 4)
                                                if (isValidLocal) {
                                                    if (storedPass == null) {
                                                        userAccounts[lowerUser] = inputPass
                                                    }
                                                    currentScreen = Screen.Home(inputUser)
                                                    if (!usersWhoSawTutorial.contains(inputUser)) {
                                                        showTutorial = true
                                                    }
                                                    true
                                                } else {
                                                    false
                                                }
                                            },
                                            onRegisterUser = { _, inputUser, inputPass ->
                                                val lowerUser = inputUser.lowercase()
                                                if (userAccounts.containsKey(lowerUser)) {
                                                    false
                                                } else {
                                                    userAccounts[lowerUser] = inputPass
                                                    currentScreen = Screen.Home(inputUser)
                                                    if (!usersWhoSawTutorial.contains(inputUser)) {
                                                        showTutorial = true
                                                    }
                                                    true
                                                }
                                            },
                                            onGuestLogin = {
                                                currentScreen = Screen.Home("Invitado")
                                                if (!usersWhoSawTutorial.contains("Invitado")) {
                                                    showTutorial = true
                                                }
                                            },
                                            onGoogleLogin = {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    try {
                                                        val credentialManager = CredentialManager.create(this@MainActivity)
                                                        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                                                            serverClientId = "1059955523091-placeholder.apps.googleusercontent.com"
                                                        ).build()
                                                        val request = GetCredentialRequest.Builder()
                                                            .addCredentialOption(signInWithGoogleOption)
                                                            .build()
                                                        val result = credentialManager.getCredential(
                                                            context = this@MainActivity,
                                                            request = request
                                                        )
                                                        val credential = result.credential
                                                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                                            val googleUserName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.id
                                                            currentScreen = Screen.Home(googleUserName)
                                                            if (!usersWhoSawTutorial.contains(googleUserName)) {
                                                                showTutorial = true
                                                            }
                                                        }
                                                    } catch (e: GetCredentialException) {
                                                        val googleUser = "Usuario Google Personal"
                                                        currentScreen = Screen.Home(googleUser)
                                                        if (!usersWhoSawTutorial.contains(googleUser)) {
                                                            showTutorial = true
                                                        }
                                                    } catch (e: Exception) {
                                                        val googleUser = "Usuario Google Personal"
                                                        currentScreen = Screen.Home(googleUser)
                                                        if (!usersWhoSawTutorial.contains(googleUser)) {
                                                            showTutorial = true
                                                        }
                                                    }
                                                }
                                            },
                                            onForgotPassword = {
                                                showRecoveryDialog = true
                                            }
                                        )
                                    }
                                    is Screen.Home -> {
                                        HomeScreen(
                                            username = screen.username,
                                            onLogout = {
                                                currentScreen = Screen.Login
                                            },
                                            onOpenProfile = {
                                                currentScreen = Screen.Profile(screen.username)
                                            },
                                            onOpenSettings = {
                                                currentScreen = Screen.Settings
                                            }
                                        )
                                    }
                                    is Screen.Profile -> {
                                        ProfileScreen(
                                            username = screen.username,
                                            onBack = {
                                                currentScreen = Screen.Home(screen.username)
                                            },
                                            onLogout = {
                                                currentScreen = Screen.Login
                                            }
                                        )
                                    }
                                    is Screen.Settings -> {
                                        SettingsScreen(
                                            currentTheme = themeMode,
                                            onThemeChanged = { themeMode = it },
                                            currentFontSize = fontSizeScale,
                                            onFontSizeChanged = { fontSizeScale = it },
                                            onBack = {
                                                currentScreen = Screen.Home("admin")
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (showConsentDialog) {
                            ConsentAndSecurityDialog(
                                onDismiss = {
                                    finish()
                                },
                                onAccept = {
                                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                                }
                            )
                        } else if (showTutorial) {
                            AppTutorialDialog(
                                onFinish = {
                                    if (currentScreen is Screen.Home) {
                                        usersWhoSawTutorial.add((currentScreen as Screen.Home).username)
                                    }
                                    showTutorial = false
                                }
                            )
                        } else if (showRecoveryDialog) {
                            PasswordRecoveryDialog(
                                onDismiss = {
                                    showRecoveryDialog = false
                                },
                                onSubmitRecovery = { identifier ->
                                    val success = userAccounts.containsKey(identifier.lowercase()) || identifier.contains("@")
                                    if (success) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            FirebaseRepository.sendPasswordReset(identifier)
                                        }
                                    }
                                    success
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.hibe7

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hibe7.data.repository.AuthRepository
import com.example.hibe7.ui.auth.LoginScreen
import com.example.hibe7.ui.auth.RegisterScreen
import com.example.hibe7.ui.auth.SplashScreen
import com.example.hibe7.ui.share.ChatDetailScreen // <-- BU EKLENDİ
import com.example.hibe7.ui.share.ChatScreen
import com.example.hibe7.ui.share.MainScreen
import com.example.hibe7.ui.theme.Hibe7Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Hibe7Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authRepository = remember { AuthRepository() }

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        // --- SPLASH ---
                        composable("splash") {
                            SplashScreen(
                                onAnimationFinished = {
                                    if (authRepository.getCurrentUserUid() != null) {
                                        navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                                    } else {
                                        navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                                    }
                                }
                            )
                        }

                        // --- LOGIN ---
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }

                        // --- REGISTER ---
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    navController.navigate("home") { popUpTo("register") { inclusive = true } }
                                },
                                onNavigateToLogin = { navController.popBackStack() }
                            )
                        }

                        // --- HOME ---
                        composable("home") {
                            MainScreen(
                                onSignOutRequest = {
                                    authRepository.signOut()
                                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                },
                                onNavigateToChat = {
                                    navController.navigate("chat_screen")
                                }
                            )
                        }

                        // --- SOHBET LİSTESİ ---
                        composable("chat_screen") {
                            ChatScreen(
                                onBackClick = { navController.popBackStack() },
                                onChatClick = { channelId ->
                                    // ARTIK BOŞ DEĞİL! Detay sayfasına ID gönderiyoruz
                                    navController.navigate("chat_detail/$channelId")
                                }
                            )
                        }

                        // --- SOHBET DETAYI (YENİ EKLENEN ROTA) ---
                        composable(
                            route = "chat_detail/{channelId}",
                            arguments = listOf(navArgument("channelId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            // Gelen ID'yi alıp ekranı çiziyoruz
                            val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
                            ChatDetailScreen(
                                channelId = channelId,
                                otherUserId = "", // İçeride otomatik bulacak
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
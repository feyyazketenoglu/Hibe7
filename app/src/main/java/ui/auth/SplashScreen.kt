package com.example.hibe7.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.hibe7.R // R sınıfını import etmeyi unutma


val HibeOrange = Color(0xFFFF7043)

@Composable
fun SplashScreen(
    onAnimationFinished: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        delay(2000)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HibeOrange),
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.logo_hibe7_transparent),
            contentDescription = "Hibe7 Logo",
            modifier = Modifier.size(300.dp)
        )
    }
}
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage("Track Expenses", "Easily log your daily spending with smart AI categorization.", R.drawable.img_track_expense),
        OnboardingPage("AI Insights", "Discover money leaks and identify areas where you can save more.", R.drawable.img_ai_insights),
        OnboardingPage("Save More Money", "Set budgets, track subscriptions, and achieve your financial goals.", R.drawable.img_save_money)
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onFinish) {
                Text("Skip", color = MaterialTheme.colorScheme.primary)
            }
        }
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { position ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Image Full Screen or Large Image
                Image(
                    painter = painterResource(id = pages[position].imageResId),
                    contentDescription = pages[position].title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().align(Alignment.Center)
                )

                // Text Overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f), Color.Black)
                            )
                        )
                        .padding(horizontal = 32.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = pages[position].title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = pages[position].description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.Black).padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pager Indicator
            Row {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(if (pagerState.currentPage == iteration) 12.dp else 8.dp)
                    )
                }
            }
            
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onFinish()
                    }
                }
            ) {
                Text(if (pagerState.currentPage == pages.lastIndex) "Get Started" else "Next")
            }
        }
    }
}

data class OnboardingPage(val title: String, val description: String, val imageResId: Int)

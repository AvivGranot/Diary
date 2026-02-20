package com.proactivediary.ui.storelisting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.proactivediary.ui.storelisting.screenshots.Screenshot01Hero
import com.proactivediary.ui.storelisting.screenshots.Screenshot02Celebration
import com.proactivediary.ui.storelisting.screenshots.Screenshot03Journal
import com.proactivediary.ui.storelisting.screenshots.Screenshot04Goals
import com.proactivediary.ui.storelisting.screenshots.Screenshot07Book
import com.proactivediary.ui.storelisting.screenshots.Screenshot08Privacy
import com.proactivediary.ui.theme.ProactiveDiaryTheme

class StoreListingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProactiveDiaryTheme {
                StoreListingPager()
            }
        }
    }
}

private const val PAGE_COUNT = 6

@Composable
private fun StoreListingPager() {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> Screenshot01Hero()
                1 -> Screenshot02Celebration()
                2 -> Screenshot03Journal()
                3 -> Screenshot04Goals()
                4 -> Screenshot07Book()
                5 -> Screenshot08Privacy()
            }
        }

        // Page indicator dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(PAGE_COUNT) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFFFFFFFF)
                            else Color(0xFF888888).copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

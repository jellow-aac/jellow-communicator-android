package com.dsource.jellowscheduler

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class AppBottomBar {
    @Preview(showBackground = true)
    @Composable
    fun Content() {
        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Adjust height as needed
        ) {
            Image( // Background image
                painter = painterResource(id = R.drawable.img_monster), // Replace with your image
                contentDescription = "Background",
                modifier = Modifier
                    .width(204.dp)
                    .height(204.dp)
                    .absoluteOffset(y= 12.dp),
                contentScale = ContentScale.Crop // Adjust scaling as needed
            )
            Image(
                painter = painterResource(id = R.drawable.bottom_wave),
                contentDescription = "Bottom Image",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomPadding)
                    .align(Alignment.BottomStart)
//                    TODO this is static required corrections here
                    .absoluteOffset(y= 12.dp),

//                contentScale = ContentScale.FillWidth // Or other scaling options
            )
            Image(
                painter = painterResource(id = R.drawable.img_icon_dashboard),
                contentDescription = "Bottom Image",
                modifier = Modifier
                    .width(52.dp)
                    .height(32.dp)
                    .padding(bottom = bottomPadding)
                    .align(Alignment.BottomStart)
                    .clickable {

                    },
                contentScale = ContentScale.Crop // Or other scaling options
            )
        }
    }
}

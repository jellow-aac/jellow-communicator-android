package com.dsource.jellowscheduler

import android.content.Context
import android.graphics.Typeface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.dsource.jellowscheduler.ui.theme.AppBarColor
import com.dsource.jellowscheduler.ui.theme.AppBarTextColor
import com.dsource.jellowscheduler.ui.theme.StatusBarColor

class AppToolbar()  {
    @Preview(showBackground = true)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content(context: Context = LocalContext.current.applicationContext) {
        val muktaTypeface = Typeface.createFromAsset(context.assets, "fonts/muktasemibold.ttf")
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.today_s_schedule),
                    color = AppBarTextColor,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(muktaTypeface)
                )
                    },
            actions = {
                IconButton(onClick = { /* Handle menu item 1 */ }) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription ="Edit",
                        tint = StatusBarColor
                        )
                }
                IconButton(onClick = { /* Handle menu item 2 */ }) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription ="Delete",
                        tint = StatusBarColor
                    )
                }
                IconButton(onClick = { /* Handle menu item 3 */ }) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription ="Delete",
                        tint = StatusBarColor
                    )
                }
                IconButton(onClick = { /* Handle menu item 4 */ }) {
                    Icon(
                        Icons.Rounded.DateRange  ,
                        contentDescription ="Delete",
                        tint = StatusBarColor
                    )
                }
                // Add more menu items as needed
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBarColor)
        )
    }
}

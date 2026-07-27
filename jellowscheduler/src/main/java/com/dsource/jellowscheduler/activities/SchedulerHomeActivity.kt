package com.dsource.jellowscheduler.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.dsource.jellowscheduler.AppBottomBar
import com.dsource.jellowscheduler.AppToolbar
import com.dsource.jellowscheduler.activities.ui.ScheduleListItem
import com.dsource.jellowscheduler.datasource.localdb.database.SchedulerDb
import com.dsource.jellowscheduler.repositories.SchedulerRepositoryImpl
import com.dsource.jellowscheduler.ui.theme.SchedulerTheme
import com.dsource.jellowscheduler.ui.theme.StatusBarColor
import com.dsource.jellowscheduler.ui.theme.YellowBackground
import com.dsource.jellowscheduler.viewmodels.SchedulerViewModel
import com.dsource.jellowscheduler.viewmodels.SchedulerViewModelFactory
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SchedulerHomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
//        val database  = SchedulerDb.getDatabase(this@SchedulerHomeActivity)
//        val repository = SchedulerRepositoryImpl(database.scheduleDao())
//        val viewModel = ViewModelProvider(this, SchedulerViewModelFactory(repository))[SchedulerViewModel::class.java]

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemUiController = rememberSystemUiController()
//            useDarkIcons = MaterialTheme.colorScheme.brightness == Brightness.Light

            SideEffect {
                systemUiController.setStatusBarColor(
                    color = StatusBarColor // Your desired status bar color
//                    darkIcons = useDarkIcons // Set to true for dark icons on light backgrounds
                )
            }

            SchedulerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { AppToolbar().Content()},
                    bottomBar = { AppBottomBar().Content() },
                    containerColor = YellowBackground
                )
                { innerPadding ->
                    HomeScreen(
//                        viewModel = viewModel,
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )

                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: SchedulerViewModel = viewModel(),
    name: String,
    modifier: Modifier = Modifier
) {
    val schedules by viewModel.allSchedules.collectAsState(initial = emptyList())

    LazyColumn(modifier = modifier) {
        items(schedules) { schedule ->
            ScheduleListItem().Content(schedule) {
                // Handle action item click here
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SchedulerListPreview() {
    SchedulerTheme {
        val viewModel = viewModel<SchedulerViewModel>()
        HomeScreen(viewModel = viewModel,
            "Android"
        )
    }
}
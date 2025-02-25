package com.nakibul.android.appscheduler.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.nakibul.android.appscheduler.models.AppSchedule
import com.nakibul.android.appscheduler.navigation.Screen
import com.nakibul.android.appscheduler.viewmodels.ScheduleViewModel

@Composable
fun ScheduleListScreen(
    modifier: Modifier = Modifier, navController: NavHostController,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val apps by viewModel.installedApps.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(Unit) {
        if (!viewModel.canScheduleExactAlarms()) {
            viewModel.requestExactAlarmPermission(activity)
        }
    }
    // Show a message if permission is denied
    if (!viewModel.canScheduleExactAlarms()) {
        Text(
            text = "Exact alarm permission is required to schedule apps. Please grant the permission in settings.",
            color = Color.Red,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        // Display the schedule list
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp, top = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "Scheduled Apps",
                    style = TextStyle(fontSize = 24.sp),
                    modifier = Modifier.padding(16.dp)
                )
                ScheduleList(viewModel)
            }

            FloatingActionButton(
                containerColor = Color.Magenta,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(),
                shape = CircleShape,
                onClick = {
                    navController.navigate(Screen.AppListScreenScreen.route)
                },
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    }
}


@Composable
fun ScheduleList(viewModel: ScheduleViewModel) {
    val context = LocalContext.current
    val schedules by viewModel.schedules.collectAsState(initial = emptyList())

    // Filter out executed schedules
    val nonExecutedSchedules = schedules

    LazyColumn {
        items(nonExecutedSchedules) { schedule ->
            ScheduleItem(
                schedule = schedule,
                onCancel = {
                    Toast.makeText(context, "Schedule deleted", Toast.LENGTH_SHORT).show()
                    viewModel.cancelSchedule(schedule)
                },
                onUpdate = { newTime ->
                    val updatedSchedule = schedule.copy(scheduledTime = newTime)
                    viewModel.updateSchedule(updatedSchedule)
                }
            )
        }
    }
}

@Composable
fun ScheduleItem(
    schedule: AppSchedule,
    onCancel: () -> Unit,
    onUpdate: (Long) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { selectedTime ->
                onUpdate(selectedTime)
                showTimePicker = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // App Name
                Text(
                    text = schedule.appName,
                    style = MaterialTheme.typography.bodyLarge
                )
                // Scheduled Time
                Text(
                    text = java.text.SimpleDateFormat("hh:mm a")
                        .format(java.util.Date(schedule.scheduledTime)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Status: ",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = if (!schedule.isExecuted) "Active" else "Executed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (!schedule.isExecuted) Color.Green else Color.Red
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                if (schedule.isExecuted) {
                    Icon(
                        modifier = Modifier.clickable {
                            onUpdate(schedule.scheduledTime)
                        },
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Update Schedule"
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                Icon(
                    modifier = Modifier.clickable {
                        onCancel()
                    },
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel Schedule"
                )
            }
        }
    }
}
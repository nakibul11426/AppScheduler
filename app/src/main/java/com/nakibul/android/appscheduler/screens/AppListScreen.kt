package com.nakibul.android.appscheduler.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.nakibul.android.appscheduler.models.AppInfo
import com.nakibul.android.appscheduler.viewmodels.ScheduleViewModel
import com.nakibul.android.appscheduler.navigation.Screen
import java.util.Calendar

@Composable
fun AppListScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Total installed app: ${installedApps.size.toString()}",
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.W900)
        )

        LazyColumn {
            items(installedApps) { app ->
                InstalledAppItem(
                    app = app,
                    modifier = Modifier,
                    onSchedule = { selectedApp, selectedTime ->
                        viewModel.scheduleApp(
                            selectedApp.packageName,
                            selectedApp.appName,
                            selectedTime
                        )
                        navController.navigate(Screen.ScheduleListScreen.route)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun InstalledAppItem(
    modifier: Modifier = Modifier,
    app: AppInfo,
    onSchedule: (AppInfo, Long) -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value) {
        AppDetailsDialog(
            app = app,
            onDismiss = { showDialog.value = false },
            onSchedule = { selectedTime ->
                onSchedule(app, selectedTime)
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Gray)
            .padding(16.dp)
            .clickable { showDialog.value = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = app.icon.toBitmap().asImageBitmap(),
            contentDescription = app.appName,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = app.appName,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun AppDetailsDialog(
    app: AppInfo,
    onDismiss: () -> Unit,
    onSchedule: (Long) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { selectedTime ->
                onSchedule(selectedTime)
                showTimePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = app.appName) },
        text = {
            Column {
                Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Package: ${app.packageName}")
            }
        },
        confirmButton = {
            Button(onClick = { showTimePicker = true }) {
                Text("Schedule")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    android.app.TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val selectedTime = calendar.apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }.timeInMillis
            onTimeSelected(selectedTime)
        },
        hour,
        minute,
        false
    ).apply {
        setOnDismissListener { onDismiss() }
        show()
    }
}
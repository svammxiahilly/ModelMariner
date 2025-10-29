package com.pranshulgg.clockmaster.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClockDisplayText(use24hr: Boolean = false, showSeconds: Boolean = false) {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {

        var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

        LaunchedEffect(Unit) {
            while (true) {
                val now = System.currentTimeMillis()
                currentTime = now

                val delayMs = 1000 - (now % 1000)
                delay(delayMs)
            }
        }

        val pattern =
            if (use24hr) "HH:mm${if (showSeconds) ":ss" else ""}" else "hh:mm${if (showSeconds) ":ss" else ""}"

        val currentLocalTime = remember {
            SimpleDateFormat(pattern, Locale.getDefault())
        }

        val currentAmPm = remember {
            SimpleDateFormat("a", Locale.getDefault())
        }

        val currentLocalDate = remember {
            SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        }

        val formattedTime = currentLocalTime.format(Date(currentTime))
        val formattedDate = currentLocalDate.format(Date(currentTime))
        val separateAmPm = currentAmPm.format(Date(currentTime))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = formattedTime,
                fontSize = 80.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.alignByBaseline()
            )

            if (!use24hr) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = separateAmPm,
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.onSurface,

                    modifier = Modifier.alignByBaseline()
                )
            }
        }


        Text(
            text = formattedDate,
            fontSize = 22.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface


        )
    }

}

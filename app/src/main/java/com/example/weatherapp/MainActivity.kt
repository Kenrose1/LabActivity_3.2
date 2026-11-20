package com.example.weatherapp

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.Glide
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.ui.theme.WeatherBlue
import com.example.weatherapp.ui.theme.WeatherCard
import com.example.weatherapp.ui.theme.WeatherPurple
import com.example.weatherapp.ui.theme.WeatherSky
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                WeatherApp()
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WeatherApp(weatherViewModel: WeatherViewModel = viewModel()) {
    var city by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("☀  Weather App") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WeatherBlue,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(WeatherBlue, WeatherPurple, WeatherSky))
                )
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = WeatherCard,
                    unfocusedContainerColor = WeatherCard,
                    focusedBorderColor = WeatherPurple,
                    unfocusedBorderColor = WeatherBlue
                ),
                label = { Text("Search city") },
                placeholder = { Text("Enter a city, e.g. New York") },
                trailingIcon = {
                    IconButton(
                        onClick = { scope.launch { weatherViewModel.search(city) } },
                        enabled = city.isNotBlank() && !weatherViewModel.isLoading
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { scope.launch { weatherViewModel.search(city) } }
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            when {
                weatherViewModel.isLoading -> CircularProgressIndicator()
                weatherViewModel.errorMessage != null -> Text(
                    text = weatherViewModel.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                weatherViewModel.weather != null -> WeatherResult(weatherViewModel.weather!!)
                else -> Text(
                    text = "Search for a city to see its current weather.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun WeatherResult(weather: WeatherResponse) {
    val condition = weather.weather.firstOrNull()
    val measurements = weather.main

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WeatherCard.copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(weather.name, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = measurements?.let { "${it.temp.toInt()}°F" } ?: "--",
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(condition?.main ?: "Unknown", style = MaterialTheme.typography.titleLarge)
                    Text(
                        condition?.description?.replaceFirstChar { it.uppercase() } ?: "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                condition?.icon?.let { WeatherIcon(it) }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetail("Feels like", measurements?.feelsLike?.let { "${it.toInt()}°F" } ?: "--")
                WeatherDetail("Humidity", measurements?.humidity?.let { "$it%" } ?: "--")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetail("Wind", weather.wind?.speed?.let { "${it.toInt()} mph" } ?: "--")
                WeatherDetail("Pressure", measurements?.pressure?.let { "$it hPa" } ?: "--")
            }
        }
    }
}

@Composable
private fun WeatherDetail(label: String, value: String) {
    Column(modifier = Modifier.padding(end = 12.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge
        )
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun WeatherIcon(iconCode: String) {
    val context = LocalContext.current
    AndroidView(
        factory = { ImageView(it) },
        modifier = Modifier.size(96.dp),
        update = { imageView ->
            Glide.with(context)
                .load("https://openweathermap.org/img/wn/${iconCode}@2x.png")
                .into(imageView)
        }
    )
}

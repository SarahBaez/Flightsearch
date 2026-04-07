package com.example.flightsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.ui.FlightViewModel
import com.example.flightsearch.ui.theme.FlightsearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlightsearchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FlightSearchApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchApp(
    viewModel: FlightViewModel = viewModel(factory = FlightViewModel.Factory)
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val favoriteList by viewModel.favoritesList.collectAsState()
    val allAirports by viewModel.allAirports.collectAsState()

    var selectedAirport by remember { mutableStateOf<Airport?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                viewModel.updateSearchQuery(it)
                selectedAirport = null
            },
            placeholder = { Text("Ingresa el aeropuerto de salida") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Icono de búsqueda") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.updateSearchQuery("")
                        selectedAirport = null
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Borrar búsqueda")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true,
            shape = RoundedCornerShape(32.dp)
        )

        if (searchQuery.isEmpty()) {
            if (favoriteList.isEmpty()) {
                Text(
                    text = "Tus rutas favoritas aparecerán aquí",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                FavoriteList(
                    favorites = favoriteList,
                    allAirports = allAirports,
                    onFavoriteClick = { departure, destination ->
                        viewModel.toggleFavorite(departure, destination, true)
                    }
                )
            }
        } else if (selectedAirport != null) {
            Text(
                text = "Vuelos desde ${selectedAirport!!.iataCode}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FlightList(
                departureAirport = selectedAirport!!,
                destinationAirports = searchResults.filter { it.id != selectedAirport!!.id },
                favoriteList = favoriteList,
                onFavoriteClick = { departure, destination, isFav ->
                    viewModel.toggleFavorite(departure, destination, isFav)
                }
            )
        } else {
            AirportList(
                airports = searchResults,
                onAirportClick = { airport ->
                    selectedAirport = airport
                }
            )
        }
    }
}

@Composable
fun AirportList(
    airports: List<Airport>,
    onAirportClick: (Airport) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(airports) { airport ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAirportClick(airport) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = airport.iataCode,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(50.dp)
                )
                Text(
                    text = airport.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun FlightList(
    departureAirport: Airport,
    destinationAirports: List<Airport>,
    favoriteList: List<Favorite>,
    onFavoriteClick: (String, String, Boolean) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(destinationAirports) { destination ->
            val isFavorite = favoriteList.any {
                it.departureCode == departureAirport.iataCode &&
                        it.destinationCode == destination.iataCode
            }

            FlightCard(
                departure = departureAirport,
                destination = destination,
                isFavorite = isFavorite,
                onFavoriteClick = {
                    onFavoriteClick(departureAirport.iataCode, destination.iataCode, isFavorite)
                }
            )
        }
    }
}

@Composable
fun FlightCard(
    departure: Airport,
    destination: Airport,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Salida", style = MaterialTheme.typography.labelSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = departure.iataCode, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = departure.name, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Llegada", style = MaterialTheme.typography.labelSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = destination.iataCode, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = destination.name, style = MaterialTheme.typography.bodySmall)
                }
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Favorito",
                    tint = if (isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FavoriteList(
    favorites: List<Favorite>,
    allAirports: List<Airport>,
    onFavoriteClick: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Rutas favoritas",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favorites) { favorite ->
                val departureAirport = allAirports.find { it.iataCode == favorite.departureCode }
                val destinationAirport = allAirports.find { it.iataCode == favorite.destinationCode }

                if (departureAirport != null && destinationAirport != null) {
                    FlightCard(
                        departure = departureAirport,
                        destination = destinationAirport,
                        isFavorite = true,
                        onFavoriteClick = {
                            onFavoriteClick(favorite.departureCode, favorite.destinationCode)
                        }
                    )
                }
            }
        }
    }
}
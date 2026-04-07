package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.FlightSearchApplication
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FlightDao
import com.example.flightsearch.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlightViewModel(
    private val flightDao: FlightDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val searchQuery: StateFlow<String> = userPreferencesRepository.searchQuery
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val favoritesList: StateFlow<List<Favorite>> = flightDao.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAirports: StateFlow<List<Airport>> = flightDao.getAllAirports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchResults = MutableStateFlow<List<Airport>>(emptyList())
    val searchResults: StateFlow<List<Airport>> = _searchResults.asStateFlow()

    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveSearchQuery(query)
            if (query.isNotEmpty()) {
                flightDao.getAirportsByQuery(query).collect {
                    _searchResults.value = it
                }
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    fun toggleFavorite(departure: String, destination: String, isAlreadyFavorite: Boolean) {
        viewModelScope.launch {
            if (isAlreadyFavorite) {
                flightDao.deleteFavoriteRoute(departure, destination)
            } else {
                flightDao.insertFavorite(Favorite(departureCode = departure, destinationCode = destination))
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FlightSearchApplication)
                FlightViewModel(application.database.flightDao(), application.userPreferencesRepository)
            }
        }
    }
}
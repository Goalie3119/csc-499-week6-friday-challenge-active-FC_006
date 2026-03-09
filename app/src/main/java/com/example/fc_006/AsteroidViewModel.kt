package com.example.fc_006

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fc_006.data.Asteroid
import com.example.fc_006.data.RetrofitInstance
import kotlinx.coroutines.launch

sealed class AsteroidUiState {
    object Loading : AsteroidUiState()
    data class Success(val asteroids: List<Asteroid>) : AsteroidUiState()
    data class Error(val message: String) : AsteroidUiState()
}

class AsteroidViewModel : ViewModel() {

    private val _uiState = MutableLiveData<AsteroidUiState>()
    val uiState: LiveData<AsteroidUiState> = _uiState

    fun scanForAsteroids() {
        _uiState.value = AsteroidUiState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.getAsteroids(RetrofitInstance.API_KEY)
                _uiState.value = AsteroidUiState.Success(response.asteroids)
            } catch (e: Exception) {
                _uiState.value = AsteroidUiState.Error("Mission Control lost asteroid tracking data.")
            }
        }
    }
}

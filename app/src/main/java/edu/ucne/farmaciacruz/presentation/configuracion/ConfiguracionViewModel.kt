package edu.ucne.farmaciacruz.presentation.configuracion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.data.local.PreferencesManager
import edu.ucne.farmaciacruz.domain.usecase.login.GetCurrentUserUseCase
import edu.ucne.farmaciacruz.domain.usecase.login.LogoutUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfiguracionViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ConfiguracionState())
    val state: StateFlow<ConfiguracionState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ConfiguracionUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        onEvent(ConfiguracionEvent.LoadUserData)
        onEvent(ConfiguracionEvent.LoadPreferences)
    }

    fun onEvent(event: ConfiguracionEvent) {
        when (event) {
            is ConfiguracionEvent.LoadUserData -> handleLoadUserData()
            is ConfiguracionEvent.LoadPreferences -> handleLoadPreferences()
            is ConfiguracionEvent.ApiUrlChanged -> handleApiUrlChanged(event.newUrl)
            is ConfiguracionEvent.ThemeToggled -> handleThemeToggled()
            is ConfiguracionEvent.ShowLogoutDialog -> handleShowLogoutDialog()
            is ConfiguracionEvent.DismissLogoutDialog -> handleDismissLogoutDialog()
            is ConfiguracionEvent.Logout -> handleLogout()
        }
    }

    private fun handleLoadUserData() {
        viewModelScope.launch {
            try {
                getCurrentUserUseCase().collect { user ->
                    _state.update { it.copy(user = user, error = null) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
                _uiEvent.emit(ConfiguracionUiEvent.ShowError(e.message ?: "Error al cargar usuario"))
            }
        }
    }

    private fun handleLoadPreferences() {
        viewModelScope.launch {
            try {
                preferencesManager.getApiUrl().collect { url ->
                    _state.update { it.copy(apiUrl = url) }
                }
            } catch (e: Exception) {
                _uiEvent.emit(ConfiguracionUiEvent.ShowError("Error al cargar preferencias"))
            }
        }
        viewModelScope.launch {
            try {
                preferencesManager.getThemePreference().collect { isDark ->
                    _state.update { it.copy(isDarkTheme = isDark) }
                }
            } catch (e: Exception) {
                _uiEvent.emit(ConfiguracionUiEvent.ShowError("Error al cargar tema"))
            }
        }
    }

    private fun handleApiUrlChanged(newUrl: String) {
        viewModelScope.launch {
            try {
                preferencesManager.saveApiUrl(newUrl)
                _state.update { it.copy(apiUrl = newUrl) }
                _uiEvent.emit(ConfiguracionUiEvent.ShowSuccess("URL de API actualizada"))
            } catch (e: Exception) {
                _uiEvent.emit(ConfiguracionUiEvent.ShowError("Error al guardar URL"))
            }
        }
    }

    private fun handleThemeToggled() {
        viewModelScope.launch {
            try {
                val newTheme = !_state.value.isDarkTheme
                preferencesManager.saveThemePreference(newTheme)
                _state.update { it.copy(isDarkTheme = newTheme) }

                val message = if (newTheme) "Tema oscuro activado" else "Tema claro activado"
                _uiEvent.emit(ConfiguracionUiEvent.ShowSuccess(message))
            } catch (e: Exception) {
                _uiEvent.emit(ConfiguracionUiEvent.ShowError("Error al cambiar tema"))
            }
        }
    }

    private fun handleShowLogoutDialog() {
        _state.update { it.copy(showLogoutDialog = true) }
    }

    private fun handleDismissLogoutDialog() {
        _state.update { it.copy(showLogoutDialog = false) }
    }

    private fun handleLogout() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                logoutUseCase()

                _state.update {
                    it.copy(
                        isLoading = false,
                        showLogoutDialog = false
                    )
                }

                _uiEvent.emit(ConfiguracionUiEvent.NavigateToLogin)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.emit(ConfiguracionUiEvent.ShowError("Error al cerrar sesión"))
            }
        }
    }
}
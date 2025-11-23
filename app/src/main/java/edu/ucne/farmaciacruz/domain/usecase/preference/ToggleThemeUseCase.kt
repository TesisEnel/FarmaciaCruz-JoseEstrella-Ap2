package edu.ucne.farmaciacruz.domain.usecase.preference

import edu.ucne.farmaciacruz.domain.repository.PreferencesRepository
import javax.inject.Inject

class ToggleThemeUseCase @Inject constructor(
    private val repo: PreferencesRepository
) {
    suspend operator fun invoke(currentTheme: Boolean) {
        repo.saveDarkTheme(!currentTheme)
    }
}
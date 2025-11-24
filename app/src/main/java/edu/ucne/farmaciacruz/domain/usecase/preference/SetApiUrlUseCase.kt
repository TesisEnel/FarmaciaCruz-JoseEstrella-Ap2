package edu.ucne.farmaciacruz.domain.usecase.preference

import edu.ucne.farmaciacruz.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetApiUrlUseCase @Inject constructor(
    private val repo: PreferencesRepository
) {
    suspend operator fun invoke(url: String) {
        repo.saveApiUrl(url)
    }
}

package edu.ucne.farmaciacruz.domain.usecase.preference

import edu.ucne.farmaciacruz.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetApiUrlUseCase @Inject constructor(
    private val repo: PreferencesRepository
) {
    operator fun invoke(): Flow<String> = repo.getApiUrl()
}
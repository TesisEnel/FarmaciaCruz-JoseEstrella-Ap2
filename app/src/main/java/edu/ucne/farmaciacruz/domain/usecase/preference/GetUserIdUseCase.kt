package edu.ucne.farmaciacruz.domain.usecase.preference

import edu.ucne.farmaciacruz.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Int?> = authRepository.getUserData().map { it?.id }
}
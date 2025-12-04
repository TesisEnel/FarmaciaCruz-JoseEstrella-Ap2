package edu.ucne.farmaciacruz.domain.usecase.login

import edu.ucne.farmaciacruz.domain.model.User
import edu.ucne.farmaciacruz.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> {
        return authRepository.getUserData()
    }
}
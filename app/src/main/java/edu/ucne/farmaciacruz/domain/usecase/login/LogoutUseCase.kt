package edu.ucne.farmaciacruz.domain.usecase.login

import edu.ucne.farmaciacruz.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
package edu.ucne.farmaciacruz.domain.usecase.login


import edu.ucne.farmaciacruz.data.repository.AuthRepositoryImpl
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
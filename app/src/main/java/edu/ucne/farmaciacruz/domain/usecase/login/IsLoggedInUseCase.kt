package edu.ucne.farmaciacruz.domain.usecase.login


import edu.ucne.farmaciacruz.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    operator fun invoke(): Flow<Boolean> {
        return authRepository.isLoggedIn()
    }
}
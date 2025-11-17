package edu.ucne.farmaciacruz.domain.usecase.login

import edu.ucne.farmaciacruz.data.repository.AuthRepositoryImpl
import edu.ucne.farmaciacruz.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    operator fun invoke(): Flow<User?> {
        return authRepository.getUserData()
    }
}
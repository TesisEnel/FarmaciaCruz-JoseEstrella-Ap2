package edu.ucne.farmaciacruz.domain.usecase.login

import android.util.Patterns
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RecoveryPasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        if (email.isBlank()) { emit(Resource.Error("Email requerido")); return@flow }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emit(Resource.Error("Email inválido")); return@flow
        }

        repository.recoveryPassword(email).collect { emit(it) }
    }
}
package com.example.aplicacaodecontrolofabrica.features.expedicao

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacaodecontrolofabrica.data.dto.ProntosExpedicaoDto
import com.example.aplicacaodecontrolofabrica.data.repository.FabricaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExpedicaoUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val total: Int = 0,
    val ordens: List<ProntosExpedicaoDto> = emptyList()
)

class ExpedicaoViewModel(
    private val repository: FabricaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpedicaoUiState(isLoading = true))
    val uiState: StateFlow<ExpedicaoUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getProntosExpedicao() }
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            total = response.total,
                            ordens = response.ordens
                        )
                    }
                }
                .onFailure { ex ->
                    Log.e("ExpedicaoVM", "Falha ao carregar expedição", ex)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ex.message ?: "Não foi possível carregar as expedições pendentes."
                        )
                    }
                }
        }
    }
}

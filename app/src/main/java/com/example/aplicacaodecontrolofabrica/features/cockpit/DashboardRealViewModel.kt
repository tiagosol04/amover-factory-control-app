package com.example.aplicacaodecontrolofabrica.features.cockpit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacaodecontrolofabrica.data.dto.DashboardResumoDto
import com.example.aplicacaodecontrolofabrica.data.dto.UtilizadorDto
import com.example.aplicacaodecontrolofabrica.data.mapper.DtoHelpers
import com.example.aplicacaodecontrolofabrica.data.repository.FabricaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardOrderUi(
    val ordemId: Int,
    val numeroOrdem: String,
    val modeloNome: String,
    val estadoLabel: String,
    val prioridadeLabel: String,
    val bloqueada: Boolean,
    val unidadeRegistada: Boolean,
    val vinPendente: Boolean,
    val controloPendente: Boolean
)

data class DashboardActionUi(
    val id: Int,
    val titulo: String,
    val descricao: String,
    val ordemId: Int? = null
)

data class DashboardPessoaUi(
    val id: Int,
    val nome: String,
    val funcao: String,
    val disponibilidade: String,
    val ativo: Boolean
)

data class DashboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Contadores de supervisão
    val totalOrdens: Int = 0,
    val emProducao: Int = 0,
    val bloqueadas: Int = 0,
    val concluidas: Int = 0,
    val semUnidade: Int = 0,
    val controloPendente: Int = 0,
    val vinPendente: Int = 0,
    val alertasAtivos: Int = 0,
    val expedicoesPendentes: Int = 0,
    val equipaAtiva: Int = 0,
    val equipaIndisponivel: Int = 0,

    val acoesImediatas: List<DashboardActionUi> = emptyList(),
    val ordensPrioritarias: List<DashboardOrderUi> = emptyList(),
    val equipaHoje: List<DashboardPessoaUi> = emptyList(),

    val servicosEmAberto: Int = 0,
    val servicosGarantia: Int = 0,
    val servicosAvaria: Int = 0
)

class DashboardRealViewModel(
    private val fabricaRepository: FabricaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { buildDashboard() }
                .onSuccess { result ->
                    _uiState.value = result.copy(isLoading = false)
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ex.message ?: "Não foi possível carregar a visão geral da fábrica."
                        )
                    }
                }
        }
    }

    private suspend fun buildDashboard(): DashboardUiState {
        val dashResumo = runCatching { fabricaRepository.getDashboardResumo() }
            .onFailure { Log.w("DashboardVM", "GET /api/controlo-fabrica/dashboard falhou — usando fallback N+1", it) }
            .getOrNull()

        return if (dashResumo != null) buildFromDashboardResumo(dashResumo) else buildFromNPlus1()
    }

    // ── Caminho otimizado via GET /api/controlo-fabrica/dashboard ──
    private suspend fun buildFromDashboardResumo(dashResumo: DashboardResumoDto): DashboardUiState {
        val utilizadoresDeferred = viewModelScope.async {
            runCatching { fabricaRepository.getUtilizadores() }
                .onFailure { Log.e("DashboardVM", "Falha ao carregar utilizadores", it) }
                .getOrDefault(emptyList())
        }
        val servicosDeferred = viewModelScope.async {
            runCatching { fabricaRepository.getServicosEmAberto() }
                .onFailure { Log.e("DashboardVM", "Falha ao carregar serviços em aberto", it) }
                .getOrNull()
        }
        val expedicaoDeferred = viewModelScope.async {
            runCatching { fabricaRepository.getProntosExpedicao() }
                .onFailure { Log.e("DashboardVM", "Falha ao carregar expedição pendente", it) }
                .getOrNull()
        }
        val alertasDeferred = viewModelScope.async {
            runCatching { fabricaRepository.getAlertas() }
                .onFailure { Log.e("DashboardVM", "Falha ao carregar alertas", it) }
                .getOrNull()
        }

        val cards = mutableListOf<DashboardOrderUi>()
        val acoes = mutableListOf<DashboardActionUi>()
        var concluidas = 0

        for (ordemDash in dashResumo.ordens) {
            val ordemId = ordemDash.ordemId ?: continue
            val estado = ordemDash.estado ?: 0
            val numeroOrdem = ordemDash.numeroOrdem?.ifBlank { "Sem nº" } ?: "Sem nº"
            val modeloNome = ordemDash.modeloNome?.takeIf { it.isNotBlank() } ?: "Modelo por identificar"

            val montagemOk = ordemDash.montagemOk
            val embalagemOk = ordemDash.embalagemOk
            val controloOk = ordemDash.controloOk
            val unidadeRegistada = ordemDash.unidadeRegistada
            val temVinPendente = ordemDash.vinPendente
            val temControloPendente = montagemOk && embalagemOk && !controloOk && estado != 2
            val estaBloqueada = estado == 3

            if (estado == 2) concluidas++

            val prioridadeLabel = when {
                estaBloqueada -> "Crítica"
                temControloPendente || !unidadeRegistada || temVinPendente -> "Alta"
                else -> "Normal"
            }
            val estadoLabel = when (estado) {
                0 -> "Por arrancar"; 1 -> "Em produção"; 2 -> "Concluída"; 3 -> "Bloqueada"; else -> "Operacional"
            }

            when {
                estaBloqueada ->
                    acoes += DashboardActionUi(acoes.size + 1, "Desbloquear $numeroOrdem", "A ordem está parada e requer decisão de supervisão.", ordemId)
                !unidadeRegistada && estado != 2 ->
                    acoes += DashboardActionUi(acoes.size + 1, "Unidade em falta: $numeroOrdem", "A ordem avançou sem unidade associada. Crítico para rastreabilidade.", ordemId)
                temControloPendente ->
                    acoes += DashboardActionUi(acoes.size + 1, "Validação final pendente: $numeroOrdem", "Produção concluída, falta fechar o controlo de qualidade.", ordemId)
                temVinPendente ->
                    acoes += DashboardActionUi(acoes.size + 1, "VIN por fechar: $numeroOrdem", "Existe pelo menos uma unidade com identificação por concluir.", ordemId)
            }

            cards += DashboardOrderUi(ordemId, numeroOrdem, modeloNome, estadoLabel, prioridadeLabel, estaBloqueada, unidadeRegistada, temVinPendente, temControloPendente)
        }

        val utilizadores = utilizadoresDeferred.await()
        val servicosData = servicosDeferred.await()
        val expedicaoData = expedicaoDeferred.await()
        val alertasData = alertasDeferred.await()

        val equipaAtiva = if (utilizadores.isNotEmpty()) {
            utilizadores.count { u -> DtoHelpers.isUserAtivo(u.ativo, u.estado) }
        } else {
            dashResumo.equipaAtiva
        }
        val equipaIndisponivel = maxOf(0, utilizadores.size - equipaAtiva)

        if (equipaIndisponivel > 0) {
            acoes += DashboardActionUi(acoes.size + 1, "Rever cobertura da equipa", "Existem $equipaIndisponivel colaborador(es) indisponível(eis) hoje.")
        }

        val servicosEmAbertoCount = dashResumo.servicosEmAberto
        val servicosGarantia = servicosData?.servicos?.count { (it.tipo ?: 0) == 3 } ?: 0
        val servicosAvaria = servicosData?.servicos?.count { (it.tipo ?: 0) == 2 } ?: 0
        if (servicosEmAbertoCount > 0) {
            acoes += DashboardActionUi(acoes.size + 1, "Serviços em aberto: $servicosEmAbertoCount", "$servicosGarantia garantia(s), $servicosAvaria avaria(s) por tratar.")
        }

        return DashboardUiState(
            totalOrdens = dashResumo.totalOrdens,
            emProducao = dashResumo.emProducao,
            bloqueadas = dashResumo.bloqueadas,
            concluidas = concluidas,
            semUnidade = dashResumo.semUnidade,
            controloPendente = dashResumo.controloPendente,
            vinPendente = dashResumo.vinPendente,
            alertasAtivos = alertasData?.alertas?.size ?: 0,
            expedicoesPendentes = expedicaoData?.total ?: 0,
            equipaAtiva = equipaAtiva,
            equipaIndisponivel = equipaIndisponivel,
            acoesImediatas = acoes.take(6),
            ordensPrioritarias = cards.sortedWith(
                compareByDescending<DashboardOrderUi> { it.bloqueada }
                    .thenByDescending { it.prioridadeLabel == "Crítica" }
                    .thenByDescending { it.prioridadeLabel == "Alta" }
                    .thenByDescending { it.vinPendente }
            ).take(5),
            equipaHoje = buildEquipaHoje(utilizadores),
            servicosEmAberto = servicosEmAbertoCount,
            servicosGarantia = servicosGarantia,
            servicosAvaria = servicosAvaria
        )
    }

    // ── Fallback N+1 ──
    private suspend fun buildFromNPlus1(): DashboardUiState {
        val ordens = fabricaRepository.getOrdens()

        val utilizadores: List<UtilizadorDto> = runCatching { fabricaRepository.getUtilizadores() }
            .onFailure { Log.e("DashboardVM", "Falha ao carregar utilizadores", it) }
            .getOrDefault(emptyList())

        val modelosMap = runCatching { fabricaRepository.getModelos().associateBy { it.idModelo ?: 0 } }
            .onFailure { Log.e("DashboardVM", "Falha ao carregar modelos", it) }
            .getOrDefault(emptyMap())

        val expedicaoDeferred = viewModelScope.async {
            runCatching { fabricaRepository.getProntosExpedicao() }
                .onFailure { Log.e("DashboardVM", "Falha ao carregar expedição", it) }
                .getOrNull()
        }
        val alertasDeferred = viewModelScope.async {
            runCatching { fabricaRepository.getAlertas() }
                .onFailure { Log.e("DashboardVM", "Falha ao carregar alertas", it) }
                .getOrNull()
        }

        val cards = mutableListOf<DashboardOrderUi>()
        val acoes = mutableListOf<DashboardActionUi>()

        var emProducao = 0; var bloqueadas = 0; var concluidas = 0; var semUnidade = 0
        var controloPendente = 0; var vinPendente = 0

        for (ordem in ordens) {
            val ordemId = ordem.idOrdemProducao ?: continue
            val estado = ordem.estado ?: 0
            val numeroOrdem = ordem.numeroOrdem?.ifBlank { "Sem nº" } ?: "Sem nº"
            val modeloNome = modelosMap[ordem.idModelo ?: -1]?.nomeModelo?.takeIf { !it.isNullOrBlank() } ?: "Modelo por identificar"

            val resumoDeferred = viewModelScope.async {
                runCatching { fabricaRepository.getOrdemResumo(ordemId) }
                    .onFailure { Log.e("DashboardVM", "Falha resumo ordem $ordemId", it) }
                    .getOrNull()
            }
            val motasDeferred = viewModelScope.async {
                runCatching { fabricaRepository.getMotasDaOrdem(ordemId) }
                    .onFailure { Log.e("DashboardVM", "Falha motas ordem $ordemId", it) }
                    .getOrDefault(emptyList())
            }

            val resumo = resumoDeferred.await()
            val motas = motasDeferred.await()

            val unidadeRegistada = motas.isNotEmpty()
            val temVinPendente = motas.any { it.numeroIdentificacao.isNullOrBlank() }
            val montagemOk = resumo?.checklists?.montagem?.ok ?: resumo?.checklists?.montagemOk ?: false
            val embalagemOk = resumo?.checklists?.embalagem?.ok ?: resumo?.checklists?.embalagemOk ?: false
            val controloOk = resumo?.checklists?.controlo?.ok ?: resumo?.checklists?.controloOk ?: false
            val temControloPendente = montagemOk && embalagemOk && !controloOk && estado != 2
            val estaBloqueada = estado == 3

            if (estado == 1) emProducao++
            if (estaBloqueada) bloqueadas++
            if (estado == 2) concluidas++
            if (!unidadeRegistada && estado != 2) semUnidade++
            if (temControloPendente) controloPendente++
            if (temVinPendente && estado != 2) vinPendente++

            val prioridadeLabel = when {
                estaBloqueada -> "Crítica"
                temControloPendente || !unidadeRegistada || temVinPendente -> "Alta"
                else -> "Normal"
            }
            val estadoLabel = when (estado) {
                0 -> "Por arrancar"; 1 -> "Em produção"; 2 -> "Concluída"; 3 -> "Bloqueada"; else -> "Operacional"
            }

            when {
                estaBloqueada ->
                    acoes += DashboardActionUi(acoes.size + 1, "Desbloquear $numeroOrdem", "A ordem está parada e requer decisão de supervisão.", ordemId)
                !unidadeRegistada && estado != 2 ->
                    acoes += DashboardActionUi(acoes.size + 1, "Unidade em falta: $numeroOrdem", "A ordem avançou sem unidade associada. Crítico para rastreabilidade.", ordemId)
                temControloPendente ->
                    acoes += DashboardActionUi(acoes.size + 1, "Validação final pendente: $numeroOrdem", "Produção concluída, falta fechar o controlo de qualidade.", ordemId)
                temVinPendente ->
                    acoes += DashboardActionUi(acoes.size + 1, "VIN por fechar: $numeroOrdem", "Existe pelo menos uma unidade com identificação por concluir.", ordemId)
            }

            cards += DashboardOrderUi(ordemId, numeroOrdem, modeloNome, estadoLabel, prioridadeLabel, estaBloqueada, unidadeRegistada, temVinPendente, temControloPendente)
        }

        val equipaAtiva = utilizadores.count { u -> DtoHelpers.isUserAtivo(u.ativo, u.estado) }
        val equipaIndisponivel = utilizadores.size - equipaAtiva

        if (equipaIndisponivel > 0) {
            acoes += DashboardActionUi(acoes.size + 1, "Rever cobertura da equipa", "Existem $equipaIndisponivel colaborador(es) indisponível(eis) hoje.")
        }

        val servicosData = runCatching { fabricaRepository.getServicosEmAberto() }
            .onFailure { Log.e("DashboardVM", "Falha ao carregar serviços em aberto", it) }
            .getOrNull()
        val servicosEmAberto = servicosData?.total ?: 0
        val servicosGarantia = servicosData?.servicos?.count { (it.tipo ?: 0) == 3 } ?: 0
        val servicosAvaria = servicosData?.servicos?.count { (it.tipo ?: 0) == 2 } ?: 0

        if (servicosEmAberto > 0) {
            acoes += DashboardActionUi(acoes.size + 1, "Serviços em aberto: $servicosEmAberto", "$servicosGarantia garantia(s), $servicosAvaria avaria(s) por tratar.")
        }

        val expedicaoData = expedicaoDeferred.await()
        val alertasData = alertasDeferred.await()

        return DashboardUiState(
            totalOrdens = ordens.size,
            emProducao = emProducao,
            bloqueadas = bloqueadas,
            concluidas = concluidas,
            semUnidade = semUnidade,
            controloPendente = controloPendente,
            vinPendente = vinPendente,
            alertasAtivos = alertasData?.alertas?.size ?: 0,
            expedicoesPendentes = expedicaoData?.total ?: 0,
            equipaAtiva = equipaAtiva,
            equipaIndisponivel = equipaIndisponivel,
            acoesImediatas = acoes.take(6),
            ordensPrioritarias = cards.sortedWith(
                compareByDescending<DashboardOrderUi> { it.bloqueada }
                    .thenByDescending { it.prioridadeLabel == "Crítica" }
                    .thenByDescending { it.prioridadeLabel == "Alta" }
                    .thenByDescending { it.vinPendente }
            ).take(5),
            equipaHoje = buildEquipaHoje(utilizadores),
            servicosEmAberto = servicosEmAberto,
            servicosGarantia = servicosGarantia,
            servicosAvaria = servicosAvaria
        )
    }

    private suspend fun buildEquipaHoje(utilizadores: List<UtilizadorDto>): List<DashboardPessoaUi> =
        utilizadores
            .sortedWith(
                compareByDescending<UtilizadorDto> { u -> DtoHelpers.isUserAtivo(u.ativo, u.estado) }
                    .thenBy { u -> DtoHelpers.text(u.username, "Utilizador") }
            )
            .take(5)
            .map { u ->
                val ativo = DtoHelpers.isUserAtivo(u.ativo, u.estado)
                val totalAssociacoes = runCatching {
                    fabricaRepository.getMotasDoUtilizador(u.idUtilizador ?: 0, true).total ?: 0
                }.getOrDefault(0)
                val disponibilidade = when {
                    !ativo -> "Indisponível"
                    totalAssociacoes >= 2 -> "Sobrecarga"
                    totalAssociacoes == 1 -> "Afeto à operação"
                    else -> "Disponível"
                }
                DashboardPessoaUi(
                    id = u.idUtilizador ?: 0,
                    nome = DtoHelpers.text(u.username, "Utilizador"),
                    funcao = DtoHelpers.text(u.tipo, "Operação"),
                    disponibilidade = disponibilidade,
                    ativo = ativo
                )
            }
}

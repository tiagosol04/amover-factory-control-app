package com.example.aplicacaodecontrolofabrica.data.repository

import com.example.aplicacaodecontrolofabrica.data.dto.*

interface FabricaRepository {

    // ── Dashboard & Alertas ──
    suspend fun getDashboardResumo(): DashboardResumoDto
    suspend fun getAlertas(): AlertasApiResponse

    // ── Ordens (supervisão / controlo) ──
    suspend fun getOrdens(estado: Int? = null): List<OrdemProducaoDto>
    suspend fun getOrdem(id: Int): OrdemProducaoDto
    suspend fun getOrdemResumo(id: Int): OrdemResumoDto
    suspend fun getOrdemFicha(id: Int): OrdemFichaDto
    suspend fun getMotasDaOrdem(ordemId: Int): List<MotaDto>
    suspend fun finalizarOrdem(id: Int): FinalizarOrdemResponse
    suspend fun bloquearOrdem(id: Int, motivo: String): BloquearOrdemResponse
    suspend fun desbloquearOrdem(id: Int, resolucao: String?): DesbloquearOrdemResponse
    suspend fun getOrdemHistorico(id: Int): HistoricoOrdemResponse
    suspend fun getOrdemUtilizadores(id: Int): OrdemUtilizadoresResponse

    // ── Controlo de qualidade ──
    suspend fun getChecklistsDaOrdem(ordemId: Int): ChecklistsStatusDto
    suspend fun updateChecklistControlo(ordemId: Int, checklistId: Int, value: Int)

    // ── Expedição ──
    suspend fun getProntosExpedicao(): ProntosExpedicaoResponse
    suspend fun marcarProntaExpedicao(id: Int): MarcarEmbalagemResponse
    suspend fun marcarEnviada(id: Int): MarcarEnviadaResponse

    // ── Rastreabilidade (leitura) ──
    suspend fun getMotaByVin(vin: String): RastreabilidadeDto

    // ── Unidades / motas (leitura) ──
    suspend fun getMotaPecasSn(motaId: Int): List<MotaPecaSnDto>
    suspend fun getMotaPecasSnResumo(motaId: Int): PecasSnResumoResponse
    suspend fun getMotaPecasFixas(motaId: Int): PecaFixaResponse

    // ── Serviços / Manutenção / Garantias ──
    suspend fun getServicosMeta(): ServicosMetaResponse
    suspend fun getServicos(estado: Int? = null, motaId: Int? = null, modeloId: Int? = null, tipo: Int? = null, vin: String? = null, emAberto: Boolean? = null, q: String? = null): List<ServicoDto>
    suspend fun getServicosEmAberto(): ServicosEmAbertoResponse
    suspend fun getServico(id: Int): ServicoDto
    suspend fun criarServico(body: CreateServicoRequest): ServicoDto
    suspend fun updateServicoEstado(id: Int, estado: Int, dataConclusao: String? = null): ServicoDto
    suspend fun getHistoricoByMota(motaId: Int): HistoricoMotaResponse
    suspend fun getHistoricoByVin(vin: String): HistoricoMotaResponse
    suspend fun getHistoricoByModelo(idModelo: Int): HistoricoModeloResponse
    suspend fun getProblemasFrequentes(idModelo: Int): ProblemasFrequentesResponse
    suspend fun getGarantiasByModelo(idModelo: Int): GarantiasModeloResponse

    // ── Encomendas (leitura) ──
    suspend fun getEncomendas(clienteId: Int? = null, estado: Int? = null): List<EncomendaDto>
    suspend fun getEncomenda(id: Int): EncomendaDto

    // ── Contexto operacional ──
    suspend fun getUtilizadores(): List<UtilizadorDto>
    suspend fun getUtilizadorDetalhe(id: Int): UtilizadorDetailResponseDto
    suspend fun updateDisponibilidadeUtilizador(id: Int, ativo: Boolean)
    suspend fun getMotasDoUtilizador(id: Int, ativasOnly: Boolean = true): UtilizadorMotasResponseDto
    suspend fun getModelos(): List<ModeloDto>
    suspend fun getModelo(id: Int): ModeloDto
    suspend fun getClientes(): List<ClienteDto>
    suspend fun getCliente(id: Int): ClienteDto
}
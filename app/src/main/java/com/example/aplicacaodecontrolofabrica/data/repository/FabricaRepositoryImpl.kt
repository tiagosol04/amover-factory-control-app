package com.example.aplicacaodecontrolofabrica.data.repository

import com.example.aplicacaodecontrolofabrica.core.network.ApiService
import com.example.aplicacaodecontrolofabrica.data.dto.*

class FabricaRepositoryImpl(
    private val api: ApiService
) : FabricaRepository {

    // ── Dashboard & Alertas ──
    override suspend fun getDashboardResumo() = api.getDashboardResumo()
    override suspend fun getAlertas() = api.getAlertas()

    // ── Ordens ──
    override suspend fun getOrdens(estado: Int?) = api.getOrdens(estado)
    override suspend fun getOrdem(id: Int) = api.getOrdem(id)
    override suspend fun getOrdemResumo(id: Int) = api.getResumo(id)
    override suspend fun getOrdemFicha(id: Int) = api.getOrdemFicha(id)
    override suspend fun getValidacaoFinal(id: Int) = api.getValidacaoFinal(id)
    override suspend fun getMotasDaOrdem(ordemId: Int) = api.getMotasDaOrdem(ordemId)
    override suspend fun finalizarOrdem(id: Int) = api.finalizarOrdem(id)
    override suspend fun bloquearOrdem(id: Int, motivo: String) = api.bloquearOrdem(id, BloquearOrdemRequest(motivo))
    override suspend fun desbloquearOrdem(id: Int, resolucao: String?) = api.desbloquearOrdem(id, DesbloquearOrdemRequest(resolucao))
    override suspend fun getOrdemHistorico(id: Int) = api.getOrdemHistorico(id)
    override suspend fun getOrdemUtilizadores(id: Int) = api.getOrdemUtilizadores(id)

    // ── Controlo de qualidade ──
    override suspend fun getChecklistsDaOrdem(ordemId: Int) = api.getChecklistsDaOrdem(ordemId)
    override suspend fun updateChecklistControlo(ordemId: Int, checklistId: Int, value: Int) {
        api.updateChecklistControlo(ordemId, checklistId, UpdateChecklistRequest(value = value))
    }

    // ── Expedição ──
    override suspend fun getProntosExpedicao() = api.getProntosExpedicao()
    override suspend fun marcarProntaExpedicao(id: Int) = api.marcarProntaExpedicao(id)
    override suspend fun marcarEnviada(id: Int) = api.marcarEnviada(id)

    // ── Rastreabilidade ──
    override suspend fun getMotaByVin(vin: String) = api.getMotaByVin(vin)

    // ── Unidades / motas (leitura) ──
    override suspend fun getMotaPecasSn(motaId: Int) = api.getMotaPecasSn(motaId)
    override suspend fun getMotaPecasSnResumo(motaId: Int) = api.getMotaPecasSnResumo(motaId)
    override suspend fun getMotaPecasFixas(motaId: Int) = api.getMotaPecasFixas(motaId)

    // ── Serviços ──
    override suspend fun getServicosMeta() = api.getServicosMeta()
    override suspend fun getServicos(estado: Int?, motaId: Int?, modeloId: Int?, tipo: Int?, vin: String?, emAberto: Boolean?, q: String?) = api.getServicos(estado, motaId, modeloId, tipo, vin, emAberto, q)
    override suspend fun getServicosEmAberto() = api.getServicosEmAberto()
    override suspend fun getServico(id: Int) = api.getServico(id)
    override suspend fun criarServico(body: CreateServicoRequest) = api.criarServico(body)
    override suspend fun updateServicoEstado(id: Int, estado: Int, dataConclusao: String?) = api.updateServicoEstado(id, UpdateServicoEstadoRequest(estado, dataConclusao))
    override suspend fun getHistoricoByMota(motaId: Int) = api.getHistoricoByMota(motaId)
    override suspend fun getHistoricoByVin(vin: String) = api.getHistoricoByVin(vin)
    override suspend fun getHistoricoByModelo(idModelo: Int) = api.getHistoricoByModelo(idModelo)
    override suspend fun getProblemasFrequentes(idModelo: Int) = api.getProblemasFrequentes(idModelo)
    override suspend fun getGarantiasByModelo(idModelo: Int) = api.getGarantiasByModelo(idModelo)

    // ── Encomendas ──
    override suspend fun getEncomendas(clienteId: Int?, estado: Int?) = api.getEncomendas(clienteId, estado)
    override suspend fun getEncomenda(id: Int) = api.getEncomenda(id)

    // ── Contexto ──
    override suspend fun getUtilizadores() = api.getUtilizadores().utilizadores
    override suspend fun getUtilizadorDetalhe(id: Int) = api.getUtilizadorDetalhe(id)
    override suspend fun updateDisponibilidadeUtilizador(id: Int, ativo: Boolean) { api.updateUtilizadorStatus(id, UpdateUserStatusRequest(ativo)) }
    override suspend fun getMotasDoUtilizador(id: Int, ativasOnly: Boolean) = api.getMotasDoUtilizador(id, ativasOnly)
    override suspend fun getModelos() = api.getModelos()
    override suspend fun getModelo(id: Int) = api.getModelo(id)
    override suspend fun getClientes() = api.getClientes()
    override suspend fun getCliente(id: Int) = api.getCliente(id)
}
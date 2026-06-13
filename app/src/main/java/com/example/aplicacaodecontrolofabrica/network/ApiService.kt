package com.example.aplicacaodecontrolofabrica.core.network

import com.example.aplicacaodecontrolofabrica.data.dto.*
import retrofit2.http.*

interface ApiService {

    // ── Auth ──
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/auth/me")
    suspend fun me(): MeResponse

    // ── Dashboard & Alertas ──
    @GET("api/controlo-fabrica/dashboard")
    suspend fun getDashboardResumo(): DashboardResumoDto

    @GET("api/controlo-fabrica/alertas")
    suspend fun getAlertas(): AlertasApiResponse

    // ── Ordens (supervisão / controlo) ──
    @GET("api/controlo-fabrica/ordens")
    suspend fun getOrdens(@Query("estado") estado: Int? = null): List<OrdemProducaoDto>

    @GET("api/controlo-fabrica/ordens/{id}")
    suspend fun getOrdem(@Path("id") id: Int): OrdemProducaoDto

    @GET("api/ordens/{id}/resumo")
    suspend fun getResumo(@Path("id") id: Int): OrdemResumoDto

    @GET("api/controlo-fabrica/ordens/{id}/ficha")
    suspend fun getOrdemFicha(@Path("id") id: Int): OrdemFichaDto

    @GET("api/controlo-fabrica/ordens/{id}/validacao-final")
    suspend fun getValidacaoFinal(@Path("id") id: Int): ValidacaoFinalDto

    @GET("api/ordens/{id}/motas")
    suspend fun getMotasDaOrdem(@Path("id") id: Int): List<MotaDto>

    @POST("api/controlo-fabrica/ordens/{id}/finalizar")
    suspend fun finalizarOrdem(@Path("id") id: Int): FinalizarOrdemResponse

    @POST("api/controlo-fabrica/ordens/{id}/bloquear")
    suspend fun bloquearOrdem(@Path("id") id: Int, @Body body: BloquearOrdemRequest): BloquearOrdemResponse

    @POST("api/controlo-fabrica/ordens/{id}/desbloquear")
    suspend fun desbloquearOrdem(@Path("id") id: Int, @Body body: DesbloquearOrdemRequest): DesbloquearOrdemResponse

    @GET("api/controlo-fabrica/ordens/{id}/historico")
    suspend fun getOrdemHistorico(@Path("id") id: Int): HistoricoOrdemResponse

    @GET("api/ordens/{id}/utilizadores")
    suspend fun getOrdemUtilizadores(@Path("id") id: Int): OrdemUtilizadoresResponse

    // ── Controlo de qualidade ──
    @GET("api/controlo-fabrica/ordens/{ordemId}/controlo-qualidade")
    suspend fun getChecklistsDaOrdem(@Path("ordemId") ordemId: Int): ChecklistsStatusDto

    @PUT("api/controlo-fabrica/ordens/{ordemId}/controlo-qualidade/{checklistId}")
    suspend fun updateChecklistControlo(
        @Path("ordemId") ordemId: Int,
        @Path("checklistId") checklistId: Int,
        @Body body: UpdateChecklistRequest
    )

    // ── Expedição ──
    @GET("api/controlo-fabrica/expedicao")
    suspend fun getProntosExpedicao(): ProntosExpedicaoResponse

    @POST("api/controlo-fabrica/expedicao/{id}/marcar-embalada")
    suspend fun marcarProntaExpedicao(@Path("id") id: Int): MarcarEmbalagemResponse

    @POST("api/controlo-fabrica/expedicao/{id}/marcar-enviada")
    suspend fun marcarEnviada(@Path("id") id: Int): MarcarEnviadaResponse

    // ── Rastreabilidade (leitura) ──
    @GET("api/controlo-fabrica/rastreabilidade/{vin}")
    suspend fun getMotaByVin(@Path("vin") vin: String): RastreabilidadeDto

    // ── Unidades / motas (leitura) ──
    @GET("api/motas/{id}/pecas-sn")
    suspend fun getMotaPecasSn(@Path("id") id: Int): List<MotaPecaSnDto>

    @GET("api/motas/{id}/pecas-sn/resumo")
    suspend fun getMotaPecasSnResumo(@Path("id") id: Int): PecasSnResumoResponse

    @GET("api/motas/{id}/pecas-fixas")
    suspend fun getMotaPecasFixas(@Path("id") id: Int): PecaFixaResponse

    // ── Serviços / Manutenção / Garantias ──
    @GET("api/servicos/meta")
    suspend fun getServicosMeta(): ServicosMetaResponse

    @GET("api/servicos")
    suspend fun getServicos(
        @Query("estado") estado: Int? = null,
        @Query("motaId") motaId: Int? = null,
        @Query("modeloId") modeloId: Int? = null,
        @Query("tipo") tipo: Int? = null,
        @Query("vin") vin: String? = null,
        @Query("emAberto") emAberto: Boolean? = null,
        @Query("q") q: String? = null
    ): List<ServicoDto>

    @GET("api/servicos/em-aberto")
    suspend fun getServicosEmAberto(): ServicosEmAbertoResponse

    @GET("api/servicos/{id}")
    suspend fun getServico(@Path("id") id: Int): ServicoDto

    @POST("api/servicos")
    suspend fun criarServico(@Body body: CreateServicoRequest): ServicoDto

    @PUT("api/servicos/{id}/estado")
    suspend fun updateServicoEstado(@Path("id") id: Int, @Body body: UpdateServicoEstadoRequest): ServicoDto

    @GET("api/servicos/{id}/pecas-alteradas")
    suspend fun getPecasAlteradas(@Path("id") id: Int): List<PecaAlteradaDto>

    @POST("api/servicos/{id}/pecas-alteradas")
    suspend fun addPecaAlterada(@Path("id") id: Int, @Body body: AddPecaAlteradaRequest)

    @DELETE("api/servicos/pecas-alteradas/{idAssoc}")
    suspend fun deletePecaAlterada(@Path("idAssoc") idAssoc: Int)

    @GET("api/servicos/motas/{motaId}/historico")
    suspend fun getHistoricoByMota(@Path("motaId") motaId: Int): HistoricoMotaResponse

    @GET("api/servicos/by-vin/{vin}/historico")
    suspend fun getHistoricoByVin(@Path("vin") vin: String): HistoricoMotaResponse

    @GET("api/servicos/modelos/{idModelo}/historico")
    suspend fun getHistoricoByModelo(@Path("idModelo") idModelo: Int): HistoricoModeloResponse

    @GET("api/servicos/modelos/{idModelo}/problemas-frequentes")
    suspend fun getProblemasFrequentes(@Path("idModelo") idModelo: Int): ProblemasFrequentesResponse

    @GET("api/servicos/modelos/{idModelo}/garantias")
    suspend fun getGarantiasByModelo(@Path("idModelo") idModelo: Int): GarantiasModeloResponse

    // ── Encomendas (leitura) ──
    @GET("api/encomendas")
    suspend fun getEncomendas(
        @Query("clienteId") clienteId: Int? = null,
        @Query("estado") estado: Int? = null
    ): List<EncomendaDto>

    @GET("api/encomendas/{id}")
    suspend fun getEncomenda(@Path("id") id: Int): EncomendaDto

    // ── Utilizadores / Modelos / Clientes ──
    @GET("api/controlo-fabrica/utilizadores")
    suspend fun getUtilizadores(): UtilizadoresListResponse

    @GET("api/utilizadores/{id}")
    suspend fun getUtilizadorDetalhe(@Path("id") id: Int): UtilizadorDetailResponseDto

    @PUT("api/utilizadores/{id}/status")
    suspend fun updateUtilizadorStatus(@Path("id") id: Int, @Body body: UpdateUserStatusRequest)

    @GET("api/utilizadores/{id}/motas")
    suspend fun getMotasDoUtilizador(
        @Path("id") id: Int,
        @Query("ativasOnly") ativasOnly: Boolean = true
    ): UtilizadorMotasResponseDto

    @GET("api/modelos")
    suspend fun getModelos(): List<ModeloDto>

    @GET("api/modelos/{id}")
    suspend fun getModelo(@Path("id") id: Int): ModeloDto

    @GET("api/clientes")
    suspend fun getClientes(): List<ClienteDto>

    @GET("api/clientes/{id}")
    suspend fun getCliente(@Path("id") id: Int): ClienteDto
}
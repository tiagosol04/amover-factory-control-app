package com.example.aplicacaodecontrolofabrica.features.operacao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacaodecontrolofabrica.data.model.ChecklistExecucao
import com.example.aplicacaodecontrolofabrica.data.model.RoleAccessUi
import com.example.aplicacaodecontrolofabrica.data.model.TipoChecklistUi
import com.example.aplicacaodecontrolofabrica.data.model.label
import com.example.aplicacaodecontrolofabrica.di.ViewModelFactory
import com.example.aplicacaodecontrolofabrica.ui.components.ErrorState
import com.example.aplicacaodecontrolofabrica.ui.components.LoadingView
import com.example.aplicacaodecontrolofabrica.ui.components.SectionCard
import com.example.aplicacaodecontrolofabrica.ui.components.StatusChip
import com.example.aplicacaodecontrolofabrica.ui.components.StatusChipType
import com.example.aplicacaodecontrolofabrica.ui.theme.FactoryAlert
import com.example.aplicacaodecontrolofabrica.ui.theme.FactoryInfo
import com.example.aplicacaodecontrolofabrica.ui.theme.FactoryPrimary
import com.example.aplicacaodecontrolofabrica.ui.theme.FactorySecondary
import com.example.aplicacaodecontrolofabrica.ui.theme.FactoryWarning

private val TABS = listOf("Geral", "Estado", "Qualidade", "Validação", "Histórico")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FichaOperacionalScreen(
    ordemId: Int,
    onBack: () -> Unit,
    access: RoleAccessUi? = null,
    viewModel: OrdemDetalheRealViewModel = viewModel(factory = ViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    var tabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(ordemId) { viewModel.load(ordemId) }

    // Limpar sucesso após mostrar
    LaunchedEffect(uiState.actionSuccess) {
        if (!uiState.actionSuccess.isNullOrBlank()) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearActionSuccess()
        }
    }

    when {
        uiState.isLoading -> LoadingView(message = "A carregar ordem...")
        uiState.errorMessage != null && uiState.ordemId == 0 -> ErrorState(
            message = uiState.errorMessage ?: "Erro.",
            onRetry = { viewModel.load(ordemId) },
            modifier = Modifier.padding(16.dp)
        )
        else -> Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header compacto ──
                FichaHeader(uiState)

                // ── Tabs ──
                ScrollableTabRow(selectedTabIndex = tabIndex, edgePadding = 16.dp) {
                    TABS.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }

                // ── Conteúdo por tab ──
                Box(modifier = Modifier.weight(1f)) {
                    when (tabIndex) {
                        0 -> TabGeral(uiState)
                        1 -> TabEstado(uiState)
                        2 -> TabQualidade(uiState, access, viewModel)
                        3 -> TabValidacao(uiState, access, viewModel)
                        4 -> TabHistorico(uiState)
                    }
                }
            }

            // ── Snackbar de sucesso / erro inline ──
            val mensagem = uiState.actionSuccess ?: (if (uiState.errorMessage != null && !uiState.isLoading) uiState.errorMessage else null)
            if (!mensagem.isNullOrBlank()) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = if (uiState.actionSuccess != null) FactorySecondary else FactoryAlert
                ) {
                    Text(mensagem, color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 0 — Geral
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TabGeral(uiState: FichaOperacionalUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Gates de rastreabilidade
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GateCard(Modifier.weight(1f), "Unidade", uiState.unidadeRegistada)
            GateCard(Modifier.weight(1f), "VIN", uiState.unidadeRegistada && !uiState.vinPendente)
            GateCard(Modifier.weight(1f), "Qualidade", uiState.montagemOk && uiState.embalagemOk && uiState.controloOk)
        }

        // Contexto da ordem
        SectionCard(title = "Contexto") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Modelo", uiState.modeloNome)
                InfoRow("Cliente", uiState.clienteNome)
                InfoRow("Destino", uiState.paisDestino)
                InfoRow("Criação", uiState.dataCriacaoIso?.take(10) ?: "—")
                InfoRow("Conclusão", uiState.dataConclusaoIso?.take(10) ?: "—")
            }
        }

        // Estado operacional
        SectionCard(title = "Estado operacional") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = uiState.prontidaoTexto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiState.vinList.isEmpty()) {
                    Text("Sem VINs registados", style = MaterialTheme.typography.bodySmall, color = FactoryWarning)
                } else {
                    uiState.vinList.forEachIndexed { i, vin ->
                        InfoRow("VIN ${i + 1}", vin)
                    }
                }
            }
        }

        // Indicadores rápidos
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniStat(Modifier.weight(1f), "Unidades", uiState.totalMotas.toString())
            MiniStat(Modifier.weight(1f), "Serviços", uiState.totalServicos.toString())
            MiniStat(Modifier.weight(1f), "Risco", uiState.resumoRisco)
        }

        // Checklists resumo
        SectionCard(title = "Checklists") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ChecklistResumoRow("Montagem", uiState.montagemOk, uiState.checklistsMontagem.size, uiState.checklistsMontagem.count { it.concluido })
                ChecklistResumoRow("Embalagem", uiState.embalagemOk, uiState.checklistsEmbalagem.size, uiState.checklistsEmbalagem.count { it.concluido })
                ChecklistResumoRow("Controlo", uiState.controloOk, uiState.checklistsControlo.size, uiState.checklistsControlo.count { it.concluido })
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 1 — Estado da Produção
// ─────────────────────────────────────────────────────────────
@Composable
private fun TabEstado(uiState: FichaOperacionalUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionCard(title = "Estado da Produção") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ProducaoStatusRow("Montagem", uiState.montagemOk)
                ProducaoStatusRow("Embalagem", uiState.embalagemOk)
                ProducaoStatusRow("VIN registado", uiState.unidadeRegistada && !uiState.vinPendente)
                ProducaoStatusRow("Controlo de Qualidade", uiState.controloOk)
            }
        }

        if (uiState.motasFicha.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Sem unidades registadas nesta ordem.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            uiState.motasFicha.forEach { mota ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Unidade #${mota.motaId}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            StatusChip(
                                status = if (mota.vin != null) StatusChipType.CONCLUIDO else StatusChipType.ATENCAO,
                                labelOverride = if (mota.vin != null) "VIN OK" else "Sem VIN"
                            )
                        }
                        InfoRow("VIN", mota.vin ?: "Por registar")
                        InfoRow("Cor", mota.cor ?: "—")
                        InfoRow("Estado", mota.estado)

                        if (mota.pecasFixas.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Peças fixas", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = FactoryInfo)
                            mota.pecasFixas.forEach { pf ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pf.nome, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                        pf.partNumber?.let { Text("PN: $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    }
                                    Text("×${pf.quantidade}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Peças SN (${mota.pecasSn.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = FactoryPrimary)
                        if (mota.pecasSn.isEmpty()) {
                            Text("Sem peças SN registadas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            mota.pecasSn.forEach { peca ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Peça #${peca.idPeca}", style = MaterialTheme.typography.bodySmall)
                                    Text("SN: ${peca.serialNumber ?: "—"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 2 — Qualidade (controlo final editável)
// ─────────────────────────────────────────────────────────────
@Composable
private fun TabQualidade(
    uiState: FichaOperacionalUiState,
    access: RoleAccessUi?,
    viewModel: OrdemDetalheRealViewModel
) {
    val podeMarcar = access?.canMarcarChecklist == true && !uiState.concluida

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Estado de Produção (só leitura)") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ChecklistResumoRow("Montagem", uiState.montagemOk, uiState.checklistsMontagem.size, uiState.checklistsMontagem.count { it.concluido })
                ChecklistResumoRow("Embalagem", uiState.embalagemOk, uiState.checklistsEmbalagem.size, uiState.checklistsEmbalagem.count { it.concluido })
            }
        }

        if (uiState.checklistsControlo.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Sem checklist de controlo de qualidade disponível.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            ChecklistGrupo(
                titulo = "Controlo Final",
                itens = uiState.checklistsControlo,
                tipo = "controlo",
                podeMarcar = podeMarcar,
                isLoading = uiState.actionLoading,
                onToggle = { checklistId, concluido -> viewModel.toggleChecklist(checklistId, concluido) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 3 — Validação Final
// ─────────────────────────────────────────────────────────────
@Composable
private fun TabValidacao(
    uiState: FichaOperacionalUiState,
    access: RoleAccessUi?,
    viewModel: OrdemDetalheRealViewModel
) {
    var confirmDialog by remember { mutableStateOf<String?>(null) }
    var motivoBloqueio by remember { mutableStateOf("") }
    var resolucaoDesbloqueio by remember { mutableStateOf("") }

    // Diálogo de confirmação genérico
    confirmDialog?.let { tipo ->
        val titulo = when (tipo) {
            "finalizar" -> "Finalizar ordem"
            "bloquear" -> "Bloquear ordem"
            "desbloquear" -> "Desbloquear ordem"
            "embalada" -> "Marcar como pronta para expedição"
            "enviada" -> "Marcar como enviada"
            else -> "Confirmar ação"
        }
        AlertDialog(
            onDismissRequest = { confirmDialog = null; motivoBloqueio = ""; resolucaoDesbloqueio = "" },
            title = { Text(titulo) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Confirmar esta ação para a ordem ${uiState.numeroOrdem}?")
                    if (tipo == "bloquear") {
                        OutlinedTextField(
                            value = motivoBloqueio,
                            onValueChange = { motivoBloqueio = it },
                            label = { Text("Motivo do bloqueio *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            minLines = 2
                        )
                    }
                    if (tipo == "desbloquear") {
                        OutlinedTextField(
                            value = resolucaoDesbloqueio,
                            onValueChange = { resolucaoDesbloqueio = it },
                            label = { Text("Resolução / nota (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            minLines = 2
                        )
                    }
                    if (tipo == "enviada") {
                        Text(
                            "A expedição ainda não tem tabela própria na BD. A mota poderá transitar para Ativa. Nenhum dado de transportadora é registado.",
                            style = MaterialTheme.typography.labelSmall,
                            color = FactoryWarning
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (tipo) {
                            "finalizar" -> viewModel.finalizarOrdem()
                            "bloquear" -> viewModel.bloquearOrdem(motivoBloqueio)
                            "desbloquear" -> viewModel.desbloquearOrdem(resolucaoDesbloqueio)
                            "embalada" -> viewModel.marcarProntaExpedicao()
                            "enviada" -> viewModel.marcarEnviada()
                        }
                        confirmDialog = null
                        motivoBloqueio = ""
                        resolucaoDesbloqueio = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipo == "bloquear") FactoryAlert else FactoryPrimary
                    ),
                    enabled = tipo != "bloquear" || motivoBloqueio.isNotBlank()
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDialog = null; motivoBloqueio = ""; resolucaoDesbloqueio = "" }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Aviso se API de validação não respondeu
        if (!uiState.validacaoFinalErro.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = FactoryWarning.copy(alpha = 0.1f))
            ) {
                Text(
                    uiState.validacaoFinalErro!!,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = FactoryWarning
                )
            }
        }

        // Checklist de prontidão — usa dados da API quando disponíveis
        val temValidacaoApi = uiState.validacaoFinalErro == null
        SectionCard(title = "Prontidão para Finalizar${if (temValidacaoApi) "" else " (calculado localmente)"}") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (temValidacaoApi) {
                    ProducaoStatusRow("VIN registado", uiState.validacaoFinalVinOk)
                    ProducaoStatusRow("Peças SN verificadas", uiState.validacaoFinalPecasSnOk)
                    ProducaoStatusRow("Montagem OK", uiState.validacaoFinalMontagemOk)
                    ProducaoStatusRow("Embalagem OK", uiState.validacaoFinalEmbalagemOk)
                    ProducaoStatusRow("Controlo de Qualidade OK", uiState.validacaoFinalControloOk)
                } else {
                    ProducaoStatusRow("Unidade registada", uiState.unidadeRegistada)
                    ProducaoStatusRow("VIN sem pendências", uiState.unidadeRegistada && !uiState.vinPendente)
                    ProducaoStatusRow("Montagem OK", uiState.montagemOk)
                    ProducaoStatusRow("Embalagem OK", uiState.embalagemOk)
                    ProducaoStatusRow("Controlo de Qualidade OK", uiState.controloOk)
                }
            }
        }

        // Pendências vindas da API
        if (uiState.validacaoFinalPendencias.isNotEmpty()) {
            SectionCard(title = "Pendências (${uiState.validacaoFinalPendencias.size})") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    uiState.validacaoFinalPendencias.forEach { pendencia ->
                        Text(
                            "• $pendencia",
                            style = MaterialTheme.typography.bodySmall,
                            color = FactoryWarning
                        )
                    }
                }
            }
        }

        // Estado atual
        SectionCard(title = "Estado") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow("Estado", uiState.estadoLabel)
                InfoRow("Próxima ação", uiState.proximaAcao)
                InfoRow("Risco", uiState.resumoRisco)
            }
        }

        val isWorking = uiState.isUpdating || uiState.actionLoading

        // ── Finalizar ordem ──
        if (!uiState.concluida && !uiState.bloqueada && access?.canFinalizarOrdem == true) {
            // Gate: API deve confirmar podeFinalizar; se indisponível, bloquear por segurança
            val podeFinalizarAgora = uiState.validacaoFinalErro == null && uiState.validacaoFinalPodeFinalizar
            Button(
                onClick = { confirmDialog = "finalizar" },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isWorking && podeFinalizarAgora,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FactorySecondary)
            ) {
                Icon(Icons.Filled.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("  Finalizar ordem")
            }
            if (!podeFinalizarAgora) {
                Text(
                    if (uiState.validacaoFinalErro != null) "Validação da API necessária para finalizar." else "Todas as condições devem estar verificadas para finalizar.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Bloquear ordem ──
        if (!uiState.concluida && !uiState.bloqueada && access?.canBloquearOrdem == true) {
            OutlinedButton(
                onClick = { confirmDialog = "bloquear" },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isWorking
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = FactoryAlert)
                Text("  Bloquear ordem", color = FactoryAlert)
            }
        }

        // ── Desbloquear ordem ──
        if (uiState.bloqueada && access?.canBloquearOrdem == true) {
            Button(
                onClick = { confirmDialog = "desbloquear" },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isWorking,
                colors = ButtonDefaults.buttonColors(containerColor = FactoryWarning)
            ) {
                Icon(Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("  Desbloquear ordem")
            }
        }

        // ── Expedição ──
        if (!uiState.concluida && access?.canFinalizarOrdem == true) {
            SectionCard(title = "Expedição") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "A expedição ainda não tem tabela própria na BD. Estas ações são proxies operacionais.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!uiState.avisoExpedicao.isNullOrBlank()) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = FactoryWarning.copy(alpha = 0.1f))
                        ) {
                            Text(
                                uiState.avisoExpedicao!!,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = FactoryWarning
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { confirmDialog = "embalada" },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isWorking
                    ) { Text("Marcar como embalada") }
                    OutlinedButton(
                        onClick = { confirmDialog = "enviada" },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isWorking
                    ) { Text("Marcar como enviada") }
                }
            }
        }

        // Loading indicator
        if (isWorking) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            }
        }

        // Sem permissões
        if (access?.canFinalizarOrdem == false && access.canBloquearOrdem == false) {
            SectionCard(title = "Permissões") {
                Text(
                    "O teu perfil (${access.perfil.name}) não tem permissão para executar ações nesta ordem. Podes consultar os dados mas não alterar o estado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 5 — Histórico
// ─────────────────────────────────────────────────────────────
@Composable
private fun TabHistorico(uiState: FichaOperacionalUiState) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (!uiState.avisoHistorico.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = FactoryInfo.copy(alpha = 0.1f))
            ) {
                Text(
                    uiState.avisoHistorico!!,
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = FactoryInfo
                )
            }
        }
        if (uiState.historicoItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Sem histórico disponível para esta ordem.\n(Endpoint calculado pelo backend.)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.historicoItems, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(item.tipo, style = MaterialTheme.typography.labelSmall, color = FactoryInfo)
                                Text(item.dataOcorrencia?.take(10) ?: "—", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(item.descricao, style = MaterialTheme.typography.bodySmall)
                            Text("Por: ${item.utilizadorNome}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// HEADER da Ficha Operacional
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FichaHeader(uiState: FichaOperacionalUiState) {
    val accentColor = when {
        uiState.bloqueada -> FactoryAlert
        uiState.concluida -> FactorySecondary
        uiState.vinPendente || !uiState.unidadeRegistada -> FactoryWarning
        else -> FactoryPrimary
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(accentColor, accentColor.copy(alpha = 0.75f))))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(uiState.numeroOrdem, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(uiState.modeloNome, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StatusChip(
                    status = when {
                        uiState.bloqueada -> StatusChipType.BLOQUEADO
                        uiState.concluida -> StatusChipType.CONCLUIDO
                        else -> StatusChipType.NORMAL
                    },
                    labelOverride = uiState.estadoLabel
                )
                if (uiState.clienteNome.isNotBlank() && uiState.clienteNome != "Cliente por identificar") {
                    StatusChip(status = StatusChipType.NORMAL, labelOverride = uiState.clienteNome)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPONENTES AUXILIARES
// ─────────────────────────────────────────────────────────────

@Composable
private fun ChecklistGrupo(
    titulo: String,
    itens: List<ChecklistExecucao>,
    tipo: String,
    podeMarcar: Boolean,
    isLoading: Boolean,
    onToggle: (checklistId: Int, concluido: Boolean) -> Unit
) {
    val concluidos = itens.count { it.concluido }
    SectionCard(title = "$titulo ($concluidos/${itens.size})") {
        if (itens.isEmpty()) {
            Text("Sem itens de checklist.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                itens.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = item.concluido,
                            onCheckedChange = { checked ->
                                if (podeMarcar && !isLoading) {
                                    onToggle(item.idChecklist, checked)
                                }
                            },
                            enabled = podeMarcar && !isLoading
                        )
                        Text(
                            text = item.estado.label(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.concluido) FactorySecondary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProducaoStatusRow(label: String, ok: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        StatusChip(status = if (ok) StatusChipType.CONCLUIDO else StatusChipType.ATENCAO, labelOverride = if (ok) "OK" else "Pendente")
    }
}

@Composable
private fun ChecklistResumoRow(titulo: String, ok: Boolean, total: Int, concluidos: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$concluidos/$total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            StatusChip(status = if (ok) StatusChipType.CONCLUIDO else StatusChipType.ATENCAO, labelOverride = if (ok) "OK" else "Pendente")
        }
    }
}

@Composable
private fun GateCard(modifier: Modifier, label: String, ok: Boolean) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ok) FactorySecondary.copy(alpha = 0.08f) else FactoryWarning.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (ok) FactorySecondary else FactoryWarning))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            Text(if (ok) "OK" else "Pendente", style = MaterialTheme.typography.labelSmall, color = if (ok) FactorySecondary else FactoryWarning)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MiniStat(modifier: Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

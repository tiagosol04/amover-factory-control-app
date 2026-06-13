package com.example.aplicacaodecontrolofabrica.app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aplicacaodecontrolofabrica.data.model.RoleAccessUi
import com.example.aplicacaodecontrolofabrica.data.model.resolveRoleAccess
import com.example.aplicacaodecontrolofabrica.di.ViewModelFactory
import com.example.aplicacaodecontrolofabrica.features.alertas.AlertaDetalheScreen
import com.example.aplicacaodecontrolofabrica.features.alertas.AlertasScreen
import com.example.aplicacaodecontrolofabrica.features.cockpit.DashboardScreen
import com.example.aplicacaodecontrolofabrica.features.encomendas.EncomendaDetalheScreen
import com.example.aplicacaodecontrolofabrica.features.encomendas.EncomendasScreen
import com.example.aplicacaodecontrolofabrica.features.equipa.EquipaScreen
import com.example.aplicacaodecontrolofabrica.features.expedicao.ExpedicaoScreen
import com.example.aplicacaodecontrolofabrica.features.historico.HistoricoScreen
import com.example.aplicacaodecontrolofabrica.features.login.AuthViewModel
import com.example.aplicacaodecontrolofabrica.features.login.LoginScreen
import com.example.aplicacaodecontrolofabrica.features.operacao.FichaOperacionalScreen
import com.example.aplicacaodecontrolofabrica.features.operacao.OperacaoScreen
import com.example.aplicacaodecontrolofabrica.features.perfil.PerfilScreen
import com.example.aplicacaodecontrolofabrica.features.servicos.ServicoDetalheScreen
import com.example.aplicacaodecontrolofabrica.features.servicos.ServicosScreen

sealed class Screen(val route: String, val title: String) {
    data object Login : Screen("login", "Login")
    data object Cockpit : Screen("cockpit", "Dashboard")
    data object Operacao : Screen("operacao", "Ordens")
    data object Qualidade : Screen("qualidade", "Qualidade")
    data object Alertas : Screen("alertas", "Alertas")
    data object Historico : Screen("historico", "Rastreabilidade")
    data object Expedicao : Screen("expedicao", "Expedição")
    data object Servicos : Screen("servicos", "Serviços")
    data object Equipa : Screen("equipa", "Equipa")
    data object Encomendas : Screen("encomendas", "Encomendas")
    data object Perfil : Screen("perfil", "Perfil")
    data object AlertaDetalhe : Screen("alerta/{alertaId}", "Alerta") {
        fun create(id: Int) = "alerta/$id"
    }
    data object FichaOperacional : Screen("ordem/{ordemId}", "Ordem") {
        fun create(id: Int) = "ordem/$id"
    }
    data object ServicoDetalhe : Screen("servico/{servicoId}", "Serviço") {
        fun create(id: Int) = "servico/$id"
    }
    data object EncomendaDetalhe : Screen("encomenda/{encomendaId}", "Encomenda") {
        fun create(id: Int) = "encomenda/$id"
    }
}

private data class NavMainItem(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun resolveInitialRoute(access: RoleAccessUi): String = when {
    access.canOpenCockpit -> Screen.Cockpit.route
    access.canOpenOperacao || access.canOpenProducao -> Screen.Operacao.route
    access.canOpenAlertas -> Screen.Alertas.route
    access.canOpenHistorico -> Screen.Historico.route
    access.canOpenEquipa -> Screen.Equipa.route
    else -> Screen.Perfil.route
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authVm: AuthViewModel = viewModel(factory = ViewModelFactory())
    val token by authVm.token.collectAsState(initial = "")
    val roles by authVm.roles.collectAsState(initial = emptyList())
    val access = resolveRoleAccess(roles)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(token, currentRoute, access) {
        if (token.isBlank()) {
            if (currentRoute != Screen.Login.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
            return@LaunchedEffect
        }
        if (currentRoute == null || currentRoute == Screen.Login.route) {
            navController.navigate(resolveInitialRoute(access)) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(onSuccess = {
                navController.navigate(resolveInitialRoute(access)) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }

        // ── Ecrãs principais ──

        composable(Screen.Cockpit.route) {
            AppShell(Screen.Cockpit.title, navController, authVm, access) { p ->
                Box(Modifier.padding(p)) {
                    DashboardScreen(
                        onOpenOrdens = { navController.navigate(Screen.Operacao.route) },
                        onOpenEquipa = { navController.navigate(Screen.Equipa.route) },
                        onOpenAlertas = { navController.navigate(Screen.Alertas.route) },
                        onOpenExpedicao = { navController.navigate(Screen.Expedicao.route) },
                        onOpenQualidade = { navController.navigate(Screen.Qualidade.route) },
                        onOpenOrdemDetalhe = { id -> navController.navigate(Screen.FichaOperacional.create(id)) }
                    )
                }
            }
        }

        composable(Screen.Operacao.route) {
            AppShell(Screen.Operacao.title, navController, authVm, access) { p ->
                OperacaoScreen(
                    contentPadding = p,
                    onOpenOrdem = { id -> navController.navigate(Screen.FichaOperacional.create(id)) }
                )
            }
        }

        // Qualidade: lista de ordens com foco em controlo de qualidade
        composable(Screen.Qualidade.route) {
            AppShell(Screen.Qualidade.title, navController, authVm, access) { p ->
                OperacaoScreen(
                    contentPadding = p,
                    onOpenOrdem = { id -> navController.navigate(Screen.FichaOperacional.create(id)) }
                )
            }
        }

        composable(Screen.Alertas.route) {
            AppShell(Screen.Alertas.title, navController, authVm, access) { p ->
                Box(Modifier.padding(p)) {
                    AlertasScreen(onOpenAlerta = { id -> navController.navigate(Screen.AlertaDetalhe.create(id)) })
                }
            }
        }

        composable(Screen.Historico.route) {
            AppShell(Screen.Historico.title, navController, authVm, access) { p ->
                Box(Modifier.padding(p)) {
                    HistoricoScreen(onOpenOrdem = { id -> navController.navigate(Screen.FichaOperacional.create(id)) })
                }
            }
        }

        composable(Screen.Expedicao.route) {
            AppShell(Screen.Expedicao.title, navController, authVm, access) { p ->
                ExpedicaoScreen(
                    contentPadding = p,
                    onOpenOrdem = { id -> navController.navigate(Screen.FichaOperacional.create(id)) }
                )
            }
        }

        composable(Screen.Servicos.route) {
            AppShell(Screen.Servicos.title, navController, authVm, access) { p ->
                ServicosScreen(
                    contentPadding = p,
                    onOpenServico = { id -> navController.navigate(Screen.ServicoDetalhe.create(id)) }
                )
            }
        }

        composable(Screen.Equipa.route) {
            AppShell(Screen.Equipa.title, navController, authVm, access) { p ->
                EquipaScreen(contentPadding = p)
            }
        }

        composable(Screen.Encomendas.route) {
            AppShell(Screen.Encomendas.title, navController, authVm, access) { p ->
                EncomendasScreen(
                    contentPadding = p,
                    onOpenEncomenda = { id -> navController.navigate(Screen.EncomendaDetalhe.create(id)) }
                )
            }
        }

        composable(Screen.Perfil.route) {
            AppShell(Screen.Perfil.title, navController, authVm, access) { p ->
                Box(Modifier.padding(p)) { PerfilScreen(onLogout = { authVm.logout() }) }
            }
        }

        // ── Ecrãs de detalhe ──

        composable(
            route = Screen.AlertaDetalhe.route,
            arguments = listOf(navArgument("alertaId") { type = NavType.IntType })
        ) { entry ->
            val alertaId = entry.arguments?.getInt("alertaId") ?: 0
            DetailShell(title = "Alerta", onBack = { navController.popBackStack() }) { p ->
                Box(Modifier.padding(p)) {
                    AlertaDetalheScreen(alertaId = alertaId, onBack = { navController.popBackStack() })
                }
            }
        }

        composable(
            route = Screen.FichaOperacional.route,
            arguments = listOf(navArgument("ordemId") { type = NavType.IntType })
        ) { entry ->
            val ordemId = entry.arguments?.getInt("ordemId") ?: 0
            DetailShell(title = "Ordem", onBack = { navController.popBackStack() }) { p ->
                Box(Modifier.padding(p)) {
                    FichaOperacionalScreen(ordemId = ordemId, onBack = { navController.popBackStack() }, access = access)
                }
            }
        }

        composable(
            route = Screen.ServicoDetalhe.route,
            arguments = listOf(navArgument("servicoId") { type = NavType.IntType })
        ) { entry ->
            val servicoId = entry.arguments?.getInt("servicoId") ?: 0
            DetailShell(title = "Serviço", onBack = { navController.popBackStack() }) { p ->
                Box(Modifier.padding(p)) {
                    ServicoDetalheScreen(servicoId = servicoId, onBack = { navController.popBackStack() })
                }
            }
        }

        composable(
            route = Screen.EncomendaDetalhe.route,
            arguments = listOf(navArgument("encomendaId") { type = NavType.IntType })
        ) { entry ->
            val encomendaId = entry.arguments?.getInt("encomendaId") ?: 0
            DetailShell(title = "Encomenda", onBack = { navController.popBackStack() }) { p ->
                Box(Modifier.padding(p)) {
                    EncomendaDetalheScreen(
                        encomendaId = encomendaId,
                        access = access,
                        onBack = { navController.popBackStack() },
                        onOpenOrdem = { id -> navController.navigate(Screen.FichaOperacional.create(id)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppShell(
    title: String,
    navController: NavController,
    authVm: AuthViewModel,
    access: RoleAccessUi,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Menu de fábrica: Dashboard · Ordens · Alertas · Serviços · Perfil
    val mainItems = buildList {
        if (access.canOpenCockpit) add(NavMainItem(Screen.Cockpit, "Dashboard", Icons.Filled.Home))
        if (access.canOpenOperacao || access.canOpenProducao) add(NavMainItem(Screen.Operacao, "Ordens", Icons.Filled.Flag))
        if (access.canOpenAlertas) add(NavMainItem(Screen.Alertas, "Alertas", Icons.Filled.WarningAmber))
        if (access.canConsultarGarantias || access.canRegistarOcorrencias) add(NavMainItem(Screen.Servicos, "Serviços", Icons.Filled.Build))
        if (access.canOpenPerfil) add(NavMainItem(Screen.Perfil, "Perfil", Icons.Filled.Person))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    // Qualidade — controlo de qualidade (sempre visível)
                    IconButton(onClick = { navController.navigate(Screen.Qualidade.route) { launchSingleTop = true } }) {
                        Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = "Qualidade")
                    }
                    // Expedição — atalho rápido para chão de fábrica
                    IconButton(onClick = { navController.navigate(Screen.Expedicao.route) { launchSingleTop = true } }) {
                        Icon(Icons.Filled.LocalShipping, contentDescription = "Expedição")
                    }
                    // Rastreabilidade
                    if (access.canOpenHistorico) {
                        IconButton(onClick = { navController.navigate(Screen.Historico.route) { launchSingleTop = true } }) {
                            Icon(Icons.Filled.QueryStats, contentDescription = "Rastreabilidade")
                        }
                    }
                    // Equipa
                    if (access.canOpenEquipa) {
                        IconButton(onClick = { navController.navigate(Screen.Equipa.route) { launchSingleTop = true } }) {
                            Icon(Icons.Filled.Groups, contentDescription = "Equipa")
                        }
                    }
                    IconButton(onClick = { authVm.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                mainItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        },
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailShell(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        content = content
    )
}

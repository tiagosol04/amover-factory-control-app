```
     ╔════════════════════════════════════════════════════════════════════╗
     ║                                                                    ║
     ║      █████╗       ███╗   ███╗  ██████╗ ██╗   ██╗ ███████╗██████╗   ║
     ║     ██╔══██╗      ████╗ ████║ ██╔═══██╗██║   ██║ ██╔════╝██╔══██╗  ║
     ║     ███████║█████╗██╔████╔██║ ██║   ██║██║   ██║ █████╗  ██████╔╝  ║
     ║     ██╔══██║╚════╝██║╚██╔╝██║ ██║   ██║╚██╗ ██╔╝ ██╔══╝  ██╔══██╗  ║
     ║     ██║  ██║      ██║ ╚═╝ ██║ ╚██████╔╝ ╚████╔╝  ███████╗██║  ██║  ║
     ║     ╚═╝  ╚═╝      ╚═╝     ╚═╝  ╚═════╝   ╚═══╝   ╚══════╝╚═╝  ╚═╝  ║
     ║                                                                    ║
     ║              F A C T O R Y   M A N A G E M E N T                  ║
     ║                                                                    ║
     ╚════════════════════════════════════════════════════════════════════╝
```

### Mobile Factory Management App — AJP Motorcycles

*360° factory visibility in the manager’s pocket.*

[![Android](https://img.shields.io/badge/Android-Kotlin-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](#)
[![MVVM](https://img.shields.io/badge/MVVM-Architecture-FF9800?style=for-the-badge)](#)
[![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens)](#)

---

</div>

## 📱 What it is

**A-MoVeR Factory Management** is a native Android application for **managers, supervisors and quality control staff** at AJP Motorcycles. It works as the mobile companion to the web platform, bringing production, service, warranty and team information directly to a smartphone, enabling real-time decision-making without being tied to a desktop computer.

```
  ┌──────────────────────────────────────────────────────────────────┐
  │                                                                  │
  │   📊 DASHBOARD    →   Instant overview of the entire factory     │
  │   🏭 PRODUCTION   →   Orders, statuses, checklists, parts        │
  │   🔧 SERVICES     →   Maintenance, faults, warranties            │
  │   📦 ORDERS       →   Customer order pipeline                    │
  │   📈 TRACKING     →   Full motorcycle/VIN history                │
  │   👥 TEAM         →   Availability and workload                  │
  │   👤 PROFILE      →   Session, roles, preferences                │
  │                                                                  │
  └──────────────────────────────────────────────────────────────────┘
```

---

## ⚡ Features

### 🎯 Operational Dashboard

The dashboard does not show generic numbers — it shows **what needs attention right now**:

| Indicator | Meaning |
|-----------|---------|
| **Blocked orders** | Production stopped — immediate decision required |
| **No unit registered** | Motorcycles in production without traceability |
| **Pending VIN** | Frame not closed — shipping risk |
| **Pending control** | Almost ready, missing final validation |
| **Open services** | Maintenance and warranty cases to resolve |
| **Unavailable team members** | Insufficient shift coverage |

The dashboard calculates **immediate actions** and sorts them by priority. Factory areas such as Assembly, Packaging, Control and Exceptions show real-time workload.

### 🏭 Production

- Order list with filters by status, model and priority
- Complete **Operational Sheet** per order:
  - Traceability gates: Unit ✓ → VIN ✓ → Quality ✓
  - Checklist status: assembly, packaging and control
  - Context: customer, model and destination
  - **Start** and **Finalize** production orders through the API with full validation
- Risk summary and suggested next action

### 🔧 Services, Maintenance & Warranties

Complete after-sales module:

```
                    ┌──────────────────┐
                    │   SERVICE LIST   │
                    │  KPIs + Filters  │
                    │   + VIN Search   │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐
                    │ SERVICE DETAILS  │
                    │ ┌──────────────┐ │
                    │ │ Vehicle info │ │
                    │ │ Type/Status  │ │
                    │ │ Changed parts│ │
                    │ │ Notes        │ │
                    │ └──────────────┘ │
                    │ ┌──────────────┐ │
                    │ │ Frequent     │ │
                    │ │ problems by  │ │
                    │ │ model        │ │
                    │ └──────────────┘ │
                    │ ┌──────────────┐ │
                    │ │   Actions:   │ │
                    │ │ Start        │ │
                    │ │ Complete     │ │
                    │ └──────────────┘ │
                    └──────────────────┘
```

**8 service types**: Maintenance, Fault, Warranty, Inspection, Diagnosis, Preparation/Delivery, Technical Campaign, Other.

**Model analysis**: grouped frequent problems + total warranties — helping identify patterns and support engineering decisions.

### 📦 Orders

Commercial pipeline overview:

- KPIs: Pending / In Production / Completed
- Customer, model, quantity and delivery date
- Accessible through a top-bar shortcut for managers/admin users

### 📈 Tracking / History

- Search by production order, VIN or destination
- Filters: With VIN, Completed, With services
- Technical summary per motorcycle: model, VIN, country, services and checklist status

### 🔐 Role System

The app adapts to the user profile:

| Role | Dashboard | Production | Services | Orders | Team | Tracking |
|------|:---------:|:----------:|:--------:|:------:|:----:|:--------:|
| **Administration** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Supervisor** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Quality** | ✅ | ✅ | ✅ | — | ✅ | ✅ |
| **After-sales** | — | ✅ | ✅ | — | ✅ | ✅ |
| **Operator** | — | ✅ | ✅ | — | ✅ | — |

The bottom navigation bar only displays the modules available to each role. Top-bar shortcuts for orders, incidents and team management are only shown to users with permission to access them.

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      UI LAYER                                │
│   Jetpack Compose · Material 3 · Role-Based Navigation      │
│                                                              │
│   ┌───────────┐ ┌────────────┐ ┌──────────┐ ┌──────────┐   │
│   │ Dashboard │ │ Production │ │ Services │ │  Orders  │   │
│   └───────────┘ └────────────┘ └──────────┘ └──────────┘   │
│   ┌───────────┐ ┌──────────┐ ┌──────────┐                  │
│   │ Tracking  │ │  Team    │ │ Profile  │                  │
│   └───────────┘ └──────────┘ └──────────┘                  │
├─────────────────────────────────────────────────────────────┤
│                   VIEWMODEL LAYER                            │
│   DashboardRealVM · OrdensRealVM · OrdemDetalheRealVM        │
│   ServicosVM · ServicoDetalheVM · EncomendasVM               │
│   AlertasVM · HistoricoVM · EquipaVM · PerfilRealVM          │
├─────────────────────────────────────────────────────────────┤
│                   REPOSITORY LAYER                           │
│   FabricaRepository interface → FabricaRepositoryImpl        │
│   AuthRepository interface → AuthRepositoryImpl              │
├─────────────────────────────────────────────────────────────┤
│                    NETWORK LAYER                             │
│   Retrofit 2 · OkHttp · JWT Interceptor · DataStore          │
│   ApiService with 65+ mapped endpoints                       │
├─────────────────────────────────────────────────────────────┤
│                     BACKEND                                  │
│   A-MoVeR API · ASP.NET Core · SQL Server · Identity + JWT  │
└─────────────────────────────────────────────────────────────┘
```

### Principles

- **MVVM** with full separation between UI, logic and data
- **StateFlow** for reactive state management — the UI automatically recomposes
- **Repository Pattern** — the UI never communicates directly with the API
- **Role-Based Access** — navigation and features adapt to the JWT user profile
- **Offline-Resilient Session** — the token is persisted in DataStore and survives app restarts

---

## 📁 Project Structure

```
app/src/main/java/com/example/aplicacaodecontrolofabrica/
│
├── auth/
│   ├── AuthDataStore.kt              ← JWT persistence
│   ├── AuthHeaderInterceptor.kt      ← Injects token into requests
│   ├── AuthFailureInterceptor.kt     ← Handles 401/403 responses
│   └── UserSession.kt                ← Session data
│
├── data/
│   ├── dto/
│   │   ├── AuthDtos.kt               ← Login/Me
│   │   ├── OrdemDtos.kt              ← Orders + summary
│   │   ├── ServicoDtos.kt            ← 15+ service DTOs ★
│   │   ├── EncomendasAlertasDto.kt   ← Orders
│   │   ├── ChecklistDtos.kt          ← Checklists
│   │   ├── MotaPecasDtos.kt          ← Motorcycles + serial-numbered parts
│   │   ├── UtilizadorDtos.kt         ← Users + associations
│   │   ├── ModelosDtos.kt            ← Motorcycle models
│   │   ├── ClientesDtos.kt           ← Customers
│   │   └── ExtraDtos.kt              ← Generic requests
│   ├── model/                        ← UI domain models
│   │   ├── Servico.kt                ← 8 types, 3 states, coverage
│   │   ├── RoleAccessUi.kt           ← 6 operational profiles ★
│   │   ├── Alerta.kt                 ← Incidents with severity
│   │   └── ...
│   ├── mapper/
│   │   ├── DtoHelpers.kt             ← Safe DTO → UI conversions
│   │   └── DtoMappers.kt             ← Complex mappings
│   └── repository/
│       ├── FabricaRepository.kt      ← Interface with 40+ methods
│       ├── FabricaRepositoryImpl.kt  ← Implementation
│       ├── AuthRepository.kt         ← Auth interface
│       ├── AuthRepositoryImpl.kt     ← Auth implementation
│       └── ServiceLocator.kt         ← Manual dependency injection
│
├── features/
│   ├── cockpit/
│   │   ├── Dashboard.kt              ← 515 lines of UI ★
│   │   └── DashboardRealViewModel.kt ← Zones, KPIs and actions
│   ├── operacao/
│   │   ├── OperacaoScreen.kt         ← Order list
│   │   ├── FichaOperacionalScreen.kt ← Details + start/finalize ★
│   │   ├── OrdensRealViewModel.kt    ← List with filters
│   │   └── OrdemDetalheRealViewModel.kt ← Start/Finalize/VIN ★
│   ├── servicos/                     ★ NEW
│   │   ├── ServicosScreen.kt         ← List + KPIs + filters
│   │   ├── ServicosViewModel.kt      ← Search + filters
│   │   ├── ServicoDetalheScreen.kt   ← Details + model problems
│   │   └── ServicoDetalheViewModel.kt ← Loads service + analysis
│   ├── encomendas/                   ★ NEW
│   │   ├── EncomendasScreen.kt       ← Order pipeline
│   │   └── EncomendasViewModel.kt    ← Resolves customers/models
│   ├── alertas/
│   ├── historico/
│   ├── equipa/
│   ├── perfil/
│   └── login/
│
├── network/
│   ├── ApiService.kt                 ← 65+ endpoints ★
│   ├── ApiModule.kt                  ← Retrofit configuration
│   ├── ApiConfig.kt                  ← Base URL
│   └── UiErrors.kt                   ← Error handling
│
├── ui/
│   ├── components/                   ← 8 reusable components
│   └── theme/                        ← Custom Material 3 theme
│
├── di/
│   └── ViewModelFactory.kt           ← 11 registered ViewModels
│
├── AppNavigation.kt                  ← 10 routes + role-based navigation ★
└── MainActivity.kt
```

---

## 🔌 API Coverage

The app consumes **65+ endpoints** from the A-MoVeR API:

| Module | Endpoints | Description |
|--------|:---------:|-------------|
| **Auth** | 2 | JWT login + user profile |
| **Orders** | 9 | CRUD + start + finalize + summary + motorcycles |
| **Motorcycles** | 10 | CRUD + VIN + serial-numbered parts + summary + status |
| **Checklists** | 4 | Per order, individual toggle by checklist type |
| **Services** | 14 | CRUD + statuses + changed parts + history by motorcycle/VIN/model + frequent problems + warranties |
| **Customer Orders** | 4 | CRUD + filters |
| **Users** | 5 | CRUD + assigned motorcycles + status |
| **Models** | 2 | List + detail |
| **Customers** | 2 | List + detail |
| **Parts** | 1 | Catalog |

---

## 🚀 Setup

### Requirements

- Android Studio Hedgehog+ or newer
- Kotlin 1.9+ / Compose BOM 2024.09
- A-MoVeR API running locally on port `5137`

### Configuration

1. **Clone the repository**
2. **Check the API URL** in `build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5137/\"")
```

3. **Build & Run**
4. **Log in** using the same credentials as the web platform, backed by the same Identity system

---

## 🧩 Ecosystem Relationship

```
  ┌─────────────────────────┐
  │    WEB APP Desktop      │  ← Full management: orders,
  │    ASP.NET Core MVC     │     models, parts, documents,
  │    + Razor Views        │     purchases, received material
  └───────────┬─────────────┘
              │
  ┌───────────▼─────────────┐
  │      A-MoVeR API        │  ← Central data layer
  │      ASP.NET Core       │     JWT Auth · SQL Server
  │      Web API            │     65+ REST endpoints
  └───────────┬─────────────┘
              │
    ┌─────────┴──────────┐
    │                    │
    ▼                    ▼
  ┌────────────┐   ┌─────────────────┐
  │ MANAGEMENT │   │ ASSEMBLY LINE   │
  │ APP        │   │ APP             │
  │            │   │                 │
  │ Managers   │   │ Operators       │
  │ Supervision│   │ Shop floor      │
  │ Quality    │   │ Tablet/Scanner  │
  └────────────┘   └─────────────────┘
```

The **Management App** and the **Assembly Line App** are complementary:

- The management app provides the **macro view** — dashboard, metrics and decisions
- The assembly line app provides the **micro control** — part by part, checklist by checklist
- Both read and write to the same data through the API
- A change on the assembly line appears on the manager’s dashboard in real time

---

## 🔮 Roadmap

- [ ] Push notifications for critical alerts such as blocked orders and warranties
- [ ] Production charts: motorcycles per week, average time per order
- [ ] Offline mode with automatic sync
- [ ] PDF report export
- [ ] PHC integration for billing and duplicate-data reduction
- [ ] QR Code scanning for quick access to an order or motorcycle
- [ ] Android widget for home-screen KPIs

---

<div align="center">

```
  The factory in your pocket.
  
  AJP Motorcycles × A-MoVeR Project
  Penafiel, Portugal · 2025
```

</div>

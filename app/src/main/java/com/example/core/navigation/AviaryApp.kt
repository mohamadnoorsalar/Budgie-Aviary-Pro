package com.example.core.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.localization.AppLanguage
import com.example.core.localization.AviaryLocalizationProvider
import com.example.core.ui.components.AviaryBottomBar
import com.example.core.ui.components.AviaryDrawer
import com.example.core.ui.components.AviaryTopBar
import com.example.data.database.AppDatabase
import com.example.data.repository.AviaryRepositoryImpl
import com.example.feature.aiassistant.AiAssistantScreen
import com.example.feature.birds.BirdsScreen
import com.example.feature.birds.BirdsViewModel
import com.example.feature.cages.CagesScreen
import com.example.feature.competitions.CompetitionsScreen
import com.example.feature.dashboard.DashboardScreen
import com.example.feature.dashboard.DashboardViewModel
import com.example.feature.finance.FinanceScreen
import com.example.feature.genetics.GeneticsScreen
import com.example.feature.health.HealthScreen
import com.example.feature.help.ContextualHelpDialog
import com.example.feature.help.HelpScreen
import com.example.feature.inventory.InventoryScreen
import com.example.feature.nutrition.NutritionScreen
import com.example.feature.onboarding.OnboardingManager
import com.example.feature.onboarding.OnboardingScreen
import com.example.feature.pairs.PairsScreen
import com.example.feature.pairs.PairsViewModel
import com.example.feature.pedigree.PedigreeScreen
import com.example.feature.reminders.RemindersScreen
import com.example.feature.reminders.RemindersViewModel
import com.example.feature.reports.ReportsScreen
import com.example.feature.reproduction.ReproductionScreen
import com.example.feature.settings.SettingsScreen
import com.example.ui.theme.BudgieAviaryTheme
import kotlinx.coroutines.launch

@Composable
fun AviaryApp() {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDark) }
    var currentLanguage by remember { mutableStateOf(AppLanguage.PERSIAN) }

    var showOnboarding by remember {
        mutableStateOf(!OnboardingManager.isOnboardingCompleted(context))
    }

    val database = remember { AppDatabase.getInstance(context) }
    val repository = remember { AviaryRepositoryImpl(database) }
    val securityManager = remember { com.example.core.security.SecurityManager.getInstance(context, database.auditLogDao()) }
    val securityState by securityManager.state.collectAsStateWithLifecycle()
    val networkMonitor = remember { com.example.core.sync.NetworkMonitor.getInstance(context) }
    val networkState by networkMonitor.networkState.collectAsStateWithLifecycle()
    val backupManager = remember { com.example.core.backup.BackupManager.getInstance(context, database) }
    val syncManager = remember { com.example.core.sync.SyncManager.getInstance(context, database, networkMonitor) }
    val deviceTransferManager = remember { com.example.core.sync.DeviceTransferManager(context, database, backupManager) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                securityManager.onAppBackgrounded()
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_START) {
                securityManager.onAppForegrounded()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AviaryLocalizationProvider(currentLanguage = currentLanguage) {
        BudgieAviaryTheme(darkTheme = isDarkMode) {
            if (showOnboarding) {
                OnboardingScreen(
                    currentLanguage = currentLanguage,
                    onFinishOnboarding = { showOnboarding = false }
                )
            } else if (securityState.isAppLocked) {
                com.example.core.security.ui.LockScreen(
                    securityManager = securityManager,
                    securityState = securityState,
                    currentLanguage = currentLanguage,
                    onUnlocked = { }
                )
            } else {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val currentDestination = AviaryDestination.fromRoute(currentRoute)

                var showQrScannerDialog by remember { mutableStateOf(false) }
                var contextualHelpTopicId by remember { mutableStateOf<String?>(null) }

                // ViewModels
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(repository)
                )
                val birdsViewModel: BirdsViewModel = viewModel(
                    factory = BirdsViewModel.Factory(repository)
                )
                val pairsViewModel: PairsViewModel = viewModel(
                    factory = PairsViewModel.Factory(repository)
                )
                val cagesViewModel: com.example.feature.cages.CagesViewModel = viewModel(
                    factory = com.example.feature.cages.CagesViewModel.Factory(repository)
                )
                val remindersViewModel: RemindersViewModel = viewModel(
                    factory = RemindersViewModel.Factory(repository)
                )
                val reproductionViewModel: com.example.feature.reproduction.ReproductionViewModel = viewModel(
                    factory = com.example.feature.reproduction.ReproductionViewModel.Factory(repository)
                )
                val competitionsViewModel: com.example.feature.competitions.CompetitionsViewModel = viewModel(
                    factory = com.example.feature.competitions.CompetitionsViewModel.Factory(repository)
                )
                val aiAssistantViewModel: com.example.feature.aiassistant.AiAssistantViewModel = viewModel(
                    factory = com.example.feature.aiassistant.AiAssistantViewModel.Factory(repository)
                )
                val reportsViewModel: com.example.feature.reports.ReportsViewModel = viewModel(
                    factory = com.example.feature.reports.ReportsViewModel.Factory(repository)
                )

                val pendingReminders by remindersViewModel.uiState.collectAsStateWithLifecycle()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AviaryDrawer(
                            currentDestination = currentDestination,
                            onNavigate = { destination ->
                                coroutineScope.launch { drawerState.close() }
                                if (destination.route != currentDestination.route) {
                                    navController.navigate(destination.route) {
                                        popUpTo(AviaryDestination.DASHBOARD.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            currentLanguage = currentLanguage,
                            pendingRemindersCount = pendingReminders.todayCount + pendingReminders.overdueCount,
                            currentRoleTitle = if (currentLanguage == AppLanguage.PERSIAN) securityState.currentRole.titleFa else securityState.currentRole.titleEn,
                            currentUserName = securityState.currentUserName
                        )
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            AviaryTopBar(
                                title = stringResource(currentDestination.titleRes),
                                onMenuClick = {
                                    coroutineScope.launch { drawerState.open() }
                                },
                                currentLanguage = currentLanguage,
                                onToggleLanguage = {
                                    currentLanguage = if (currentLanguage == AppLanguage.PERSIAN) {
                                        AppLanguage.ENGLISH
                                    } else {
                                        AppLanguage.PERSIAN
                                    }
                                },
                                onNotificationsClick = {
                                    navController.navigate(AviaryDestination.REMINDERS.route) {
                                        launchSingleTop = true
                                    }
                                },
                                onQrScanClick = {
                                    showQrScannerDialog = true
                                },
                                onHelpClick = {
                                    contextualHelpTopicId = when (currentDestination) {
                                        AviaryDestination.BIRDS -> "birds"
                                        AviaryDestination.PAIRS -> "pairs"
                                        AviaryDestination.CAGES -> "cages"
                                        AviaryDestination.REPRODUCTION -> "reproduction"
                                        AviaryDestination.GENETICS -> "genetics"
                                        AviaryDestination.PEDIGREE -> "pedigree"
                                        AviaryDestination.HEALTH -> "health"
                                        AviaryDestination.NUTRITION -> "nutrition"
                                        AviaryDestination.INVENTORY -> "inventory"
                                        AviaryDestination.FINANCE -> "finance"
                                        AviaryDestination.COMPETITIONS -> "competitions"
                                        AviaryDestination.AI_ASSISTANT -> "ai_assistant"
                                        AviaryDestination.REPORTS -> "reports"
                                        AviaryDestination.REMINDERS -> "reminders"
                                        AviaryDestination.SECURITY -> "security"
                                        AviaryDestination.BACKUP_RESTORE -> "backup"
                                        AviaryDestination.SETTINGS -> "settings"
                                        else -> "getting_started"
                                    }
                                },
                                pendingRemindersCount = pendingReminders.todayCount + pendingReminders.overdueCount,
                                currentRoleTitle = if (currentLanguage == AppLanguage.PERSIAN) securityState.currentRole.titleFa else securityState.currentRole.titleEn,
                                onLockClick = if (securityState.isLockConfigured) {
                                    { securityManager.lockApp() }
                                } else null,
                                isOnline = networkState.isOnline
                            )
                        },
                        bottomBar = {
                            AviaryBottomBar(
                                currentDestination = currentDestination,
                                onNavigate = { destination ->
                                    if (destination.route != currentDestination.route) {
                                        navController.navigate(destination.route) {
                                            popUpTo(AviaryDestination.DASHBOARD.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                onOpenDrawer = {
                                    coroutineScope.launch { drawerState.open() }
                                },
                                currentLanguage = currentLanguage
                            )
                        }
                    ) { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = AviaryDestination.DASHBOARD.route,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            composable(AviaryDestination.DASHBOARD.route) {
                                DashboardScreen(
                                    viewModel = dashboardViewModel,
                                    onNavigate = { dest ->
                                        navController.navigate(dest.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AviaryDestination.BIRDS.route) {
                                BirdsScreen(
                                    viewModel = birdsViewModel,
                                    onNavigateToPedigree = { ring ->
                                        navController.navigate(AviaryDestination.PEDIGREE.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToGenetics = {
                                        navController.navigate(AviaryDestination.GENETICS.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AviaryDestination.PAIRS.route) {
                                PairsScreen(
                                    viewModel = pairsViewModel,
                                    onNavigateToBird = { ring ->
                                        birdsViewModel.selectBird(ring)
                                        navController.navigate(AviaryDestination.BIRDS.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToCage = { cageCode ->
                                        cagesViewModel.selectCage(cageCode)
                                        navController.navigate(AviaryDestination.CAGES.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AviaryDestination.CAGES.route) {
                                com.example.feature.cages.CagesScreen(
                                    viewModel = cagesViewModel,
                                    onNavigateToBird = { ring ->
                                        birdsViewModel.selectBird(ring)
                                        navController.navigate(AviaryDestination.BIRDS.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToPair = { pairId ->
                                        pairsViewModel.selectPair(pairId)
                                        navController.navigate(AviaryDestination.PAIRS.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AviaryDestination.REPRODUCTION.route) {
                                ReproductionScreen(
                                    viewModel = reproductionViewModel,
                                    onNavigateToBird = { ring ->
                                        birdsViewModel.selectBird(ring)
                                        navController.navigate(AviaryDestination.BIRDS.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToCage = { cageCode ->
                                        cagesViewModel.selectCage(cageCode)
                                        navController.navigate(AviaryDestination.CAGES.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AviaryDestination.GENETICS.route) {
                                GeneticsScreen(
                                    onNavigateToBird = { ring ->
                                        birdsViewModel.selectBird(ring)
                                        navController.navigate(AviaryDestination.BIRDS.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToPedigree = { ring ->
                                        navController.navigate(AviaryDestination.PEDIGREE.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AviaryDestination.PEDIGREE.route) {
                                PedigreeScreen(
                                    onNavigateToBird = { ring ->
                                        birdsViewModel.selectBird(ring)
                                        navController.navigate(AviaryDestination.BIRDS.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AviaryDestination.HEALTH.route) {
                                HealthScreen()
                            }
                            composable(AviaryDestination.NUTRITION.route) {
                                NutritionScreen()
                            }
                            composable(AviaryDestination.INVENTORY.route) {
                                InventoryScreen()
                            }
                            composable(AviaryDestination.FINANCE.route) {
                                FinanceScreen()
                            }
                            composable(AviaryDestination.COMPETITIONS.route) {
                                CompetitionsScreen(viewModel = competitionsViewModel)
                            }
                            composable(AviaryDestination.AI_ASSISTANT.route) {
                                AiAssistantScreen(viewModel = aiAssistantViewModel)
                            }
                            composable(AviaryDestination.REPORTS.route) {
                                ReportsScreen(viewModel = reportsViewModel)
                            }
                            composable(AviaryDestination.REMINDERS.route) {
                                RemindersScreen(viewModel = remindersViewModel)
                            }
                            composable(AviaryDestination.SETTINGS.route) {
                                SettingsScreen(
                                    currentLanguage = currentLanguage,
                                    onLanguageChange = { currentLanguage = it },
                                    isDarkMode = isDarkMode,
                                    onDarkModeToggle = { isDarkMode = it },
                                    onNavigateToSecurity = {
                                        navController.navigate(AviaryDestination.SECURITY.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToAuditLog = {
                                        navController.navigate(AviaryDestination.AUDIT_LOG.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onNavigateToBackupRestore = {
                                        navController.navigate(AviaryDestination.BACKUP_RESTORE.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AviaryDestination.BACKUP_RESTORE.route) {
                                com.example.feature.backup.BackupRestoreScreen(
                                    backupManager = backupManager,
                                    syncManager = syncManager,
                                    deviceTransferManager = deviceTransferManager,
                                    networkMonitor = networkMonitor,
                                    currentLanguage = currentLanguage,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToConflicts = {
                                        navController.navigate(AviaryDestination.SYNC_CONFLICTS.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AviaryDestination.SYNC_CONFLICTS.route) {
                                com.example.feature.sync.SyncConflictsScreen(
                                    syncManager = syncManager,
                                    currentLanguage = currentLanguage,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(AviaryDestination.SECURITY.route) {
                                com.example.core.security.ui.SecuritySettingsScreen(
                                    securityManager = securityManager,
                                    currentLanguage = currentLanguage,
                                    onBack = { navController.popBackStack() },
                                    onOpenAuditLog = {
                                        navController.navigate(AviaryDestination.AUDIT_LOG.route) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                            composable(AviaryDestination.AUDIT_LOG.route) {
                                com.example.core.security.ui.AuditLogScreen(
                                    auditLogDao = database.auditLogDao(),
                                    currentLanguage = currentLanguage,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(AviaryDestination.HELP.route) {
                                HelpScreen(
                                    onLaunchOnboarding = {
                                        showOnboarding = true
                                    }
                                )
                            }
                        }
                    }
                }

                // Contextual Help Modal Dialog
                contextualHelpTopicId?.let { topicId ->
                    ContextualHelpDialog(
                        topicId = topicId,
                        currentLanguage = currentLanguage,
                        onDismiss = { contextualHelpTopicId = null },
                        onOpenFullHelpCenter = {
                            navController.navigate(AviaryDestination.HELP.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // QR Code Scanner Dialog (Global Scanner)
                if (showQrScannerDialog) {
                    com.example.feature.qr.QrScannerDialog(
                        repository = repository,
                        onDismiss = { showQrScannerDialog = false },
                        onOpenBird = { ringNumber ->
                            birdsViewModel.selectBird(ringNumber)
                            navController.navigate(AviaryDestination.BIRDS.route) {
                                launchSingleTop = true
                            }
                        },
                        onOpenCage = { cageCode ->
                            cagesViewModel.selectCage(cageCode)
                            navController.navigate(AviaryDestination.CAGES.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}

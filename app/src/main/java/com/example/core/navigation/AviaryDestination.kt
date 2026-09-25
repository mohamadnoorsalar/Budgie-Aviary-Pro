package com.example.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

enum class AviaryModuleGroup(val titleRes: Int) {
    CORE(R.string.group_core),
    BREEDING(R.string.group_breeding),
    CARE(R.string.group_care),
    BUSINESS(R.string.group_business),
    TOOLS(R.string.group_tools)
}

enum class AviaryDestination(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
    val group: AviaryModuleGroup,
    val isPrimaryBottomNav: Boolean = false
) {
    DASHBOARD(
        route = "dashboard",
        titleRes = R.string.nav_dashboard,
        icon = Icons.Filled.Home,
        group = AviaryModuleGroup.CORE,
        isPrimaryBottomNav = true
    ),
    BIRDS(
        route = "birds",
        titleRes = R.string.nav_birds,
        icon = Icons.Filled.Pets,
        group = AviaryModuleGroup.CORE,
        isPrimaryBottomNav = true
    ),
    PAIRS(
        route = "pairs",
        titleRes = R.string.nav_pairs,
        icon = Icons.Filled.Favorite,
        group = AviaryModuleGroup.CORE,
        isPrimaryBottomNav = true
    ),
    CAGES(
        route = "cages",
        titleRes = R.string.nav_cages,
        icon = Icons.Filled.GridView,
        group = AviaryModuleGroup.CORE
    ),
    REPRODUCTION(
        route = "reproduction",
        titleRes = R.string.nav_reproduction,
        icon = Icons.Filled.Egg,
        group = AviaryModuleGroup.BREEDING,
        isPrimaryBottomNav = true
    ),
    GENETICS(
        route = "genetics",
        titleRes = R.string.nav_genetics,
        icon = Icons.Filled.Biotech,
        group = AviaryModuleGroup.BREEDING
    ),
    PEDIGREE(
        route = "pedigree",
        titleRes = R.string.nav_pedigree,
        icon = Icons.Filled.AccountTree,
        group = AviaryModuleGroup.BREEDING
    ),
    HEALTH(
        route = "health",
        titleRes = R.string.nav_health,
        icon = Icons.Filled.MedicalServices,
        group = AviaryModuleGroup.CARE
    ),
    NUTRITION(
        route = "nutrition",
        titleRes = R.string.nav_nutrition,
        icon = Icons.Filled.Restaurant,
        group = AviaryModuleGroup.CARE
    ),
    INVENTORY(
        route = "inventory",
        titleRes = R.string.nav_inventory,
        icon = Icons.Filled.Inventory2,
        group = AviaryModuleGroup.CARE
    ),
    FINANCE(
        route = "finance",
        titleRes = R.string.nav_finance,
        icon = Icons.Filled.AccountBalance,
        group = AviaryModuleGroup.BUSINESS
    ),
    COMPETITIONS(
        route = "competitions",
        titleRes = R.string.nav_competitions,
        icon = Icons.Filled.EmojiEvents,
        group = AviaryModuleGroup.BUSINESS
    ),
    AI_ASSISTANT(
        route = "ai_assistant",
        titleRes = R.string.nav_ai_assistant,
        icon = Icons.Filled.AutoAwesome,
        group = AviaryModuleGroup.TOOLS
    ),
    REPORTS(
        route = "reports",
        titleRes = R.string.nav_reports,
        icon = Icons.Filled.BarChart,
        group = AviaryModuleGroup.BUSINESS
    ),
    REMINDERS(
        route = "reminders",
        titleRes = R.string.nav_reminders,
        icon = Icons.Filled.Notifications,
        group = AviaryModuleGroup.TOOLS
    ),
    SETTINGS(
        route = "settings",
        titleRes = R.string.nav_settings,
        icon = Icons.Filled.Settings,
        group = AviaryModuleGroup.TOOLS
    ),
    SECURITY(
        route = "security",
        titleRes = R.string.nav_security,
        icon = Icons.Filled.Lock,
        group = AviaryModuleGroup.TOOLS
    ),
    AUDIT_LOG(
        route = "audit_log",
        titleRes = R.string.nav_audit_log,
        icon = Icons.Filled.History,
        group = AviaryModuleGroup.TOOLS
    ),
    BACKUP_RESTORE(
        route = "backup_restore",
        titleRes = R.string.nav_backup_restore,
        icon = Icons.Filled.Sync,
        group = AviaryModuleGroup.TOOLS
    ),
    SYNC_CONFLICTS(
        route = "sync_conflicts",
        titleRes = R.string.nav_sync_conflicts,
        icon = Icons.Filled.Warning,
        group = AviaryModuleGroup.TOOLS
    ),
    HELP(
        route = "help",
        titleRes = R.string.nav_help,
        icon = Icons.Filled.HelpOutline,
        group = AviaryModuleGroup.TOOLS
    );

    companion object {
        fun fromRoute(route: String?): AviaryDestination {
            return entries.find { it.route == route } ?: DASHBOARD
        }
    }
}

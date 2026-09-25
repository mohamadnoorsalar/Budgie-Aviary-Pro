package com.example.core.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.navigation.AviaryDestination

@Composable
fun AviaryBottomBar(
    currentDestination: AviaryDestination,
    onNavigate: (AviaryDestination) -> Unit,
    onOpenDrawer: () -> Unit,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val primaryDestinations = AviaryDestination.entries.filter { it.isPrimaryBottomNav }

    NavigationBar(
        modifier = modifier
            .testTag("aviary_bottom_bar")
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        primaryDestinations.forEach { destination ->
            val isSelected = currentDestination == destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.titleRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.titleRes),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag("bottom_nav_${destination.route}")
            )
        }

        // "More" / Drawer opener
        NavigationBarItem(
            selected = !primaryDestinations.contains(currentDestination),
            onClick = onOpenDrawer,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "All Modules"
                )
            },
            label = {
                Text(
                    text = if (currentLanguage == AppLanguage.PERSIAN) "سایر بخش‌ها" else "More",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("bottom_nav_more")
        )
    }
}

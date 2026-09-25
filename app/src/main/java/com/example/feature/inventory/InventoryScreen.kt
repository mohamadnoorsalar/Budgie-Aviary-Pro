package com.example.feature.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.inventory.InventoryAnalyticsHelper
import com.example.core.inventory.StockStatus
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.ui.components.EmptyStateView
import com.example.core.ui.components.StatMetricCard
import com.example.data.database.AppDatabase
import com.example.data.database.entity.InventoryCategory
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.InventoryTransactionEntity
import com.example.data.database.entity.StockTransactionType
import com.example.data.repository.AviaryRepositoryImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InventoryScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = run {
        val context = LocalContext.current
        val db = AppDatabase.getInstance(context)
        val repo = AviaryRepositoryImpl(db)
        viewModel(factory = InventoryViewModelFactory(repo))
    }
) {
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN

    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val rawInventory by viewModel.rawInventory.collectAsState()
    val filteredInventory by viewModel.filteredInventory.collectAsState()
    val lowStockItems by viewModel.lowStockItems.collectAsState()
    val consumptionHistory by viewModel.consumptionHistory.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val stockSummary by viewModel.stockSummary.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var itemForStockIn by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var itemForStockOut by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var itemForConsumption by remember { mutableStateOf<InventoryItemEntity?>(null) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("inventory_screen"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // KPI Metrics Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = if (isFa) "تعداد کل اقلام" else "Total Items",
                        value = "${stockSummary.totalItemsCount}",
                        icon = Icons.Filled.Inventory,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_total_items"
                    )
                    StatMetricCard(
                        title = if (isFa) "هشدار کمبود موجودی" else "Low Stock Alerts",
                        value = "${stockSummary.lowStockCount + stockSummary.outOfStockCount}",
                        icon = Icons.Filled.Warning,
                        iconTint = if (stockSummary.lowStockCount + stockSummary.outOfStockCount > 0)
                            MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        testTag = "stat_low_stock"
                    )
                    StatMetricCard(
                        title = if (isFa) "ارزش انبار ($)" else "Total Stock Value",
                        value = "$${"%.1f".format(stockSummary.totalInventoryValue)}",
                        icon = Icons.Filled.AttachMoney,
                        iconTint = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1.2f),
                        testTag = "stat_stock_value"
                    )
                }

                // Low Stock Alert Banner
                if (lowStockItems.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectTab(1) }
                            .testTag("low_stock_banner"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isFa)
                                        "${lowStockItems.size} قلم کالا به حداقل موجودی رسیده‌اند!"
                                    else
                                        "${lowStockItems.size} items reached minimum stock threshold!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = if (isFa) "جهت سفارش مجدد دان و دارو کلیک کنید." else "Tap to review items requiring restock.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Action Bar: Add Item & Search
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            itemToEdit = null
                            showAddItemDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_add_stock_item")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isFa) "افزودن کالا به انبار" else "Add Stock Item")
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text(if (isFa) "جستجوی کالا یا SKU..." else "Search SKU or name...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp)
                            .testTag("input_search_inventory")
                    )
                }

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf(
                        "ALL" to (if (isFa) "همه (${rawInventory.size})" else "All (${rawInventory.size})"),
                        InventoryCategory.FOOD to (if (isFa) "دان و خوراک" else "Food"),
                        InventoryCategory.SUPPLEMENTS to (if (isFa) "مکمل‌ها" else "Supplements"),
                        InventoryCategory.MEDICINE to (if (isFa) "دارو و بهداشت" else "Medicine"),
                        InventoryCategory.EQUIPMENT to (if (isFa) "تجهیزات" else "Equipment"),
                        InventoryCategory.CONSUMABLES to (if (isFa) "ملزومات" else "Consumables")
                    )
                    items(categories) { (catKey, catLabel) ->
                        FilterChip(
                            selected = selectedCategory == catKey,
                            onClick = { viewModel.selectCategory(catKey) },
                            label = { Text(catLabel) },
                            modifier = Modifier.testTag("chip_cat_$catKey")
                        )
                    }
                }

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        text = { Text(if (isFa) "موجودی انبار (${filteredInventory.size})" else "Stock List (${filteredInventory.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        text = { Text(if (isFa) "هشدار کسری (${lowStockItems.size})" else "Low Stock (${lowStockItems.size})") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        text = { Text(if (isFa) "تاریخچه مصرف (${consumptionHistory.size})" else "Consumption (${consumptionHistory.size})") }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        text = { Text(if (isFa) "گردش انبار (${allTransactions.size})" else "Transactions (${allTransactions.size})") }
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Stock Items List
                if (filteredInventory.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.Inventory2,
                            title = if (isFa) "کالایی با این مشخصات یافت نشد" else "No Inventory Items Found",
                            description = if (isFa)
                                "دان، ویتامین‌ها، ملزومات لانه و داروها را در انبار ثبت کنید."
                            else
                                "Add seeds, vitamins, nest bedding, and medications to track stock levels.",
                            actionLabel = if (isFa) "افزودن اولین کالا" else "Add Stock Item",
                            onActionClick = {
                                itemToEdit = null
                                showAddItemDialog = true
                            },
                            testTag = "empty_inventory_view"
                        )
                    }
                } else {
                    items(filteredInventory, key = { it.id }) { item ->
                        InventoryItemCard(
                            item = item,
                            isPersian = isFa,
                            onStockIn = { itemForStockIn = item },
                            onStockOut = { itemForStockOut = item },
                            onConsume = { itemForConsumption = item },
                            onEdit = {
                                itemToEdit = item
                                showAddItemDialog = true
                            },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }
            }
            1 -> {
                // Low Stock Alerts Only
                if (lowStockItems.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.Inventory2,
                            title = if (isFa) "وضعیت موجودی تمام اقلام مطلوب است" else "All Items Well Stocked",
                            description = if (isFa) "هیچ کالایی به آستانه بحرانی حداقل موجودی نرسیده است." else "No items currently below minimum stock threshold.",
                            actionLabel = if (isFa) "مشاهده کل انبار" else "View All Stock",
                            onActionClick = { viewModel.selectTab(0) },
                            testTag = "empty_low_stock_view"
                        )
                    }
                } else {
                    items(lowStockItems, key = { it.id }) { item ->
                        InventoryItemCard(
                            item = item,
                            isPersian = isFa,
                            onStockIn = { itemForStockIn = item },
                            onStockOut = { itemForStockOut = item },
                            onConsume = { itemForConsumption = item },
                            onEdit = {
                                itemToEdit = item
                                showAddItemDialog = true
                            },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }
            }
            2 -> {
                // Consumption History
                if (consumptionHistory.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.Restaurant,
                            title = if (isFa) "هیچ سابقه مصرفی ثبت نشده است" else "No Consumption History Recorded",
                            description = if (isFa) "هنگام دان‌دهی یا دارودرمانی، مصرف اقلام را ثبت کنید تا موجودی خودکار کسر شود." else "Record feeding or medicine usage to log consumption and deduct stock.",
                            actionLabel = if (isFa) "ثبت کالا در انبار" else "Add Item First",
                            onActionClick = { viewModel.selectTab(0) },
                            testTag = "empty_consumption_view"
                        )
                    }
                } else {
                    items(consumptionHistory, key = { it.id }) { tx ->
                        val matchedItem = rawInventory.find { it.id == tx.itemId }
                        StockTransactionCard(
                            transaction = tx,
                            itemName = matchedItem?.name ?: "Unknown Item",
                            itemUnit = matchedItem?.unit ?: "units",
                            dateFormat = dateFormat,
                            isPersian = isFa
                        )
                    }
                }
            }
            3 -> {
                // All Stock Transactions (Stock In / Stock Out / Adjustments)
                if (allTransactions.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Filled.History,
                            title = if (isFa) "تراکنش انباری ثبت نشده است" else "No Stock Transactions",
                            description = if (isFa) "با ورود و خروج کالا از انبار، سوابق گردش کالا در اینجا نمایش داده می‌شود." else "Stock additions and deductions will appear here in chronological order.",
                            actionLabel = if (isFa) "ورود کالا به انبار" else "Stock In",
                            onActionClick = {
                                if (rawInventory.isNotEmpty()) {
                                    itemForStockIn = rawInventory.first()
                                } else {
                                    showAddItemDialog = true
                                }
                            },
                            testTag = "empty_transactions_view"
                        )
                    }
                } else {
                    items(allTransactions, key = { it.id }) { tx ->
                        val matchedItem = rawInventory.find { it.id == tx.itemId }
                        StockTransactionCard(
                            transaction = tx,
                            itemName = matchedItem?.name ?: "Item #${tx.itemId.take(5)}",
                            itemUnit = matchedItem?.unit ?: "units",
                            dateFormat = dateFormat,
                            isPersian = isFa
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddItemDialog) {
        AddEditInventoryItemDialog(
            itemToEdit = itemToEdit,
            isPersian = isFa,
            onDismiss = { showAddItemDialog = false },
            onSave = { savedItem ->
                viewModel.saveItem(savedItem)
                showAddItemDialog = false
            }
        )
    }

    itemForStockIn?.let { item ->
        StockInDialog(
            item = item,
            isPersian = isFa,
            onDismiss = { itemForStockIn = null },
            onConfirm = { qty, price, notes, createExpense ->
                viewModel.recordStockIn(item.id, qty, price, notes, createExpense)
                itemForStockIn = null
            }
        )
    }

    itemForStockOut?.let { item ->
        StockOutDialog(
            item = item,
            isPersian = isFa,
            onDismiss = { itemForStockOut = null },
            onConfirm = { qty, reason, notes ->
                viewModel.recordStockOut(item.id, qty, reason, notes)
                itemForStockOut = null
            }
        )
    }

    itemForConsumption?.let { item ->
        LogConsumptionDialog(
            item = item,
            isPersian = isFa,
            onDismiss = { itemForConsumption = null },
            onConfirm = { qty, purpose, notes ->
                viewModel.recordConsumption(item.id, qty, "FEEDING", null, "$purpose ${notes ?: ""}".trim())
                itemForConsumption = null
            }
        )
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItemEntity,
    isPersian: Boolean,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onConsume: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val stockStatus = InventoryAnalyticsHelper.getStockStatus(item)
    val statusColor = when (stockStatus) {
        StockStatus.OUT_OF_STOCK -> MaterialTheme.colorScheme.error
        StockStatus.LOW_STOCK -> Color(0xFFE65100)
        StockStatus.OPTIMAL -> Color(0xFF2E7D32)
    }

    val statusLabel = when (stockStatus) {
        StockStatus.OUT_OF_STOCK -> if (isPersian) "ناموجود" else "Out of Stock"
        StockStatus.LOW_STOCK -> if (isPersian) "کمبود موجودی" else "Low Stock"
        StockStatus.OPTIMAL -> if (isPersian) "مطلوب" else "In Stock"
    }

    val categoryLabel = when (item.category) {
        InventoryCategory.FOOD -> if (isPersian) "دان و خوراک" else "Food"
        InventoryCategory.SUPPLEMENTS -> if (isPersian) "مکمل" else "Supplements"
        InventoryCategory.MEDICINE -> if (isPersian) "دارو" else "Medicine"
        InventoryCategory.EQUIPMENT -> if (isPersian) "تجهیزات" else "Equipment"
        InventoryCategory.CONSUMABLES -> if (isPersian) "ملزومات" else "Consumables"
        else -> item.category
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("inventory_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Name, SKU, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "SKU: ${item.sku} • $categoryLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isPersian) "موجودی فعلی" else "Current Stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${"%.1f".format(item.currentStock)} ${item.unit}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }

                Column {
                    Text(
                        text = if (isPersian) "آستانه هشدار" else "Min Threshold",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${"%.1f".format(item.minStockThreshold)} ${item.unit}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column {
                    Text(
                        text = if (isPersian) "قیمت واحد" else "Cost/Unit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${"%.2f".format(item.costPerUnit)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column {
                    Text(
                        text = if (isPersian) "ارزش کل" else "Total Value",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${"%.1f".format(item.currentStock * item.costPerUnit)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (!item.storageLocation.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isPersian) "محل انبار: ${item.storageLocation}" else "Location: ${item.storageLocation}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Stock In, Stock Out, Consume, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock In (+)
                Button(
                    onClick = onStockIn,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.testTag("btn_stock_in_${item.id}")
                ) {
                    Icon(
                        Icons.Filled.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPersian) "شارژ" else "Stock In",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Consume (Log Usage)
                Button(
                    onClick = onConsume,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.testTag("btn_consume_${item.id}")
                ) {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPersian) "ثبت مصرف" else "Consume",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Stock Out (Manual deduction)
                IconButton(
                    onClick = onStockOut,
                    modifier = Modifier.size(32.dp).testTag("btn_stock_out_${item.id}")
                ) {
                    Icon(
                        Icons.Filled.RemoveShoppingCart,
                        contentDescription = "Stock Out",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Edit
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).testTag("btn_edit_${item.id}")
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("btn_delete_${item.id}")
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StockTransactionCard(
    transaction: InventoryTransactionEntity,
    itemName: String,
    itemUnit: String,
    dateFormat: SimpleDateFormat,
    isPersian: Boolean
) {
    val isPositive = transaction.transactionType == StockTransactionType.STOCK_IN
    val isConsumption = transaction.transactionType == StockTransactionType.CONSUMPTION

    val typeColor = when (transaction.transactionType) {
        StockTransactionType.STOCK_IN -> Color(0xFF2E7D32)
        StockTransactionType.CONSUMPTION -> MaterialTheme.colorScheme.primary
        StockTransactionType.STOCK_OUT -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }

    val typeLabel = when (transaction.transactionType) {
        StockTransactionType.STOCK_IN -> if (isPersian) "ورود به انبار" else "Stock In"
        StockTransactionType.CONSUMPTION -> if (isPersian) "مصرف سالن" else "Consumption"
        StockTransactionType.STOCK_OUT -> if (isPersian) "کسر دستی" else "Stock Out"
        else -> transaction.transactionType
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("tx_card_${transaction.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(typeColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isPositive -> Icons.Filled.AddShoppingCart
                        isConsumption -> Icons.Filled.Restaurant
                        else -> Icons.Filled.RemoveShoppingCart
                    },
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = itemName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${if (isPositive) "+" else "-"}${"%.1f".format(transaction.quantity)} $itemUnit",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = typeColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$typeLabel • ${dateFormat.format(Date(transaction.date))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (transaction.totalCost > 0) {
                        Text(
                            text = "$${"%.2f".format(transaction.totalCost)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!transaction.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

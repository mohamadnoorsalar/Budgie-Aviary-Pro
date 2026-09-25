package com.example.feature.reports

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.export.ExcelExporter
import com.example.core.export.GenericReportPdfExporter
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.ui.components.StatMetricCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN

    val filterState by viewModel.filterState.collectAsState()
    val facilityPerf by viewModel.facilityPerformance.collectAsState()
    val cagesList by viewModel.cages.collectAsState()
    val birdsList by viewModel.birds.collectAsState()

    var isFilterPanelExpanded by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    val (headers, rows) = remember(filterState, viewModel.birds.collectAsState().value, viewModel.pairsWithDetails.collectAsState().value, viewModel.eggs.collectAsState().value, viewModel.chicks.collectAsState().value, viewModel.genetics.collectAsState().value, viewModel.pedigrees.collectAsState().value, viewModel.healthRecords.collectAsState().value, viewModel.weightRecords.collectAsState().value, viewModel.inventoryItems.collectAsState().value, viewModel.expenses.collectAsState().value, viewModel.incomes.collectAsState().value, viewModel.competitions.collectAsState().value) {
        viewModel.computeTableData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen")
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header Title Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isFa) "مرکز گزارشات و خروجی اکسل و PDF" else "Reports & Export Center",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isFa) "استخراج گزارشات تفصیلی ۱۴ گانه با فیلتر هوشمند" else "Export 14 aviary modules to PDF & UTF-8 Excel/CSV",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // PDF Export Button
                        Button(
                            onClick = {
                                isExporting = true
                                val filterSummary = buildString {
                                    append(filterState.category.titleEn)
                                    if (filterState.status != "ALL") append(" | Status: ${filterState.status}")
                                    if (filterState.cageCode.isNotBlank()) append(" | Cage: ${filterState.cageCode}")
                                    if (filterState.dateRangeDays > 0) append(" | Last ${filterState.dateRangeDays}d")
                                }
                                val pdfFile = GenericReportPdfExporter.exportTableReport(
                                    context = context,
                                    reportTitleEn = "Aviary Report - ${filterState.category.titleEn}",
                                    reportTitleFa = "گزارش سالن - ${filterState.category.titleFa}",
                                    filterSummary = filterSummary,
                                    headers = headers,
                                    rows = rows,
                                    isPersian = isFa
                                )
                                isExporting = false
                                if (pdfFile != null) {
                                    Toast.makeText(context, if (isFa) "فایل PDF با موفقیت تولید شد" else "PDF Report Generated", Toast.LENGTH_SHORT).show()
                                    com.example.core.export.BirdDossierPdfExporter.sharePdf(context, pdfFile, title = "Aviary PDF Report")
                                } else {
                                    Toast.makeText(context, if (isFa) "خطا در تولید PDF" else "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("reports_export_pdf_button")
                        ) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Excel Export Button
                        Button(
                            onClick = {
                                isExporting = true
                                val file = ExcelExporter.exportToCsv(
                                    context = context,
                                    fileNamePrefix = "Aviary_${filterState.category.name}",
                                    headers = headers,
                                    rows = rows
                                )
                                isExporting = false
                                if (file != null) {
                                    Toast.makeText(context, if (isFa) "خروجی اکسل با موفقیت ذخیره شد" else "Excel/CSV Report Exported", Toast.LENGTH_SHORT).show()
                                    ExcelExporter.shareCsv(context, file, title = "Export: ${filterState.category.titleEn}")
                                } else {
                                    Toast.makeText(context, if (isFa) "خطا در استخراج اکسل" else "Failed to export Excel", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("reports_export_excel_button")
                        ) {
                            Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Categories Scrollable Horizontal Bar
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(ReportCategory.values()) { cat ->
                val isSelected = filterState.category == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCategory(cat) },
                    label = {
                        Text(
                            text = if (isFa) cat.titleFa else cat.titleEn,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(cat),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("report_category_chip_${cat.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Bar & Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(if (isFa) "جستجو در رکوردها..." else "Search records...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (filterState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("reports_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedButton(
                onClick = { isFilterPanelExpanded = !isFilterPanelExpanded },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isFilterPanelExpanded) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .height(50.dp)
                    .testTag("reports_filter_toggle_button")
            ) {
                Icon(Icons.Filled.FilterList, contentDescription = "Filter", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isFa) "فیلترها" else "Filters", fontSize = 12.sp)
            }
        }

        // Expandable Filter Tray
        AnimatedVisibility(visible = isFilterPanelExpanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isFa) "تنظیم پارامترهای فیلتر گزارش" else "Filter Parameters",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    // Date range chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val dateOptions = listOf(
                            0 to if (isFa) "همه زمان‌ها" else "All Time",
                            7 to if (isFa) "۷ روز اخیر" else "7 Days",
                            30 to if (isFa) "۳۰ روز اخیر" else "30 Days",
                            90 to if (isFa) "۳ ماه اخیر" else "90 Days",
                            365 to if (isFa) "۱ سال اخیر" else "1 Year"
                        )
                        dateOptions.forEach { (days, label) ->
                            FilterChip(
                                selected = filterState.dateRangeDays == days,
                                onClick = { viewModel.updateFilters(dateRangeDays = days) },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.testTag("filter_date_$days")
                            )
                        }
                    }

                    // Cage & Status row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cage filter
                        var cageMenuExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = cageMenuExpanded,
                            onExpandedChange = { cageMenuExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = if (filterState.cageCode.isBlank()) (if (isFa) "همه قفس‌ها" else "All Cages") else filterState.cageCode,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isFa) "قفس" else "Cage", fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cageMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = cageMenuExpanded,
                                onDismissRequest = { cageMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (isFa) "همه قفس‌ها" else "All Cages") },
                                    onClick = {
                                        viewModel.updateFilters(cageCode = "")
                                        cageMenuExpanded = false
                                    }
                                )
                                cagesList.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("Cage ${c.code} (${c.location ?: "-"})") },
                                        onClick = {
                                            viewModel.updateFilters(cageCode = c.code)
                                            cageMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Status filter
                        var statusMenuExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = statusMenuExpanded,
                            onExpandedChange = { statusMenuExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = filterState.status,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isFa) "وضعیت" else "Status", fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = statusMenuExpanded,
                                onDismissRequest = { statusMenuExpanded = false }
                            ) {
                                listOf("ALL", "AVAILABLE", "BREEDING", "ACTIVE", "INACTIVE", "FERTILE", "HATCHED", "DECEASED").forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st) },
                                        onClick = {
                                            viewModel.updateFilters(status = st)
                                            statusMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Clear Filters Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { viewModel.clearFilters() },
                            modifier = Modifier.testTag("reports_clear_filters_button")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isFa) "پاک کردن فیلترها" else "Clear All Filters", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Summary Metric Highlight for Facility Performance
        if (filterState.category == ReportCategory.FACILITY_PERFORMANCE) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMetricCard(
                    title = if (isFa) "نرخ باروری تخم‌ها" else "Egg Fertility Rate",
                    value = String.format(Locale.US, "%.1f%%", facilityPerf.fertilityRate),
                    icon = Icons.Filled.Egg,
                    iconTint = Color(0xFF1E88E5),
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = if (isFa) "نرخ بقای جوجه‌ها" else "Chick Survival",
                    value = String.format(Locale.US, "%.1f%%", facilityPerf.chickSurvivalRate),
                    icon = Icons.Filled.CheckCircle,
                    iconTint = Color(0xFF43A047),
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = if (isFa) "سود خالص سالن" else "Net Aviary Profit",
                    value = "$${facilityPerf.netProfit}",
                    icon = Icons.Filled.AccountBalance,
                    iconTint = if (facilityPerf.netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Data Table Header & Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${if (isFa) "تعداد رکوردهای منطبق:" else "Matching Records:"} ${rows.size}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isExporting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isFa) "در حال پردازش..." else "Processing...", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Scrollable Table View
        val horizontalScrollState = rememberScrollState()

        if (rows.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isFa) "هیچ رکوردی با فیلترهای انتخابی یافت نشد." else "No records match current filter criteria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                            .testTag("reports_data_table")
                    ) {
                        // Header Row
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                                ) {
                                    headers.forEach { header ->
                                        Text(
                                            text = header,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.width(135.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Data Rows
                        items(rows.size) { rIdx ->
                            val rowData = rows[rIdx]
                            val isAlt = rIdx % 2 == 1
                            Surface(
                                color = if (isAlt) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    rowData.forEach { cellVal ->
                                        val isEmpty = cellVal == "—" || cellVal.isBlank()
                                        Text(
                                            text = cellVal,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isEmpty) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.width(135.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

private fun getCategoryIcon(cat: ReportCategory): ImageVector {
    return when (cat) {
        ReportCategory.BIRDS -> Icons.Filled.Pets
        ReportCategory.PAIRS -> Icons.Filled.Favorite
        ReportCategory.REPRODUCTION -> Icons.Filled.Egg
        ReportCategory.EGGS -> Icons.Filled.Egg
        ReportCategory.CHICKS -> Icons.Filled.Pets
        ReportCategory.GENETICS -> Icons.Filled.Biotech
        ReportCategory.PEDIGREE -> Icons.Filled.AccountTree
        ReportCategory.HEALTH -> Icons.Filled.MedicalServices
        ReportCategory.WEIGHTS -> Icons.Filled.FitnessCenter
        ReportCategory.MORTALITY -> Icons.Filled.Warning
        ReportCategory.INVENTORY -> Icons.Filled.Inventory2
        ReportCategory.FINANCE -> Icons.Filled.AccountBalance
        ReportCategory.COMPETITIONS -> Icons.Filled.EmojiEvents
        ReportCategory.FACILITY_PERFORMANCE -> Icons.Filled.BarChart
    }
}
